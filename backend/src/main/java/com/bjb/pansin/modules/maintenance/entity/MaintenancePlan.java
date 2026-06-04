package com.bjb.pansin.modules.maintenance.entity;

import com.bjb.pansin.common.entity.BaseEntity;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.vault.entity.Vault;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "maintenance_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id")
    private Vault vault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "next_due_at")
    private Instant nextDueAt;

    @Column(name = "last_done_at")
    private Instant lastDoneAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
