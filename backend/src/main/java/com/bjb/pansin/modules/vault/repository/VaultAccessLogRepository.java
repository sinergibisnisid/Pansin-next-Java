package com.bjb.pansin.modules.vault.repository;

import com.bjb.pansin.modules.vault.entity.VaultAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VaultAccessLogRepository extends JpaRepository<VaultAccessLog, UUID> {
    Page<VaultAccessLog> findByVaultId(UUID vaultId, Pageable pageable);
    Page<VaultAccessLog> findByUserId(UUID userId, Pageable pageable);
}
