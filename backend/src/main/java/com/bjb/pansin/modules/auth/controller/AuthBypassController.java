package com.bjb.pansin.modules.auth.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.UnauthorizedException;
import com.bjb.pansin.common.security.AppUserDetailsService;
import com.bjb.pansin.common.security.AppUserPrincipal;
import com.bjb.pansin.modules.auth.dto.LoginRequest;
import com.bjb.pansin.modules.auth.dto.TokenResponse;
import com.bjb.pansin.modules.auth.service.TokenService;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * DEV ONLY: Bypass OTP for development/testing
 * DO NOT enable in production!
 */
@Slf4j
@Tag(name = "Debug - Auth Bypass (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/auth")
@RequiredArgsConstructor
@Profile("dev")
public class AuthBypassController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    /**
     * Direct login without OTP (DEV only)
     */
    @PostMapping("/login-no-otp")
    public ResponseEntity<ApiResponse<TokenResponse>> loginNoOtp(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getIdentifier(), req.getPassword()));

            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", principal.getRoles());
            if (principal.getBranchId() != null) {
                claims.put("branch", principal.getBranchId().toString());
            }

            TokenService.IssuedTokens tokens = tokenService.issue(
                    principal.getId(), principal.getUsername(), claims);

            TokenResponse.UserInfo userInfo = TokenResponse.UserInfo.builder()
                    .id(principal.getId().toString())
                    .username(principal.getUsername())
                    .email(principal.getEmail())
                    .fullName(user.getFullName())
                    .branchId(principal.getBranchId() != null ? principal.getBranchId().toString() : null)
                    .roles(principal.getRoles())
                    .permissions(principal.getPermissions())
                    .build();

            TokenResponse response = TokenResponse.builder()
                    .accessToken(tokens.accessToken())
                    .refreshToken(tokens.refreshToken())
                    .tokenType("Bearer")
                    .expiresIn(tokens.expiresIn())
                    .user(userInfo)
                    .build();

            log.warn("=== DEV LOGIN BYPASS === User: {} logged in WITHOUT OTP ===", principal.getUsername());

            return ResponseEntity.ok(ApiResponse.ok("Login success (NO OTP)", response));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid credentials"));
        } catch (Exception e) {
            log.error("Login bypass error", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
}
