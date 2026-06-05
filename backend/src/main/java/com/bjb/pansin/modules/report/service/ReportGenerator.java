package com.bjb.pansin.modules.report.service;

import com.bjb.pansin.modules.vault.entity.VaultAccessLog;
import com.bjb.pansin.modules.vault.repository.VaultAccessLogRepository;
import com.bjb.pansin.modules.activity.entity.ActivityLog;
import com.bjb.pansin.modules.activity.repository.ActivityLogRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerator {

    private final VaultAccessLogRepository accessLogRepository;
    private final ActivityLogRepository activityLogRepository;

    public byte[] generateAccessLogPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("PANSIN ACCESS - Vault Access Log Report"));
            document.add(new Paragraph(" "));

            for (VaultAccessLog log : accessLogRepository.findAll()) {
                document.add(new Paragraph("[%s] %s vault=%s user=%s method=%s".formatted(
                        DateTimeFormatter.ISO_INSTANT.format(log.getCreatedAt()),
                        log.getAction(),
                        log.getVault().getCode(),
                        log.getUser() != null ? log.getUser().getUsername() : "-",
                        log.getMethod())));
            }
        }
        return out.toByteArray();
    }

    public byte[] generateAccessLogExcel() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Access Logs");
            Row header = sheet.createRow(0);
            String[] cols = {"Timestamp", "Vault", "User", "Action", "Method", "Source IP"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int idx = 1;
            for (VaultAccessLog log : accessLogRepository.findAll()) {
                Row row = sheet.createRow(idx++);
                row.createCell(0).setCellValue(log.getCreatedAt().toString());
                row.createCell(1).setCellValue(log.getVault().getCode());
                row.createCell(2).setCellValue(log.getUser() != null ? log.getUser().getUsername() : "-");
                row.createCell(3).setCellValue(log.getAction());
                row.createCell(4).setCellValue(log.getMethod() != null ? log.getMethod() : "-");
                row.createCell(5).setCellValue(log.getSourceIp() != null ? log.getSourceIp() : "-");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Excel generation failed", ex);
            throw new RuntimeException(ex);
        }
    }

    public byte[] generateAccessLogCsv() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write("Timestamp,Vault,User,Action,Method,SourceIP\n");
            for (VaultAccessLog log : accessLogRepository.findAll()) {
                writer.write("%s,%s,%s,%s,%s,%s%n".formatted(
                        log.getCreatedAt(),
                        log.getVault().getCode(),
                        log.getUser() != null ? log.getUser().getUsername() : "-",
                        log.getAction(),
                        log.getMethod() != null ? log.getMethod() : "-",
                        log.getSourceIp() != null ? log.getSourceIp() : "-"));
            }
        } catch (Exception ex) {
            log.error("CSV generation failed", ex);
            throw new RuntimeException(ex);
        }
        return out.toByteArray();
    }

    public byte[] generateAuditLogPdf(String category, String severity) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Header
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("PANSIN ACCESS SYSTEM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 102, 204));
            Paragraph subtitle = new Paragraph("Audit Log Report", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            
            document.add(new Paragraph(" "));
            
            // Metadata
            Font metaFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")), metaFont));
            if (category != null && !category.equals("all")) {
                document.add(new Paragraph("Filter Category: " + category, metaFont));
            }
            var logs = activityLogRepository.findAll();
            long count = logs.stream().filter(l -> category == null || category.equals("all") || category.equals(l.getActivity())).count();
            document.add(new Paragraph("Total Records: " + count, metaFont));
            document.add(new Paragraph(" "));
            
            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 3f, 1.5f});
            
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Color headerBg = new Color(0, 51, 102);
            String[] headers = {"Timestamp", "User", "Action", "Details", "IP Address"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
            for (ActivityLog log : logs) {
                if (category != null && !category.equals("all") && !category.equals(log.getActivity())) continue;
                table.addCell(new Phrase(log.getCreatedAt() != null ? log.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "-", cellFont));
                table.addCell(new Phrase(log.getUser() != null ? log.getUser().getFullName() : "System", cellFont));
                table.addCell(new Phrase(log.getActivity(), cellFont));
                table.addCell(new Phrase(log.getDescription() != null ? log.getDescription().substring(0, Math.min(50, log.getDescription().length())) : "-", cellFont));
                table.addCell(new Phrase(log.getIpAddress() != null ? log.getIpAddress() : "-", cellFont));
            }
            
            document.add(table);
        } catch (Exception ex) {
            log.error("PDF generation failed", ex);
            throw new RuntimeException(ex);
        }
        return out.toByteArray();
    }

    public byte[] generateAuditLogExcel(String category, String severity) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Audit Log");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Timestamp");
            header.createCell(1).setCellValue("User");
            header.createCell(2).setCellValue("Action");
            header.createCell(3).setCellValue("Category");
            header.createCell(4).setCellValue("Details");
            header.createCell(5).setCellValue("Severity");
            header.createCell(6).setCellValue("IP Address");
            
            var logs = activityLogRepository.findAll();
            int rowNum = 1;
            for (ActivityLog log : logs) {
                if (category != null && !category.equals("all") && !category.equals(log.getActivity())) continue;
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "-");
                row.createCell(1).setCellValue(log.getUser() != null ? log.getUser().getFullName() : "-");
                row.createCell(2).setCellValue(log.getActivity());
                row.createCell(3).setCellValue(log.getActivity());
                row.createCell(4).setCellValue(log.getDescription() != null ? log.getDescription() : "-");
                row.createCell(5).setCellValue("info");
                row.createCell(6).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "-");
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Excel generation failed", ex);
            throw new RuntimeException(ex);
        }
    }

    public byte[] generateAuditLogCsv(String category, String severity) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            writer.write("Timestamp,User,Action,Category,Details,Severity,IP Address\n");
            var logs = activityLogRepository.findAll();
            for (ActivityLog log : logs) {
                if (category != null && !category.equals("all") && !category.equals(log.getActivity())) continue;
                writer.write("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n".formatted(
                        log.getCreatedAt(),
                        log.getUser() != null ? log.getUser().getFullName() : "-",
                        log.getActivity(),
                        log.getActivity(),
                        log.getDescription() != null ? log.getDescription().replace("\"", "\"\"") : "-",
                        "info",
                        log.getIpAddress() != null ? log.getIpAddress() : "-"));
            }
        } catch (Exception ex) {
            log.error("CSV generation failed", ex);
            throw new RuntimeException(ex);
        }
        return out.toByteArray();
    }
}
