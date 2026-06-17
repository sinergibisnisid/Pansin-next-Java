package com.bjb.pansin.modules.branch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private UUID id;
    private OrganizationSummary organization;
    private UUID organizationId;
    private String code;
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
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationSummary {
        private UUID id;
        private String code;
        private String name;
    }
}
