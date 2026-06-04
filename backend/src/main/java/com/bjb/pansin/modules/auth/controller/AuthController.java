package com.bjb.pansin.modules.auth.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.auth.dto.LoginRequest;
import com.bjb.pansin.modules.auth.dto.LoginResponse;
import com.bjb.pansin.modules.auth.dto.OtpRequest;
import com.bjb.pansin.modules.auth.dto.OtpVerifyRequest;
import com.bjb.pansin.modules.auth.dto.RefreshTokenRequest;
import com.bjb.pansin.modules.auth.dto.TokenResponse;
import com.bjb.pansin.modules.auth.dto.VerifyLoginOtpRequest;
import com.bjb.pansin.modules.auth.service.AuthService;
import com.bjb.pansin.modules.auth.service.LoginOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication & token management")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginOtpService loginOtpService;

    @PostMapping("/login")
    @Operation(summary = "Login with username/email + password, returns OTP session")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req,
                                                             HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("OTP sent", (LoginResponse) authService.login(req, clientIp(http))));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new access token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refresh(req.getRefreshToken())));
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP code via WhatsApp/Email")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequest req) {
        authService.requestOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent", null));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and issue JWT")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("OTP verified", authService.verifyOtp(req)));
    }

    @PostMapping("/login/verify-otp")
    @Operation(summary = "Verify login OTP after password authentication")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyLoginOtp(@Valid @RequestBody VerifyLoginOtpRequest req,
                                                                      HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Login success", 
                loginOtpService.verifyLoginOtp(req, clientIp(http))));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token & blacklist access token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest req,
                                                    HttpServletRequest http) {
        String access = extractBearer(http);
        authService.logout(req != null ? req.getRefreshToken() : null, access);
        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    private String clientIp(HttpServletRequest http) {
        String fwd = http.getHeader("X-Forwarded-For");
        return fwd != null && !fwd.isBlank() ? fwd.split(",")[0].trim() : http.getRemoteAddr();
    }

    private String extractBearer(HttpServletRequest http) {
        String header = http.getHeader(AppConstants.HEADER_AUTH);
        if (header != null && header.startsWith(AppConstants.BEARER_PREFIX)) {
            return header.substring(AppConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
