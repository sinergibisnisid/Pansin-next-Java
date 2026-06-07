package com.bjb.pansin.modules.deviceprovisioning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLifecycleStateRequest {
    @NotBlank
    private String state;
    private String reason;
    private Map<String, Object> metadata;
}
