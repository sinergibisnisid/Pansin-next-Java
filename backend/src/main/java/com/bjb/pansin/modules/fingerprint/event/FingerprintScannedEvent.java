package com.bjb.pansin.modules.fingerprint.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintScannedEvent {
    private UUID deviceId;
    private UUID userId;
    private String templateId;
    private boolean matched;
    private Instant occurredAt;
}
