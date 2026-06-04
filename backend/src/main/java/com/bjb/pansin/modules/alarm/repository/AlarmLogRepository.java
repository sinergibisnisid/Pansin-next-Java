package com.bjb.pansin.modules.alarm.repository;

import com.bjb.pansin.modules.alarm.entity.AlarmLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, UUID> {
    Page<AlarmLog> findByAcknowledgedFalse(Pageable pageable);
    Page<AlarmLog> findByVaultId(UUID vaultId, Pageable pageable);
}
