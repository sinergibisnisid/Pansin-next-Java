package com.bjb.pansin.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-ttl}")
    private long accessTtl;

    @Value("${app.jwt.refresh-token-ttl}")
    private long refreshTtl;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private SecretKey key;

    public enum TokenType { ACCESS, REFRESH }

    @PostConstruct
    void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits)");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(UUID userId, String username, Map<String, Object> claims) {
        return build(userId, username, claims, TokenType.ACCESS, accessTtl);
    }

    public String generateRefreshToken(UUID userId, String username, String sessionId) {
        return build(userId, username, Map.of("sid", sessionId), TokenType.REFRESH, refreshTtl);
    }

    private String build(UUID userId, String username, Map<String, Object> claims,
                         TokenType type, long ttlSeconds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlSeconds * 1000);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .subject(userId.toString())
                .claim("username", username)
                .claim("type", type.name())
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return TokenType.ACCESS.name().equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TokenType.REFRESH.name().equals(claims.get("type", String.class));
    }

    public long getAccessTtl() {
        return accessTtl;
    }

    public long getRefreshTtl() {
        return refreshTtl;
    }
}
