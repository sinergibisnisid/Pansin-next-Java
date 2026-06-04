package com.bjb.pansin.modules.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UpdateUserRequest {
    @Email
    private String email;
    private String fullName;
    private String phone;
    private String nik;
    private String employeeId;
    private UUID organizationId;
    private UUID branchId;
    private Set<String> roleCodes;
    private Boolean enabled;
}
