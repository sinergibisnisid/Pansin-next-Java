package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> ops;

    @InjectMocks OtpService service;

    @Test
    void generateAndStoreReturnsNumericCodeOfConfiguredLength() {
        ReflectionTestUtils.setField(service, "otpLength", 6);
        ReflectionTestUtils.setField(service, "ttlSeconds", 300L);
        ReflectionTestUtils.setField(service, "cooldownSeconds", 60L);

        when(redis.hasKey("auth:otp:cooldown:alice")).thenReturn(false);
        when(redis.opsForValue()).thenReturn(ops);

        String otp = service.generateAndStore("alice");

        assertThat(otp).hasSize(6).matches("\\d{6}");
        verify(ops).set(eq("auth:otp:alice"), eq(otp), any());
        verify(ops).set(eq("auth:otp:cooldown:alice"), eq("1"), any());
    }

    @Test
    void generateAndStoreEnforcesCooldown() {
        when(redis.hasKey("auth:otp:cooldown:alice")).thenReturn(true);

        assertThatThrownBy(() -> service.generateAndStore("alice"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OTP recently sent");
    }

    @Test
    void verifyMatchingOtpDeletesAndReturnsTrue() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("auth:otp:alice")).thenReturn("123456");

        assertThat(service.verify("alice", "123456")).isTrue();
        verify(redis).delete("auth:otp:alice");
    }

    @Test
    void verifyWrongOtpReturnsFalseAndKeeps() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("auth:otp:alice")).thenReturn("123456");

        assertThat(service.verify("alice", "999999")).isFalse();
        verify(redis, never()).delete(anyString());
    }

    @Test
    void verifyNoCodeReturnsFalse() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("auth:otp:alice")).thenReturn(null);

        assertThat(service.verify("alice", "any")).isFalse();
    }
}
