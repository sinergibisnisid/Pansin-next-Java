package com.bjb.pansin.modules.fingerprint.repository;

import com.bjb.pansin.modules.fingerprint.entity.FingerprintDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FingerprintDeviceRepository extends JpaRepository<FingerprintDevice, UUID> {
    Optional<FingerprintDevice> findBySerialNumber(String serialNumber);
}
