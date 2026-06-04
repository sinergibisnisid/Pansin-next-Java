package com.bjb.pansin.modules.monitoring.entity;

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
@Table(name = "server_monitorings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerMonitoring {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 120)
    private String hostname;

    @Column(name = "cpu_load", precision = 5, scale = 2)
    private BigDecimal cpuLoad;

    @Column(name = "memory_load", precision = 5, scale = 2)
    private BigDecimal memoryLoad;

    @Column(name = "disk_load", precision = 5, scale = 2)
    private BigDecimal diskLoad;

    @Column(name = "mqtt_connected")
    private Boolean mqttConnected;

    @Column(name = "websocket_count")
    private Integer websocketCount;

    @Column(name = "queue_size")
    private Integer queueSize;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
