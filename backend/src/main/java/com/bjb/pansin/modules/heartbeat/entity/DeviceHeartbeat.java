package com.bjb.pansin.modules.heartbeat.entity;

import com.bjb.pansin.modules.device.entity.Device;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "device_heartbeats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceHeartbeat {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "cpu_load", precision = 5, scale = 2)
    private BigDecimal cpuLoad;

    @Column(name = "memory_load", precision = 5, scale = 2)
    private BigDecimal memoryLoad;

    @Column(name = "signal_quality")
    private Integer signalQuality;

    @Column(name = "uptime_seconds")
    private Long uptimeSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
