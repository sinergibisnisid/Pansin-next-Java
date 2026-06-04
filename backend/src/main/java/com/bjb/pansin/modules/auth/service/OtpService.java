package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.ttl-seconds}")
    private long ttlSeconds;

    @Value("${app.otp.resend-cooldown-seconds}")
    private long cooldownSeconds;

    public String generateAndStore(String identifier) {
        String cooldownKey = AppConstants.REDIS_KEY_OTP + "cooldown:" + identifier;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            throw new BusinessException("OTP_COOLDOWN", "OTP recently sent, please wait");
        }

        String otp = generate();
        String key = AppConstants.REDIS_KEY_OTP + identifier;
        redis.opsForValue().set(key, otp, Duration.ofSeconds(ttlSeconds));
        redis.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(cooldownSeconds));
        log.debug("OTP issued for {}", identifier);
        return otp;
    }

    public boolean verify(String identifier, String otp) {
        String key = AppConstants.REDIS_KEY_OTP + identifier;
        String stored = redis.opsForValue().get(key);
        if (stored == null) return false;

        boolean ok = stored.equals(otp);
        if (ok) redis.delete(key);
        return ok;
    }

    private String generate() {
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
