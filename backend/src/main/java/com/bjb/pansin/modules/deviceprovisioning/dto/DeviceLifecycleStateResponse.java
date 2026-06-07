package com.bjb.pansin.modules.deviceprovisioning.dto;

import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.deviceprovisioning.entity.DeviceLifecycleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLifecycleStateResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceCode;
    private String deviceName;
    private String state;
    private String previousState;
    private String reason;
    private String actorName;
    private Map<String, Object> metadata;
    private Instant createdAt;

    public static DeviceLifecycleStateResponse from(DeviceLifecycleState state) {
        Device device = state.getDevice();
        return DeviceLifecycleStateResponse.builder()
                .id(state.getId())
                .deviceId(device != null ? device.getId() : null)
                .deviceCode(device != null ? device.getDeviceCode() : null)
                .deviceName(device != null ? device.getName() : null)
                .state(state.getState())
                .previousState(state.getPreviousState())
                .reason(state.getReason())
                .actorName(state.getActor() != null ? state.getActor().getUsername() : null)
                .metadata(state.getMetadata())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
