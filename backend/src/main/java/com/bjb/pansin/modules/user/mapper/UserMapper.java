package com.bjb.pansin.modules.user.mapper;

import com.bjb.pansin.modules.user.dto.UserResponse;
import com.bjb.pansin.modules.user.entity.User;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .nik(u.getNik())
                .employeeId(u.getEmployeeId())
                .avatarUrl(u.getAvatarUrl())
                .organizationId(u.getOrganization() != null ? u.getOrganization().getId() : null)
                .branchId(u.getBranch() != null ? u.getBranch().getId() : null)
                .enabled(u.isEnabled())
                .locked(u.isLocked())
                .roles(u.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet()))
                .permissions(u.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getCode())
                        .collect(Collectors.toSet()))
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
