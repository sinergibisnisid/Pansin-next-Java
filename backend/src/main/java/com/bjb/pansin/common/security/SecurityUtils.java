package com.bjb.pansin.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<AppUserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AppUserPrincipal p)) {
            return Optional.empty();
        }
        return Optional.of(p);
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentPrincipal().map(AppUserPrincipal::getId);
    }

    public static Optional<UUID> getCurrentBranchId() {
        return getCurrentPrincipal().map(AppUserPrincipal::getBranchId);
    }

    public static String getCurrentUsername() {
        return getCurrentPrincipal().map(AppUserPrincipal::getUsername).orElse("system");
    }
}
