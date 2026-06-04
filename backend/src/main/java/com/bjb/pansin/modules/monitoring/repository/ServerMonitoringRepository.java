package com.bjb.pansin.modules.monitoring.repository;

import com.bjb.pansin.modules.monitoring.entity.ServerMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServerMonitoringRepository extends JpaRepository<ServerMonitoring, UUID> {
}
