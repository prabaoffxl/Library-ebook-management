package com.library.service.impl;

import com.library.dto.report.*;
import com.library.service.ReportExportService;
import com.library.service.ReportService;
import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportExportServiceImpl implements ReportExportService {

    private final ReportService reportService;

    public ReportExportServiceImpl(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public ByteArrayInputStream exportBooksCsv(String userEmail) {
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Metric/Category,Count");
        writer.println("Total Books," + dto.getTotalBooks());
        writer.println("Available Books," + dto.getAvailableBooks());
        writer.println("Unavailable Books," + dto.getUnavailableBooks());

        writer.println("\n--- Books By Category ---");
        writer.println("Category,Count");
        for (LabelValueDTO lv : dto.getBooksByCategory()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.println("\n--- Books By Language ---");
        writer.println("Language,Count");
        for (LabelValueDTO lv : dto.getBooksByLanguage()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportLicensesCsv(String userEmail) {
        List<LicenseExpiryReportItemDTO> list = reportService.getLicenseExpiryReport(userEmail, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("License ID,License Key,Book Title,Provider,License Type,Start Date,End Date,Status,Days Remaining,Max Users,Current Users,Price");
        for (LicenseExpiryReportItemDTO item : list) {
            writer.println(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d,%d,%d,%s",
                    item.getId(),
                    escapeCsv(item.getLicenseKey()),
                    escapeCsv(item.getBookTitle()),
                    escapeCsv(item.getProvider()),
                    item.getLicenseType(),
                    item.getStartDate(),
                    item.getEndDate(),
                    item.getStatus(),
                    item.getDaysRemaining(),
                    item.getMaxUsers(),
                    item.getCurrentUsers(),
                    item.getPrice() != null ? item.getPrice().toString() : "0.00"
            ));
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportAssetsCsv(String userEmail) {
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Metric,Value");
        writer.println("Total Assets," + dto.getTotalAssets());
        writer.println("Active Assets," + dto.getActiveAssets());
        writer.println("Archived Assets," + dto.getArchivedAssets());
        writer.println("Replaced Assets," + dto.getReplacedAssets());
        writer.println("Deleted Assets," + dto.getDeletedAssets());
        writer.println("E-Book Assets," + dto.getEbookAssets());
        writer.println("Cover Image Assets," + dto.getCoverAssets());
        writer.println("Preview Assets," + dto.getPreviewAssets());
        writer.println("Total Storage," + escapeCsv(dto.getTotalStorageFormatted()));

        writer.println("\n--- Storage By Asset Type ---");
        writer.println("Asset Type,Bytes");
        for (LabelValueDTO lv : dto.getStorageByAssetType()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportLibrariesCsv(String userEmail) {
        List<LibraryReportDTO> list = reportService.getLibraryAnalytics(userEmail);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Library ID,Name,Code,Total Books,Available Books,Unavailable Books,Assigned Librarians,Active Licenses,Expiring Licenses,Digital Assets,Storage");
        for (LibraryReportDTO item : list) {
            writer.println(String.format("%d,%s,%s,%d,%d,%d,%d,%d,%d,%d,%s",
                    item.getLibraryId(),
                    escapeCsv(item.getLibraryName()),
                    escapeCsv(item.getLibraryCode()),
                    item.getTotalBooks(),
                    item.getAvailableBooks(),
                    item.getUnavailableBooks(),
                    item.getAssignedLibrarians(),
                    item.getActiveLicenses(),
                    item.getExpiringLicenses(),
                    item.getDigitalAssets(),
                    escapeCsv(item.getStorageFormatted())
            ));
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportUsersCsv(String userEmail) {
        UserReportDTO dto = reportService.getUserAnalytics(userEmail, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Metric,Value");
        writer.println("Total Users," + dto.getTotalUsers());
        writer.println("Active Users," + dto.getActiveUsers());
        writer.println("Inactive Users," + dto.getInactiveUsers());
        writer.println("Suspended Users," + dto.getSuspendedUsers());

        writer.println("\n--- Users By Role ---");
        writer.println("Role,Count");
        for (LabelValueDTO lv : dto.getUsersByRole()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportActivityCsv(String userEmail) {
        ActivityReportDTO dto = reportService.getActivityAnalytics(userEmail, null, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Activity Type,Count");
        for (LabelValueDTO lv : dto.getActivityByType()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.println("\n--- Daily Activity ---");
        writer.println("Date,Count");
        for (LabelValueDTO lv : dto.getActivityByDay()) {
            writer.println(escapeCsv(lv.getLabel()) + "," + lv.getValue());
        }

        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream exportSummaryPdf(String userEmail, LocalDate startDate, LocalDate endDate) {
        ReportSummaryDTO summary = reportService.getOverallSummary(userEmail, startDate, endDate);
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Paragraph title = new Paragraph("Municipal Library System - Executive Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            String dateRangeStr = (startDate != null && endDate != null) ?
                    "Period: " + startDate + " to " + endDate : "Period: Lifetime Summary";
            Paragraph subtitle = new Paragraph("Generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " | " + dateRangeStr, subFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Summary KPIs Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addTableHeader(table, "Metric", "Value");
            addTableRow(table, "Total Registered Users", String.valueOf(summary.getTotalUsers()));
            addTableRow(table, "Active Users", String.valueOf(summary.getActiveUsers()));
            addTableRow(table, "Total Municipal Libraries", String.valueOf(summary.getTotalLibraries()));
            addTableRow(table, "Total E-Books in Collection", String.valueOf(summary.getTotalBooks()));
            addTableRow(table, "Available E-Books", String.valueOf(summary.getAvailableBooks()));
            addTableRow(table, "Unavailable E-Books", String.valueOf(summary.getUnavailableBooks()));
            addTableRow(table, "Total Digital Assets", String.valueOf(summary.getTotalDigitalAssets()));
            addTableRow(table, "Total Storage Consumption", summary.getTotalStorageFormatted());
            addTableRow(table, "Active Licenses", String.valueOf(summary.getActiveLicenses()));
            addTableRow(table, "Licenses Expiring Soon", String.valueOf(summary.getExpiringLicenses()));
            addTableRow(table, "Expired Licenses", String.valueOf(summary.getExpiredLicenses()));
            addTableRow(table, "Total License Investment", "INR " + summary.getTotalLicenseCost().toPlainString());

            document.add(table);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF summary report", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableHeader(PdfPTable table, String col1, String col2) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        PdfPCell cell1 = new PdfPCell(new Phrase(col1, headerFont));
        cell1.setBackgroundColor(new Color(30, 144, 255));
        cell1.setPadding(8);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(col2, headerFont));
        cell2.setBackgroundColor(new Color(30, 144, 255));
        cell2.setPadding(8);
        table.addCell(cell2);
    }

    private void addTableRow(PdfPTable table, String col1, String col2) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        PdfPCell cell1 = new PdfPCell(new Phrase(col1, font));
        cell1.setPadding(6);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase(col2, font));
        cell2.setPadding(6);
        table.addCell(cell2);
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
