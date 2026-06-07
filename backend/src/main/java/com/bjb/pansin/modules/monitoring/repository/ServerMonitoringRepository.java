package com.bjb.pansin.modules.monitoring.repository;

import com.bjb.pansin.modules.monitoring.entity.ServerMonitoring;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServerMonitoringRepository extends JpaRepository<ServerMonitoring, UUID> {
    Optional<ServerMonitoring> findTopByOrderByCreatedAtDesc();
    List<ServerMonitoring> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);
    List<ServerMonitoring> findByOrderByCreatedAtDesc(Pageable pageable);
}
