package com.bjb.pansin.modules.vault.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultClosedEvent {
    private UUID vaultId;
    private UUID sessionId;
    private UUID userId;
    private long durationSeconds;
    private boolean exceededLimit;
    private Instant occurredAt;
}
