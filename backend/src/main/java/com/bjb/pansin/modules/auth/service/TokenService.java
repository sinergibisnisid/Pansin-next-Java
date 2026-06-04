package com.bjb.pansin.modules.auth.service;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redis;

    public IssuedTokens issue(UUID userId, String username, Map<String, Object> claims) {
        String sessionId = UUID.randomUUID().toString();
        String access = tokenProvider.generateAccessToken(userId, username, claims);
        String refresh = tokenProvider.generateRefreshToken(userId, username, sessionId);

        redis.opsForValue().set(
                AppConstants.REDIS_KEY_REFRESH + userId + ":" + sessionId,
                refresh,
                Duration.ofSeconds(tokenProvider.getRefreshTtl()));

        return new IssuedTokens(access, refresh, tokenProvider.getAccessTtl());
    }

    public IssuedTokens rotate(String refreshToken, Map<String, Object> newClaims) {
        Claims claims = tokenProvider.parse(refreshToken);
        if (!tokenProvider.isRefreshToken(claims)) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        String username = claims.get("username", String.class);
        String sessionId = claims.get("sid", String.class);

        String key = AppConstants.REDIS_KEY_REFRESH + userId + ":" + sessionId;
        String stored = redis.opsForValue().get(key);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token not recognized");
        }

        // rotate: invalidate previous, issue new pair (new sid)
        redis.delete(key);
        return issue(userId, username, newClaims);
    }

    public void revoke(String refreshToken) {
        Claims claims = tokenProvider.parse(refreshToken);
        UUID userId = UUID.fromString(claims.getSubject());
        String sessionId = claims.get("sid", String.class);
        redis.delete(AppConstants.REDIS_KEY_REFRESH + userId + ":" + sessionId);
    }

    public void blacklistAccess(String accessToken) {
        Claims claims = tokenProvider.parse(accessToken);
        long ttl = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
        if (ttl > 0) {
            redis.opsForValue().set(
                    AppConstants.REDIS_KEY_BLACKLIST + claims.getId(),
                    "1",
                    Duration.ofSeconds(ttl));
        }
    }

    public String parseUsername(String token) {
        return tokenProvider.parse(token).get("username", String.class);
    }

    public record IssuedTokens(String accessToken, String refreshToken, long expiresIn) {}
}
