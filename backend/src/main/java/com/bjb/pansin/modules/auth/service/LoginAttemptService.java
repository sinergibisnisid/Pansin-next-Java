package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redis;

    @Value("${app.security.login.max-failed-attempts}")
    private int maxAttempts;

    @Value("${app.security.login.lock-duration-minutes}")
    private long lockMinutes;

    public void recordFailed(String identifier) {
        String key = AppConstants.REDIS_KEY_LOGIN_FAIL + identifier;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, Duration.ofMinutes(15));
        }
        if (count != null && count >= maxAttempts) {
            String lockKey = AppConstants.REDIS_KEY_LOGIN_LOCK + identifier;
            redis.opsForValue().set(lockKey, "1", Duration.ofMinutes(lockMinutes));
            log.warn("Identifier {} locked after {} failed attempts", identifier, count);
        }
    }

    public boolean isLocked(String identifier) {
        String lockKey = AppConstants.REDIS_KEY_LOGIN_LOCK + identifier;
        return Boolean.TRUE.equals(redis.hasKey(lockKey));
    }

    public void reset(String identifier) {
        redis.delete(AppConstants.REDIS_KEY_LOGIN_FAIL + identifier);
        redis.delete(AppConstants.REDIS_KEY_LOGIN_LOCK + identifier);
    }
}
