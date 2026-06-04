package com.bjb.pansin.modules.organization.repository;

import com.bjb.pansin.modules.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);
}
