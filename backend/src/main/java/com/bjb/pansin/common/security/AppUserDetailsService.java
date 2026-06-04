package com.bjb.pansin.common.security;

import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findActiveByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        return toPrincipal(user);
    }

    public AppUserPrincipal toPrincipal(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getCode())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toSet());

        return AppUserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .roles(roles)
                .permissions(permissions)
                .enabled(user.isEnabled())
                .accountNonLocked(!user.isLocked())
                .build();
    }
}
