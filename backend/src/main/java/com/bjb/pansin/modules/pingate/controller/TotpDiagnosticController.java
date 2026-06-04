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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Tag(name = "Debug - TOTP Diagnosis (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/totp")
@Profile("dev")
public class TotpDiagnosticController {

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    @Value("${app.security.totp.issuer:PANSIN-ACCESS}")
    private String issuer;

    /**
     * Comprehensive TOTP diagnostic endpoint
     */
    @GetMapping("/diagnose")
    public ResponseEntity<ApiResponse<Map<String, Object>>> diagnose() {
        try {
            CodeGenerator generator = new DefaultCodeGenerator();
            SystemTimeProvider timeProvider = new SystemTimeProvider();
            
            long currentTimeSeconds = timeProvider.getTime();
            long currentBucket = Math.floorDiv(currentTimeSeconds, 30);
            long bucketTimeSeconds = currentBucket * 30;
            
            // Generate codes for multiple time windows
            Map<String, Object> diagnosis = new LinkedHashMap<>();
            
            // Secret info
            diagnosis.put("secret", totpSecret);
            diagnosis.put("secretLength", totpSecret.length());
            diagnosis.put("issuer", issuer);
            
            // Time info
            diagnosis.put("serverTimeSeconds", currentTimeSeconds);
            diagnosis.put("serverTimeISO", Instant.ofEpochSecond(currentTimeSeconds).toString());
            diagnosis.put("serverTimeWIB", formatToWIB(currentTimeSeconds));
            diagnosis.put("currentBucket", currentBucket);
            diagnosis.put("bucketTimeSeconds", bucketTimeSeconds);
            diagnosis.put("bucketTimeISO", Instant.ofEpochSecond(bucketTimeSeconds).toString());
            diagnosis.put("bucketTimeWIB", formatToWIB(bucketTimeSeconds));
            
            // Timezone info
            diagnosis.put("jvmTimezone", TimeZone.getDefault().getID());
            diagnosis.put("jvmOffset", TimeZone.getDefault().getRawOffset() / 3600000 + " hours");
            
            // Generate codes for current and adjacent buckets
            List<Map<String, Object>> codes = new ArrayList<>();
            for (int offset = -2; offset <= 2; offset++) {
                long targetBucket = currentBucket + offset;
                long targetTime = targetBucket * 30;
                String code = generator.generate(totpSecret, targetTime);
                
                Map<String, Object> codeInfo = new LinkedHashMap<>();
                codeInfo.put("offset", offset);
                codeInfo.put("bucket", targetBucket);
                codeInfo.put("time", targetTime);
                codeInfo.put("timeISO", Instant.ofEpochSecond(targetTime).toString());
                codeInfo.put("timeWIB", formatToWIB(targetTime));
                codeInfo.put("code", code);
                codeInfo.put("isCurrent", offset == 0);
                
                codes.add(codeInfo);
            }
            diagnosis.put("generatedCodes", codes);
            
            // Expected current code (what authenticator apps should show)
            String currentCode = generator.generate(totpSecret, bucketTimeSeconds);
            diagnosis.put("expectedCurrentCode", currentCode);
            diagnosis.put("note", "Authenticator apps should show: " + currentCode);
            
            // Logging
            log.info("=== TOTP DIAGNOSTIC ===");
            log.info("Secret: {}", totpSecret);
            log.info("Current Time: {} ({})", currentTimeSeconds, Instant.ofEpochSecond(currentTimeSeconds));
            log.info("Bucket Time: {} ({})", bucketTimeSeconds, Instant.ofEpochSecond(bucketTimeSeconds));
            log.info("Expected Code: {}", currentCode);
            log.info("JVM Timezone: {}", TimeZone.getDefault().getID());
            log.info("======================");
            
            return ResponseEntity.ok(ApiResponse.ok("TOTP diagnostic complete", diagnosis));
        } catch (Exception e) {
            log.error("Error during TOTP diagnosis", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Diagnosis failed: " + e.getMessage()));
        }
    }
    
    private String formatToWIB(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.of("Asia/Jakarta"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
}
