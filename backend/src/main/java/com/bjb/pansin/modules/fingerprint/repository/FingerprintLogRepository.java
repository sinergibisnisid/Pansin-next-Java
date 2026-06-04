package com.bjb.pansin.modules.fingerprint.repository;

import com.bjb.pansin.modules.fingerprint.entity.FingerprintLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FingerprintLogRepository extends JpaRepository<FingerprintLog, UUID> {
}
