package com.bjb.pansin.modules.fingerprint.entity;

import com.bjb.pansin.common.entity.BaseEntity;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.vault.entity.Vault;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fingerprint_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FingerprintDevice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id")
    private Vault vault;

    @Column(name = "serial_number", nullable = false, unique = true, length = 120)
    private String serialNumber;

    @Column(length = 80)
    private String model;

    private Integer capacity;

    @Column(name = "enrolled_count", nullable = false)
    @Builder.Default
    private int enrolledCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
