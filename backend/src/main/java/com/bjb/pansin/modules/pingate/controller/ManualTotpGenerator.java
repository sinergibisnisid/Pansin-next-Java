package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Debug - Manual TOTP (RFC 6238)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/totp")
@Profile("dev")
public class ManualTotpGenerator {

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    /**
     * Manual TOTP implementation following RFC 6238 strictly
     * This should match Google/Microsoft Authenticator exactly
     */
    @GetMapping("/manual-generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> manualGenerate() {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            // Decode Base32 secret
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(totpSecret);
            
            long currentTime = System.currentTimeMillis() / 1000L;
            long timeStep = 30L;
            long currentBucket = currentTime / timeStep;
            
            // Generate codes for current and adjacent buckets
            result.put("secret", totpSecret);
            result.put("decodedBytesLength", secretBytes.length);
            result.put("currentTime", currentTime);
            result.put("currentBucket", currentBucket);
            result.put("currentTimeISO", Instant.ofEpochSecond(currentTime).toString());
            
            Map<String, Object> codes = new LinkedHashMap<>();
            for (int offset = -2; offset <= 2; offset++) {
                long bucket = currentBucket + offset;
                String code = generateTotpRfc6238(secretBytes, bucket);
                codes.put("offset_" + offset, code);
            }
            result.put("manualCodes_RFC6238", codes);
            
            // Currently expected code
            String currentCode = generateTotpRfc6238(secretBytes, currentBucket);
            result.put("currentExpectedCode", currentCode);
            result.put("note", "This code MUST match Google/Microsoft Authenticator");
            
            log.info("=== MANUAL TOTP RFC 6238 ===");
            log.info("Secret: {}", totpSecret);
            log.info("Current Bucket: {}", currentBucket);
            log.info("Generated Code: {}", currentCode);
            log.info("============================");
            
            return ResponseEntity.ok(ApiResponse.ok("Manual TOTP generated", result));
        } catch (Exception e) {
            log.error("Error generating manual TOTP", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Generation failed: " + e.getMessage()));
        }
    }

    /**
     * RFC 6238 TOTP implementation
     * Reference: https://datatracker.ietf.org/doc/html/rfc6238
     */
    private String generateTotpRfc6238(byte[] secret, long counter) throws Exception {
        // Convert counter to 8-byte big-endian
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
        
        // HMAC-SHA1
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA1");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(counterBytes);
        
        // Dynamic truncation (RFC 4226)
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);
        
        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
}
