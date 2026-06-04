package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Tag(name = "Debug - Secret Validation (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/totp")
@Profile("dev")
public class TotpSecretValidator {

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    /**
     * Validate and test secret
     */
    @GetMapping("/validate-secret")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateSecret() {
        Map<String, Object> validation = new LinkedHashMap<>();
        
        try {
            // Secret info
            validation.put("secret", totpSecret);
            validation.put("secretLength", totpSecret.length());
            
            // Check if valid Base32
            boolean validBase32 = isValidBase32(totpSecret);
            validation.put("isValidBase32", validBase32);
            
            if (!validBase32) {
                validation.put("error", "Secret is NOT valid Base32! This is the root cause.");
                validation.put("validBase32Chars", "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");
                validation.put("note", "TOTP requires Base32 encoded secrets");
            }
            
            // Try to generate code
            try {
                CodeGenerator generator = new DefaultCodeGenerator();
                SystemTimeProvider timeProvider = new SystemTimeProvider();
                long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
                String code = generator.generate(totpSecret, currentBucket * 30);
                validation.put("canGenerateCode", true);
                validation.put("testCode", code);
            } catch (Exception e) {
                validation.put("canGenerateCode", false);
                validation.put("generationError", e.getMessage());
            }
            
            // Char analysis
            List<Map<String, Object>> charAnalysis = new ArrayList<>();
            for (int i = 0; i < totpSecret.length(); i++) {
                char c = totpSecret.charAt(i);
                Map<String, Object> charInfo = new LinkedHashMap<>();
                charInfo.put("position", i);
                charInfo.put("char", String.valueOf(c));
                charInfo.put("isBase32", isBase32Char(c));
                charInfo.put("ascii", (int) c);
                charAnalysis.add(charInfo);
            }
            validation.put("characterAnalysis", charAnalysis);
            
            log.info("=== SECRET VALIDATION ===");
            log.info("Secret: {}", totpSecret);
            log.info("Length: {}", totpSecret.length());
            log.info("Valid Base32: {}", validBase32);
            log.info("========================");
            
            return ResponseEntity.ok(ApiResponse.ok("Secret validation complete", validation));
        } catch (Exception e) {
            log.error("Error validating secret", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Validation failed: " + e.getMessage()));
        }
    }
    
    private boolean isValidBase32(String input) {
        if (input == null || input.isEmpty()) return false;
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        for (char c : input.toCharArray()) {
            if (base32Chars.indexOf(c) == -1) return false;
        }
        return true;
    }
    
    private boolean isBase32Char(char c) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        return base32Chars.indexOf(c) != -1;
    }
}
