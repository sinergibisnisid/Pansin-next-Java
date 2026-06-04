package com.bjb.pansin.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
@AllArgsConstructor
public class AppUserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String password;
    private final UUID branchId;
    private final UUID organizationId;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final boolean enabled;
    private final boolean accountNonLocked;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                roles.stream().map(r -> "ROLE_" + r),
                permissions.stream()
        ).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
