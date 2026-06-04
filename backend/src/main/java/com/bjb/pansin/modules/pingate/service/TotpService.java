package com.bjb.pansin.modules.pingate.service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Slf4j
@Service
public class TotpService {

    private final CodeVerifier verifier;
    private final QrGenerator qrGenerator;
    private final StringRedisTemplate redis;

    @Value("${app.security.totp.secret}")
    private String totpSecret;

    @Value("${app.security.totp.issuer:PANSIN-ACCESS}")
    private String issuer;

    public TotpService(StringRedisTemplate redis) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        // DefaultCodeVerifier with discrepancy allowance (allows 2 periods = 1 minute drift)
        this.verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        this.qrGenerator = new ZxingPngQrGenerator();
        this.redis = redis;
    }

    /**
     * Verify TOTP code from user input.
     * Rate-limited: max 5 attempts per IP per minute.
     */
    public boolean verify(String code, String clientIp) {
        // Rate limiting with Redis (optional - continues without Redis in dev mode)
        try {
            String rateLimitKey = "totp:attempt:" + clientIp;
            Long attempts = redis.opsForValue().increment(rateLimitKey);
            if (attempts != null && attempts == 1L) {
                redis.expire(rateLimitKey, Duration.ofMinutes(1));
            }
            if (attempts != null && attempts > 5) {
                log.warn("TOTP rate limit exceeded for IP {}", clientIp);
                return false;
            }
        } catch (Exception e) {
            log.warn("Redis not available for rate limiting, continuing without rate limit: {}", e.getMessage());
        }

        // DEBUG: Log verification details
        log.info("DEBUG: Verifying code='{}' against secret='{}'", code, totpSecret);
        
        // Manual RFC 6238 verification with time window
        boolean valid = false;
        try {
            Base32 base32 = new Base32();
            byte[] secretBytes = base32.decode(totpSecret);
            long currentBucket = System.currentTimeMillis() / 1000L / 30L;
            
            // Check current period and 2 periods before/after (total 5 periods = 2.5 min window)
            for (int i = -2; i <= 2; i++) {
                try {
                    String expectedCode = generateTotpRfc6238(secretBytes, currentBucket + i);
                    log.debug("DEBUG: Checking period offset {}: expected={}, provided={}", i, expectedCode, code);
                    if (expectedCode.equals(code)) {
                        valid = true;
                        log.info("DEBUG: Code matched at period offset {}", i);
                        break;
                    }
                } catch (Exception e) {
                    log.error("Error generating code for offset {}", i, e);
                }
            }
        } catch (Exception e) {
            log.error("Error during TOTP verification", e);
        }
        
        log.info("DEBUG: Verification result: {}", valid);
        
        // Clean up rate limit on success
        if (valid) {
            try {
                redis.delete("totp:attempt:" + clientIp);
            } catch (Exception e) {
                log.debug("Could not clear rate limit key: {}", e.getMessage());
            }
        }
        
        return valid;
    }

    /**
     * Generate QR code data URI for initial setup.
     * Should only be called by admin, not exposed to public.
     */
    public String generateQrCodeDataUri(String label) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(label)
                .secret(totpSecret)
                .issuer(issuer)
                .algorithm(dev.samstevens.totp.code.HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        byte[] imageData = qrGenerator.generate(data);
        return getDataUriForImage(imageData, qrGenerator.getImageMimeType());
    }

    /**
     * Generate a new random secret (for initial setup or rotation).
     * Store this in app.security.totp.secret config.
     */
    public static String generateNewSecret() {
        return new DefaultSecretGenerator().generate();
    }

    /**
     * RFC 6238 TOTP implementation
     * Reference: https://datatracker.ietf.org/doc/html/rfc6238
     * This implementation matches Google/Microsoft Authenticator exactly.
     */
    private String generateTotpRfc6238(byte[] secret, long counter) throws Exception {
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA1");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(counterBytes);
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                     ((hash[offset + 1] & 0xFF) << 16) |
                     ((hash[offset + 2] & 0xFF) << 8) |
                     (hash[offset + 3] & 0xFF);
        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }
}
