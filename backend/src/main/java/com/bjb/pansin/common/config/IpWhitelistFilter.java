package com.bjb.pansin.common.config;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class IpWhitelistFilter extends OncePerRequestFilter {

    @Value("${app.security.ip-whitelist.enabled:false}")
    private boolean enabled;

    @Value("${app.security.ip-whitelist.ips:}")
    private String allowedIpsCsv;

    private Set<String> allowedIps = Collections.emptySet();

    @Override
    protected void initFilterBean() {
        if (allowedIpsCsv != null && !allowedIpsCsv.isBlank()) {
            allowedIps = new HashSet<>(Arrays.asList(allowedIpsCsv.split(",")));
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (!enabled || allowedIps.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/actuator/health")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        if (allowedIps.contains(ip)) {
            chain.doFilter(request, response);
        } else {
            log.warn("IP {} blocked by whitelist on {}", ip, path);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("{\"success\":false,\"message\":\"IP not allowed\"}");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        if (fwd != null && !fwd.isBlank()) return fwd.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
