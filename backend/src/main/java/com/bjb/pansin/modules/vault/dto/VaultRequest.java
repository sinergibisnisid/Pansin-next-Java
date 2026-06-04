package com.bjb.pansin.modules.vault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VaultRequest {
    @NotNull
    private UUID branchId;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String location;
}
