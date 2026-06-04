package com.bjb.pansin.modules.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BranchRequest {
    @NotNull
    private UUID organizationId;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String phone;
    private String email;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String timezone;
}
