package com.bjb.pansin.modules.vault.repository;

import com.bjb.pansin.common.enums.SessionStatus;
import com.bjb.pansin.modules.vault.entity.VaultSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaultSessionRepository extends JpaRepository<VaultSession, UUID> {
    Optional<VaultSession> findFirstByVaultIdAndStatusOrderByOpenedAtDesc(UUID vaultId, SessionStatus status);
    List<VaultSession> findByStatus(SessionStatus status);
}
