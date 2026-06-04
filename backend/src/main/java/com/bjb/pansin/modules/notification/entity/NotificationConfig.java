package com.bjb.pansin.modules.notification.entity;

import com.bjb.pansin.common.entity.BaseEntity;
import com.bjb.pansin.common.enums.NotificationChannel;
import com.bjb.pansin.modules.branch.entity.Branch;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "notification_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationConfig extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> recipients;

    @Column(columnDefinition = "TEXT")
    private String template;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
