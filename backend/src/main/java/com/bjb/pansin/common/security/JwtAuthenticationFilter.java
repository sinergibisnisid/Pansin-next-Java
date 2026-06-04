package com.bjb.pansin.common.security;

import com.bjb.pansin.common.constants.AppConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AppUserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parse(token);

                if (!jwtTokenProvider.isAccessToken(claims)) {
                    chain.doFilter(request, response);
                    return;
                }

                String jti = claims.getId();
                if (jti != null && Boolean.TRUE.equals(
                        redisTemplate.hasKey(AppConstants.REDIS_KEY_BLACKLIST + jti))) {
                    log.debug("Blacklisted token: {}", jti);
                    chain.doFilter(request, response);
                    return;
                }

                String username = claims.get("username", String.class);
                UserDetails ud = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        ud, null, ud.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("JWT validation failed: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception ex) {
                log.warn("Unexpected JWT processing error", ex);
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AppConstants.HEADER_AUTH);
        if (StringUtils.hasText(header) && header.startsWith(AppConstants.BEARER_PREFIX)) {
            return header.substring(AppConstants.BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
