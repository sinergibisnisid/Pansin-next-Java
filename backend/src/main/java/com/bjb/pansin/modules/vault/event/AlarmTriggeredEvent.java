package com.bjb.pansin.modules.vault.event;

import com.bjb.pansin.common.enums.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmTriggeredEvent {
    private UUID vaultId;
    private UUID deviceId;
    private UUID sessionId;
    private AlarmType type;
    private String severity;
    private String message;
    private Instant occurredAt;
}
