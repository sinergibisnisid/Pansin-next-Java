package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.common.exceptions.UnauthorizedException;
import com.bjb.pansin.common.security.AppUserDetailsService;
import com.bjb.pansin.common.security.AppUserPrincipal;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.activity.service.ActivityLogService;
import com.bjb.pansin.modules.auth.dto.LoginRequest;
import com.bjb.pansin.modules.auth.dto.LoginResponse;
import com.bjb.pansin.modules.auth.dto.OtpRequest;
import com.bjb.pansin.modules.auth.dto.OtpVerifyRequest;
import com.bjb.pansin.modules.auth.dto.TokenResponse;
import com.bjb.pansin.modules.auth.dto.VerifyLoginOtpRequest;
import com.bjb.pansin.modules.notification.service.NotificationService;
import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final LoginAttemptService loginAttemptService;
    private final NotificationService notificationService;
    private final com.bjb.pansin.modules.activity.service.ActivityLogService activityLogService;
    private final org.springframework.data.redis.core.StringRedisTemplate redis;

    @Transactional
    public Object login(LoginRequest req, String clientIp) {
        if (loginAttemptService.isLocked(req.getIdentifier())) {
            throw new BusinessException("ACCOUNT_LOCKED",
                    "Account temporarily locked due to too many failed attempts");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getIdentifier(), req.getPassword()));
        } catch (BadCredentialsException ex) {
            loginAttemptService.recordFailed(req.getIdentifier());
            throw new UnauthorizedException("Invalid credentials");
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Account disabled");
        } catch (LockedException ex) {
            throw new UnauthorizedException("Account locked");
        }

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        loginAttemptService.reset(req.getIdentifier());

        // Generate OTP session instead of JWT directly
        String otpSessionId = UUID.randomUUID().toString();
        String otp = otpService.generateAndStore(principal.getUsername());
        
        // DEV MODE: Log OTP for testing (remove in production)
        log.warn("=== DEV OTP === User: {}, OTP: {} ===", principal.getUsername(), otp);

        // Store session metadata in Redis (5 minutes TTL)
        String sessionKey = "auth:otp-session:" + otpSessionId;
        Map<String, String> sessionData = Map.of(
                "userId", principal.getId().toString(),
                "username", principal.getUsername(),
                "ip", clientIp
        );
        redis.opsForHash().putAll(sessionKey, sessionData);
        redis.expire(sessionKey, Duration.ofMinutes(5));

        // Send OTP
        User user = userRepository.findById(principal.getId()).orElseThrow();
        String message = "PANSIN Login OTP: %s. Berlaku 5 menit.".formatted(otp);
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            notificationService.send(NotificationChannel.WHATSAPP, user.getPhone(),
                    "PANSIN Login OTP", message);
        } else if (user.getEmail() != null) {
            notificationService.send(NotificationChannel.EMAIL, user.getEmail(),
                    "PANSIN Login OTP", message);
        }

        activityLogService.log("LOGIN_OTP_SENT", "OTP sent for login", clientIp, req.getUserAgent(), null);

        return LoginResponse.builder()
                .otpRequired(true)
                .otpSessionId(otpSessionId)
                .expiresIn(300)
                .message("OTP has been sent to your phone/email")
                .build();
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        Map<String, Object> claims = new HashMap<>();
        TokenService.IssuedTokens tokens = tokenService.rotate(refreshToken, claims);

        AppUserPrincipal principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(
                extractUsername(refreshToken));
        return TokenResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .tokenType("Bearer")
                .expiresIn(tokens.expiresIn())
                .user(toUserInfo(principal))
                .build();
    }

    public void logout(String refreshToken, String accessToken) {
        if (refreshToken != null) tokenService.revoke(refreshToken);
        if (accessToken != null) tokenService.blacklistAccess(accessToken);
        activityLogService.log("LOGOUT", "User logged out", null, null, null);
    }

    @Transactional(readOnly = true)
    public TokenResponse.UserInfo getProfile() {
        AppUserPrincipal principal = SecurityUtils.getCurrentPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Unauthenticated"));
        return toUserInfo(principal);
    }

    public void requestOtp(OtpRequest req) {
        User user = userRepository.findActiveByUsernameOrEmail(req.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String otp = otpService.generateAndStore(user.getUsername());
        String message = "PANSIN OTP: %s. Berlaku 5 menit.".formatted(otp);

        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            notificationService.send(NotificationChannel.WHATSAPP, user.getPhone(),
                    "PANSIN OTP", message);
        } else if (user.getEmail() != null) {
            notificationService.send(NotificationChannel.EMAIL, user.getEmail(),
                    "PANSIN OTP", message);
        }
    }

    @Transactional
    public TokenResponse verifyOtp(OtpVerifyRequest req) {
        User user = userRepository.findActiveByUsernameOrEmail(req.getIdentifier())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!otpService.verify(user.getUsername(), req.getOtp())) {
            throw new UnauthorizedException("Invalid OTP");
        }

        AppUserPrincipal principal = userDetailsService.toPrincipal(user);
        return buildTokenResponse(principal);
    }

    private TokenResponse buildTokenResponse(AppUserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", principal.getRoles());
        if (principal.getBranchId() != null) claims.put("branch", principal.getBranchId().toString());

        TokenService.IssuedTokens tokens = tokenService.issue(
                principal.getId(), principal.getUsername(), claims);

        return TokenResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .tokenType("Bearer")
                .expiresIn(tokens.expiresIn())
                .user(toUserInfo(principal))
                .build();
    }

    private TokenResponse.UserInfo toUserInfo(AppUserPrincipal p) {
        User user = userRepository.findById(p.getId()).orElseThrow();
        return TokenResponse.UserInfo.builder()
                .id(p.getId().toString())
                .username(p.getUsername())
                .email(p.getEmail())
                .fullName(user.getFullName())
                .branchId(p.getBranchId() != null ? p.getBranchId().toString() : null)
                .roles(p.getRoles())
                .permissions(p.getPermissions())
                .build();
    }

    private String extractUsername(String refreshToken) {
        return tokenService.parseUsername(refreshToken);
    }
}
