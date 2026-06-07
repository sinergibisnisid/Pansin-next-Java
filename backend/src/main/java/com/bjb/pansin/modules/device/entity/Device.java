package com.bjb.pansin.modules.device.entity;

import com.bjb.pansin.common.entity.BaseEntity;
import com.bjb.pansin.common.enums.DeviceStatus;
import com.bjb.pansin.modules.branch.entity.Branch;
import com.bjb.pansin.modules.vault.entity.Vault;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_id")
    private Vault vault;

    @Column(name = "device_code", nullable = false, unique = true, length = 80)
    private String deviceCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "mac_address", length = 64)
    private String macAddress;

    @Column(name = "firmware_version", length = 40)
    private String firmwareVersion;

    @Column(name = "signal_quality")
    private Integer signalQuality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "lifecycle_state", nullable = false, length = 40)
    @Builder.Default
    private String lifecycleState = "MANUFACTURED";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
