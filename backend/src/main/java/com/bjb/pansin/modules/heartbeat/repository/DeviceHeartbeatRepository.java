package com.bjb.pansin.modules.heartbeat.repository;

import com.bjb.pansin.modules.heartbeat.entity.DeviceHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceHeartbeatRepository extends JpaRepository<DeviceHeartbeat, UUID> {
}
