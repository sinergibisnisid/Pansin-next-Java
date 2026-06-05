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
        try {
            Document document = new Document(PageSize.A4.rotate()); // Landscape
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Professional header
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(0, 51, 102));
            Paragraph title = new Paragraph("PANSIN ACCESS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            Font subtitleFont = new Font(Font.HELVETICA, 14, Font.NORMAL, new Color(0, 102, 204));
            Paragraph subtitle = new Paragraph("Audit Log Report", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(10f);
            document.add(subtitle);
            
            // Metadata section
            Font metaFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
            document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")), metaFont));
            document.add(new Paragraph("Filter Category: " + (category != null && !category.equals("all") ? category : "All"), metaFont));
            document.add(new Paragraph("Filter Severity: " + (severity != null && !severity.equals("all") ? severity : "All"), metaFont));
            
            var logs = activityLogRepository.findAll();
            long totalRecords = logs.stream().filter(l -> category == null || category.equals("all") || category.equals(l.getActivity())).count();
            document.add(new Paragraph("Total Records: " + totalRecords, metaFont));
            document.add(new Paragraph(" "));
            
            // Table header
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            for (String header : new String[]{"Timestamp", "User", "Action", "Details", "IP Address"}) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(0, 51, 102));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            
            // Table rows
            Font cellFont = new Font(Font.HELVETICA, 8);
            int rowIndex = 0;
            for (ActivityLog log : logs) {
                if (category != null && !category.equals("all") && !category.equals(log.getActivity())) continue;
                
                Color rowBg = (rowIndex++ % 2 == 0) ? Color.WHITE : new Color(245, 245, 245);
                
                PdfPCell[] cells = {
                    new PdfPCell(new Phrase(log.getCreatedAt() != null ? log.getCreatedAt().toString().substring(0, 16) : "-", cellFont)),
                    new PdfPCell(new Phrase(log.getUser() != null ? log.getUser().getFullName() : "System", cellFont)),
                    new PdfPCell(new Phrase(log.getActivity(), cellFont)),
                    new PdfPCell(new Phrase(log.getDescription() != null ? log.getDescription().substring(0, Math.min(40, log.getDescription().length())) : "-", cellFont)),
                    new PdfPCell(new Phrase(log.getIpAddress() != null ? log.getIpAddress() : "-", cellFont))
                };
                
                for (PdfPCell cell : cells) {
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(4);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("PDF generation failed", ex);
            throw new RuntimeException("PDF generation failed: " + ex.getMessage(), ex);
        }
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
