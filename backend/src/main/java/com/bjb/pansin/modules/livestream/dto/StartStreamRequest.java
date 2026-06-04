package com.bjb.pansin.modules.livestream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StartStreamRequest {
    @NotNull
    private UUID vaultId;
    private UUID deviceId;
}
