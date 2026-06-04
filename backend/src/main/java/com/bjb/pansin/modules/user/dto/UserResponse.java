package com.bjb.pansin.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String nik;
    private String employeeId;
    private String avatarUrl;
    private UUID organizationId;
    private UUID branchId;
    private boolean enabled;
    private boolean locked;
    private Set<String> roles;
    private Set<String> permissions;
    private Instant lastLoginAt;
    private Instant createdAt;
}
