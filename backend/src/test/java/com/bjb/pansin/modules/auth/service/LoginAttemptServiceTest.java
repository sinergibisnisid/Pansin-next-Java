package com.bjb.pansin.modules.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> ops;

    @InjectMocks private LoginAttemptService service;

    @Test
    void recordFailedSetsExpireOnFirstHit() {
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "lockMinutes", 15L);

        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(1L);

        service.recordFailed("alice");

        verify(ops).increment("auth:fail:alice");
        verify(redis).expire(eq("auth:fail:alice"), any());
        verify(ops, never()).set(eq("auth:lock:alice"), anyString(), any());
    }

    @Test
    void recordFailedLocksAfterThreshold() {
        ReflectionTestUtils.setField(service, "maxAttempts", 3);
        ReflectionTestUtils.setField(service, "lockMinutes", 5L);

        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(anyString())).thenReturn(3L);

        service.recordFailed("bob");

        verify(ops).set(eq("auth:lock:bob"), eq("1"), any());
    }

    @Test
    void isLockedReturnsTrueWhenKeyExists() {
        when(redis.hasKey("auth:lock:carol")).thenReturn(true);
        assertThat(service.isLocked("carol")).isTrue();
    }

    @Test
    void resetClearsBothCounters() {
        service.reset("dave");
        verify(redis).delete("auth:fail:dave");
        verify(redis).delete("auth:lock:dave");
    }
}
