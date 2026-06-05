package com.bjb.pansin.modules.report.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.report.dto.ReportRequest;
import com.bjb.pansin.modules.report.entity.Report;
import com.bjb.pansin.modules.report.repository.ReportRepository;
import com.bjb.pansin.modules.report.service.ReportGenerator;
import com.bjb.pansin.modules.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Tag(name = "Reports")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenerator generator;
    private final ReportService reportService;
    private final ReportRepository reportRepository;

    // Direct (sync) export, kept for compatibility
    @GetMapping("/access-log")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<byte[]> exportAccessLog(@RequestParam(defaultValue = "pdf") String format) {
        byte[] body;
        MediaType mt;
        String filename;
        switch (format.toLowerCase()) {
            case "excel" -> {
                body = generator.generateAccessLogExcel();
                mt = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                filename = "access-log.xlsx";
            }
            case "csv" -> {
                body = generator.generateAccessLogCsv();
                mt = MediaType.parseMediaType("text/csv");
                filename = "access-log.csv";
            }
            default -> {
                body = generator.generateAccessLogPdf();
                mt = MediaType.APPLICATION_PDF;
                filename = "access-log.pdf";
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mt)
                .body(body);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) String category, @RequestParam(required = false) String severity) {
        byte[] body = generator.generateAuditLogCsv(category, severity);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) String category, @RequestParam(required = false) String severity) {
        byte[] body = generator.generateAuditLogExcel(category, severity);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(body);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) String category, @RequestParam(required = false) String severity) {
        byte[] body = generator.generateAuditLogPdf(category, severity);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }

    // Async report job
    @PostMapping
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<ApiResponse<Report>> request(@Valid @RequestBody ReportRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Report queued", reportService.request(req)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<ApiResponse<List<Report>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(reportRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<ApiResponse<Report>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.get(id)));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Report report = reportService.get(id);
        if (!"COMPLETED".equals(report.getStatus()) || report.getFilePath() == null) {
            return ResponseEntity.status(409).build();
        }
        Path path = Path.of(report.getFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        MediaType mt = switch (report.getFormat()) {
            case "PDF"   -> MediaType.APPLICATION_PDF;
            case "EXCEL" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "CSV"   -> MediaType.parseMediaType("text/csv");
            default      -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName() + "\"")
                .contentType(mt)
                .body(new FileSystemResource(path));
    }
}
