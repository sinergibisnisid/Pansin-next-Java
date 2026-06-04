package com.bjb.pansin.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "secret",
                "this-is-a-very-long-256-bit-secret-key-please-rotate-now-okay-1234");
        ReflectionTestUtils.setField(provider, "accessTtl", 900L);
        ReflectionTestUtils.setField(provider, "refreshTtl", 604800L);
        ReflectionTestUtils.setField(provider, "issuer", "pansin-test");
        ReflectionTestUtils.invokeMethod(provider, "init");
    }

    @Test
    void generateAndParseAccessToken() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "alice", Map.of("role", "ADMIN"));

        Claims claims = provider.parse(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.get("type", String.class)).isEqualTo("ACCESS");
        assertThat(provider.isAccessToken(claims)).isTrue();
        assertThat(provider.isRefreshToken(claims)).isFalse();
    }

    @Test
    void generateAndParseRefreshToken() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateRefreshToken(userId, "alice", "session-1");

        Claims claims = provider.parse(token);
        assertThat(claims.get("sid", String.class)).isEqualTo("session-1");
        assertThat(provider.isRefreshToken(claims)).isTrue();
    }
}
