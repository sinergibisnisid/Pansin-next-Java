package com.bjb.pansin.modules.vault.entity;

import com.bjb.pansin.common.entity.BaseEntity;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.modules.branch.entity.Branch;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "vaults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vault extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VaultStatus status = VaultStatus.CLOSED;

    @Column(name = "last_opened_at")
    private Instant lastOpenedAt;

    @Column(name = "last_closed_at")
    private Instant lastClosedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
