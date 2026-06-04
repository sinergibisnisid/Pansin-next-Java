package com.bjb.pansin.modules.report.service;

import com.bjb.pansin.modules.vault.entity.VaultAccessLog;
import com.bjb.pansin.modules.vault.repository.VaultAccessLogRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
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
}
