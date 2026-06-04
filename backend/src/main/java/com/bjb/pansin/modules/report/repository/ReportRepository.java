package com.bjb.pansin.modules.report.repository;

import com.bjb.pansin.modules.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
}
