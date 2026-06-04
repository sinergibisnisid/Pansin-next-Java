package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.common.exceptions.UnauthorizedException;
import com.bjb.pansin.modules.auth.dto.VerifyLoginOtpRequest;
import com.bjb.pansin.modules.auth.dto.TokenResponse;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginOtpService {

    private final OtpService otpService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redis;
    private final com.bjb.pansin.common.security.AppUserDetailsService userDetailsService;
    private final com.bjb.pansin.modules.activity.service.ActivityLogService activityLogService;

    @Transactional
    public TokenResponse verifyLoginOtp(VerifyLoginOtpRequest req, String clientIp) {
        String sessionKey = "auth:otp-session:" + req.getOtpSessionId();
        
        // Rate limit: max 3 attempts per session
        String attemptKey = sessionKey + ":attempt";
        Long attempts = redis.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1L) {
            redis.expire(attemptKey, java.time.Duration.ofMinutes(5));
        }
        if (attempts != null && attempts > 3) {
            redis.delete(sessionKey);
            redis.delete(attemptKey);
            throw new BusinessException("OTP_MAX_ATTEMPT", "Maximum OTP attempts exceeded");
        }

        // Get session metadata
        Map<Object, Object> sessionData = redis.opsForHash().entries(sessionKey);
        if (sessionData.isEmpty()) {
            throw new UnauthorizedException("OTP session expired or invalid");
        }

        String username = (String) sessionData.get("username");
        String sessionIp = (String) sessionData.get("ip");

        // Optional: verify IP hasn't changed
        if (sessionIp != null && !sessionIp.equals(clientIp)) {
            log.warn("OTP verify from different IP: session={}, current={}", sessionIp, clientIp);
        }

        // Verify OTP
        if (!otpService.verify(username, req.getOtp())) {
            activityLogService.log("LOGIN_OTP_FAILED", "Invalid OTP", clientIp, null, null);
            throw new UnauthorizedException("Invalid OTP");
        }

        // OTP valid, clean up session
        redis.delete(sessionKey);
        redis.delete(attemptKey);

        // Update user last login
        UUID userId = UUID.fromString((String) sessionData.get("userId"));
        userRepository.findById(userId).ifPresent(u -> {
            u.setLastLoginAt(Instant.now());
            u.setLastLoginIp(clientIp);
            u.setFailedAttempts(0);
            userRepository.save(u);
        });

        activityLogService.log("LOGIN_SUCCESS", "User logged in", clientIp, null, null);

        // Build JWT response
        var principal = (com.bjb.pansin.common.security.AppUserPrincipal) 
                userDetailsService.loadUserByUsername(username);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", principal.getRoles());
        if (principal.getBranchId() != null) {
            claims.put("branch", principal.getBranchId().toString());
        }

        TokenService.IssuedTokens tokens = tokenService.issue(
                principal.getId(), principal.getUsername(), claims);

        User user = userRepository.findById(principal.getId()).orElseThrow();
        return TokenResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .tokenType("Bearer")
                .expiresIn(tokens.expiresIn())
                .user(TokenResponse.UserInfo.builder()
                        .id(principal.getId().toString())
                        .username(principal.getUsername())
                        .email(principal.getEmail())
                        .fullName(user.getFullName())
                        .branchId(principal.getBranchId() != null ? principal.getBranchId().toString() : null)
                        .roles(principal.getRoles())
                        .permissions(principal.getPermissions())
                        .build())
                .build();
    }
}
