package com.bjb.pansin.modules.report.service;

import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.report.dto.ReportRequest;
import com.bjb.pansin.modules.report.entity.Report;
import com.bjb.pansin.modules.report.repository.ReportRepository;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReportGenerator generator;

    @Value("${app.storage.report-path:./storage/reports}")
    private String reportPath;

    @Transactional
    public Report request(ReportRequest req) {
        Map<String, Object> params = new HashMap<>();
        if (req.getFrom() != null) params.put("from", req.getFrom().toString());
        if (req.getTo() != null) params.put("to", req.getTo().toString());
        if (req.getBranchId() != null) params.put("branchId", req.getBranchId().toString());
        if (req.getVaultId() != null) params.put("vaultId", req.getVaultId().toString());

        Report report = Report.builder()
                .name(req.getName())
                .type(req.getType().name())
                .format(req.getFormat().name())
                .parameters(params)
                .status("PENDING")
                .build();

        SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).ifPresent(report::setRequestedBy);
        report = reportRepository.save(report);

        generateAsync(report.getId(), req);
        return report;
    }

    public Report get(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", id));
    }

    @Async("taskExecutor")
    public void generateAsync(UUID reportId, ReportRequest req) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) return;

        try {
            byte[] body = switch (req.getType()) {
                case ACCESS_LOG -> switch (req.getFormat()) {
                    case PDF   -> generator.generateAccessLogPdf();
                    case EXCEL -> generator.generateAccessLogExcel();
                    case CSV   -> generator.generateAccessLogCsv();
                };
                default -> throw new UnsupportedOperationException(
                        "Report type not yet supported: " + req.getType());
            };

            String ext = switch (req.getFormat()) {
                case PDF -> "pdf";
                case EXCEL -> "xlsx";
                case CSV -> "csv";
            };
            Path dir = Paths.get(reportPath);
            Files.createDirectories(dir);
            Path file = dir.resolve("%s_%s.%s".formatted(
                    req.getType().name().toLowerCase(),
                    reportId, ext));
            Files.write(file, body);

            report.setFilePath(file.toString());
            report.setFileSize((long) body.length);
            report.setStatus("COMPLETED");
            report.setGeneratedAt(Instant.now());
        } catch (Exception ex) {
            log.error("Report generation failed for {}", reportId, ex);
            report.setStatus("FAILED");
            report.setErrorMessage(ex.getMessage());
        }
        reportRepository.save(report);
    }
}
