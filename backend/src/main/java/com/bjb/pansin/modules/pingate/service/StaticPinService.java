package com.bjb.pansin.modules.pingate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Simple static PIN service as alternative to TOTP
 * For production, store PIN hash in database per admin user
 */
@Slf4j
@Service
public class StaticPinService {

    private final StringRedisTemplate redis;

    @Value("${app.security.static-pin:123456}")
    private String staticPin;

    public StaticPinService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Verify static PIN with rate limiting
     */
    public boolean verify(String pin, String clientIp) {
        // Rate limiting
        try {
            String rateLimitKey = "pin:attempt:" + clientIp;
            Long attempts = redis.opsForValue().increment(rateLimitKey);
            if (attempts != null && attempts == 1L) {
                redis.expire(rateLimitKey, Duration.ofMinutes(5));
            }
            if (attempts != null && attempts > 3) {
                log.warn("PIN rate limit exceeded for IP {}", clientIp);
                return false;
            }
        } catch (Exception e) {
            log.warn("Redis not available for rate limiting: {}", e.getMessage());
        }

        boolean valid = staticPin.equals(pin);
        
        if (valid) {
            try {
                redis.delete("pin:attempt:" + clientIp);
            } catch (Exception e) {
                log.debug("Could not clear rate limit key: {}", e.getMessage());
            }
        }
        
        return valid;
    }
}
