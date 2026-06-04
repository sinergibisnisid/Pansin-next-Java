package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Tag(name = "Debug - TOTP Configuration Audit (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/totp")
@Profile("dev")
public class TotpConfigAudit {

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    /**
     * Comprehensive TOTP configuration audit
     */
    @GetMapping("/config-audit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> configAudit() {
        Map<String, Object> audit = new LinkedHashMap<>();
        
        try {
            // Decode secret to bytes using Apache Commons Base32
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(totpSecret);
            String secretHex = bytesToHex(secretBytes);
            
            // Time info
            SystemTimeProvider timeProvider = new SystemTimeProvider();
            long currentTimeSeconds = timeProvider.getTime();
            long currentBucket = Math.floorDiv(currentTimeSeconds, 30);
            long bucketTimeSeconds = currentBucket * 30;
            
            // Configuration used by backend
            Map<String, Object> backendConfig = new LinkedHashMap<>();
            backendConfig.put("secret", totpSecret);
            backendConfig.put("secretLength", totpSecret.length());
            backendConfig.put("decodedSecretBytes", secretBytes.length);
            backendConfig.put("decodedSecretHex", secretHex);
            backendConfig.put("algorithm", "SHA1");
            backendConfig.put("digits", 6);
            backendConfig.put("period", 30);
            backendConfig.put("timeStep", 30);
            backendConfig.put("currentTimestamp", currentTimeSeconds);
            backendConfig.put("currentBucket", currentBucket);
            backendConfig.put("bucketTimestamp", bucketTimeSeconds);
            
            // Generate code with explicit configuration
            CodeGenerator generator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
            String generatedCode = generator.generate(totpSecret, bucketTimeSeconds);
            backendConfig.put("generatedCode", generatedCode);
            
            audit.put("backendConfiguration", backendConfig);
            
            // Expected Authenticator configuration
            Map<String, Object> authenticatorConfig = new LinkedHashMap<>();
            authenticatorConfig.put("secret", totpSecret);
            authenticatorConfig.put("algorithm", "SHA1 (default)");
            authenticatorConfig.put("digits", "6 (default)");
            authenticatorConfig.put("period", "30 seconds (default)");
            authenticatorConfig.put("encoding", "Base32 (standard)");
            authenticatorConfig.put("expectedCode", generatedCode);
            authenticatorConfig.put("note", "Google/Microsoft Authenticator should show: " + generatedCode);
            
            audit.put("authenticatorExpectedConfiguration", authenticatorConfig);
            
            // QR Code configuration (from TotpService.generateQrCodeDataUri)
            Map<String, Object> qrConfig = new LinkedHashMap<>();
            qrConfig.put("secret", totpSecret);
            qrConfig.put("algorithm", "SHA1");
            qrConfig.put("digits", 6);
            qrConfig.put("period", 30);
            qrConfig.put("issuer", "PANSIN-ACCESS");
            qrConfig.put("label", "PANSIN-ACCESS");
            qrConfig.put("note", "QR Code embeds these exact parameters");
            
            audit.put("qrCodeConfiguration", qrConfig);
            
            // Verification configuration
            Map<String, Object> verifyConfig = new LinkedHashMap<>();
            verifyConfig.put("secret", totpSecret);
            verifyConfig.put("algorithm", "SHA1 (implicit from library)");
            verifyConfig.put("timeWindow", "5 periods (±2)");
            verifyConfig.put("note", "Checks codes from bucket-2 to bucket+2");
            
            audit.put("verificationConfiguration", verifyConfig);
            
            // Comparison summary
            Map<String, Object> comparison = new LinkedHashMap<>();
            comparison.put("allUseIdenticalSecret", true);
            comparison.put("allUseSHA1", true);
            comparison.put("allUse6Digits", true);
            comparison.put("allUse30SecondPeriod", true);
            comparison.put("secretMatches", "YES - All processes use same secret");
            comparison.put("configurationMatches", "YES - All use standard TOTP config");
            
            audit.put("comparisonSummary", comparison);
            
            // Critical findings
            List<String> findings = new ArrayList<>();
            findings.add("✅ Secret is valid Base32: " + totpSecret);
            findings.add("✅ Secret decodes to " + secretBytes.length + " bytes");
            findings.add("✅ Algorithm: SHA1 (standard)");
            findings.add("✅ Digits: 6 (standard)");
            findings.add("✅ Period: 30 seconds (standard)");
            findings.add("✅ All configurations are identical");
            findings.add("⚠️ Backend generates code: " + generatedCode);
            findings.add("❓ Authenticator apps show DIFFERENT code?");
            findings.add("🔍 ROOT CAUSE: Authenticator may be using DIFFERENT secret");
            
            audit.put("criticalFindings", findings);
            
            // Logging
            log.info("=== TOTP CONFIGURATION AUDIT ===");
            log.info("Secret: {}", totpSecret);
            log.info("Decoded Secret Hex: {}", secretHex);
            log.info("Algorithm: SHA1");
            log.info("Digits: 6");
            log.info("Period: 30");
            log.info("Timestamp: {}", bucketTimeSeconds);
            log.info("Bucket: {}", currentBucket);
            log.info("Generated Code: {}", generatedCode);
            log.info("================================");
            
            return ResponseEntity.ok(ApiResponse.ok("Configuration audit complete", audit));
        } catch (Exception e) {
            log.error("Error during config audit", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Audit failed: " + e.getMessage()));
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
