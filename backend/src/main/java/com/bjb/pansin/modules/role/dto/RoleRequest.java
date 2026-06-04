package com.bjb.pansin.modules.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    private Set<String> permissionCodes;
}
