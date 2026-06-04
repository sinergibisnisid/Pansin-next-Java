package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.pingate.service.TotpService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Debug - PIN Gate (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/pin")
@RequiredArgsConstructor
@Profile("dev") // Only available in development mode
public class PinGateDebugController {

    private final TotpService totpService;

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    @Value("${app.security.totp.issuer:PANSIN-ACCESS}")
    private String issuer;

    /**
     * Generate current valid TOTP code for testing
     */
    @GetMapping("/current-code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentCode() {
        try {
            CodeGenerator generator = new DefaultCodeGenerator();
            SystemTimeProvider timeProvider = new SystemTimeProvider();
            
            long currentTime = timeProvider.getTime();
            long currentBucket = Math.floorDiv(currentTime, 30);
            long bucketTime = currentBucket * 30;
            String currentCode = generator.generate(totpSecret, bucketTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentCode", currentCode);
            response.put("secret", totpSecret);
            response.put("issuer", issuer);
            response.put("serverTime", Instant.ofEpochSecond(currentTime));
            response.put("bucketTime", Instant.ofEpochSecond(bucketTime));
            response.put("validFor", "30 seconds");
            response.put("note", "Use this code in PIN gate or Google Authenticator should generate the same code");
            
            log.info("DEBUG: Current TOTP code: {} at time: {} (bucket time: {})", 
                    currentCode, Instant.ofEpochSecond(currentTime), Instant.ofEpochSecond(bucketTime));
            
            return ResponseEntity.ok(ApiResponse.ok("Current TOTP code generated", response));
        } catch (Exception e) {
            log.error("Error generating TOTP code", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate code: " + e.getMessage()));
        }
    }

    /**
     * Test TOTP verification manually
     */
    @PostMapping("/test-verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testVerify(@RequestParam String code) {
        try {
            boolean isValid = totpService.verify(code, "debug-test");
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", code);
            response.put("isValid", isValid);
            response.put("secret", totpSecret);
            response.put("serverTime", Instant.now());
            
            log.info("DEBUG: Tested code {} - Result: {}", code, isValid);
            
            return ResponseEntity.ok(ApiResponse.ok(
                    isValid ? "Code is VALID ✅" : "Code is INVALID ❌", 
                    response
            ));
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to verify: " + e.getMessage()));
        }
    }

    /**
     * Get QR code for Google Authenticator setup
     */
    @GetMapping("/qr-code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQrCode() {
        try {
            String qrDataUri = totpService.generateQrCodeDataUri("PANSIN-ACCESS");
            
            Map<String, Object> response = new HashMap<>();
            response.put("qrCodeDataUri", qrDataUri);
            response.put("secret", totpSecret);
            response.put("issuer", issuer);
            response.put("manualEntry", Map.of(
                    "account", "PANSIN-ACCESS",
                    "key", totpSecret,
                    "type", "Time-based",
                    "digits", 6,
                    "period", 30
            ));
            
            return ResponseEntity.ok(ApiResponse.ok("QR code generated", response));
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate QR: " + e.getMessage()));
        }
    }
}
