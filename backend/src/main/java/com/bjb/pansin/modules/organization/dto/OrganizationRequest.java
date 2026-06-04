package com.bjb.pansin.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
}
