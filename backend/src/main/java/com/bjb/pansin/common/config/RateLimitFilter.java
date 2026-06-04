package com.bjb.pansin.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limit.capacity}")
    private long capacity;

    @Value("${app.security.rate-limit.refill-tokens}")
    private long refillTokens;

    @Value("${app.security.rate-limit.refill-period-seconds}")
    private long refillPeriod;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger") || path.startsWith("/ws")) {
            chain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(refillPeriod));
            response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded\"}");
        }
    }

    private Bucket newBucket() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, Duration.ofSeconds(refillPeriod))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
