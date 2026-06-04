package com.bjb.pansin.modules.vault.repository;

import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.modules.vault.entity.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaultRepository extends JpaRepository<Vault, UUID> {
    Optional<Vault> findByCode(String code);
    List<Vault> findByBranchId(UUID branchId);
    List<Vault> findByStatus(VaultStatus status);
    boolean existsByCode(String code);
}
