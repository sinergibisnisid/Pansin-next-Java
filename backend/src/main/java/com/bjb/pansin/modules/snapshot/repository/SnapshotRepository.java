package com.bjb.pansin.modules.snapshot.repository;

import com.bjb.pansin.modules.snapshot.entity.Snapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {
    Page<Snapshot> findByVaultId(UUID vaultId, Pageable pageable);
}
