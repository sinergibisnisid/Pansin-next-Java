package com.bjb.pansin.modules.device.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOfflineEvent {
    private UUID deviceId;
    private String deviceCode;
    private Instant occurredAt;
}
