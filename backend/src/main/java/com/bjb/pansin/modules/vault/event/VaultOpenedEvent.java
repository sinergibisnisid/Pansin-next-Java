package com.bjb.pansin.modules.vault.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultOpenedEvent {
    private UUID vaultId;
    private UUID sessionId;
    private UUID userId;
    private String openMethod;
    private Instant occurredAt;
}
