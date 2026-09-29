package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.report.*;
import com.library.service.ReportExportService;
import com.library.service.ReportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    public ReportController(ReportService reportService, ReportExportService reportExportService) {
        this.reportService = reportService;
        this.reportExportService = reportExportService;
    }

    // 1. Overall Summary
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummaryDTO>> getOverallSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ReportSummaryDTO summary = reportService.getOverallSummary(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Overall summary report retrieved successfully", summary));
    }

    // 2. Book Analytics
    @GetMapping("/books/summary")
    public ResponseEntity<ApiResponse<BookReportDTO>> getBookAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Book analytics summary retrieved successfully", dto));
    }

    @GetMapping("/books/by-category")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getBooksByCategory(Authentication authentication) {
        String userEmail = authentication.getName();
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Books by category retrieved successfully", dto.getBooksByCategory()));
    }

    @GetMapping("/books/by-language")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getBooksByLanguage(Authentication authentication) {
        String userEmail = authentication.getName();
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Books by language retrieved successfully", dto.getBooksByLanguage()));
    }

    @GetMapping("/books/by-library")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getBooksByLibrary(Authentication authentication) {
        String userEmail = authentication.getName();
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Books by library retrieved successfully", dto.getBooksByLibrary()));
    }

    @GetMapping("/books/activity")
    public ResponseEntity<ApiResponse<BookReportDTO>> getBookActivity(Authentication authentication) {
        String userEmail = authentication.getName();
        BookReportDTO dto = reportService.getBookAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Book activity retrieved successfully", dto));
    }

    // 3. License Analytics
    @GetMapping("/licenses/summary")
    public ResponseEntity<ApiResponse<LicenseReportDTO>> getLicenseAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("License analytics summary retrieved successfully", dto));
    }

    @GetMapping("/licenses/status")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicensesByStatus(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("License status breakdown retrieved successfully", dto.getExpirationTrends()));
    }

    @GetMapping("/licenses/type")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicensesByType(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Licenses by type retrieved successfully", dto.getLicensesByType()));
    }

    @GetMapping("/licenses/provider")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicensesByProvider(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Licenses by provider retrieved successfully", dto.getLicensesByProvider()));
    }

    @GetMapping("/licenses/library")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicensesByLibrary(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Licenses by library retrieved successfully", dto.getLicensesByLibrary()));
    }

    @GetMapping("/licenses/cost")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicenseCost(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("License cost summary retrieved successfully", dto.getCostByMonth()));
    }

    @GetMapping("/licenses/trends")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getLicenseTrends(Authentication authentication) {
        String userEmail = authentication.getName();
        LicenseReportDTO dto = reportService.getLicenseAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("License expiration trends retrieved successfully", dto.getExpirationTrends()));
    }

    // 4. Dedicated License Expiry Report
    @GetMapping("/licenses/expiry")
    public ResponseEntity<ApiResponse<List<LicenseExpiryReportItemDTO>>> getLicenseExpiryReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer days,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        List<LicenseExpiryReportItemDTO> list = reportService.getLicenseExpiryReport(userEmail, status, days);
        return ResponseEntity.ok(ApiResponse.success("License expiry report retrieved successfully", list));
    }

    // 5. Digital Asset Analytics
    @GetMapping("/assets/summary")
    public ResponseEntity<ApiResponse<AssetReportDTO>> getAssetAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Digital asset analytics summary retrieved successfully", dto));
    }

    @GetMapping("/assets/type")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getAssetsByType(Authentication authentication) {
        String userEmail = authentication.getName();
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Assets by type retrieved successfully", dto.getStorageByAssetType()));
    }

    @GetMapping("/assets/library")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getAssetsByLibrary(Authentication authentication) {
        String userEmail = authentication.getName();
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Assets by library retrieved successfully", dto.getAssetsByLibrary()));
    }

    @GetMapping("/assets/storage")
    public ResponseEntity<ApiResponse<AssetReportDTO>> getAssetStorage(Authentication authentication) {
        String userEmail = authentication.getName();
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Asset storage statistics retrieved successfully", dto));
    }

    @GetMapping("/assets/activity")
    public ResponseEntity<ApiResponse<AssetReportDTO>> getAssetActivity(Authentication authentication) {
        String userEmail = authentication.getName();
        AssetReportDTO dto = reportService.getDigitalAssetAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Asset activity statistics retrieved successfully", dto));
    }

    // 6. Library-Wise Reports
    @GetMapping("/libraries")
    public ResponseEntity<ApiResponse<List<LibraryReportDTO>>> getLibraryAnalytics(Authentication authentication) {
        String userEmail = authentication.getName();
        List<LibraryReportDTO> list = reportService.getLibraryAnalytics(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Library branch analytics retrieved successfully", list));
    }

    @GetMapping("/libraries/{libraryId}")
    public ResponseEntity<ApiResponse<LibraryReportDTO>> getLibraryAnalyticsById(
            @PathVariable Long libraryId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        LibraryReportDTO dto = reportService.getLibraryAnalyticsById(libraryId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Library analytics for branch retrieved successfully", dto));
    }

    // 7. User Analytics
    @GetMapping("/users/summary")
    public ResponseEntity<ApiResponse<UserReportDTO>> getUserAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UserReportDTO dto = reportService.getUserAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("User analytics summary retrieved successfully", dto));
    }

    @GetMapping("/users/roles")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getUsersByRoles(Authentication authentication) {
        String userEmail = authentication.getName();
        UserReportDTO dto = reportService.getUserAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Users by role retrieved successfully", dto.getUsersByRole()));
    }

    @GetMapping("/users/libraries")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getUsersByLibraries(Authentication authentication) {
        String userEmail = authentication.getName();
        UserReportDTO dto = reportService.getUserAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("Users by library retrieved successfully", dto.getUsersByLibrary()));
    }

    @GetMapping("/users/activity")
    public ResponseEntity<ApiResponse<List<LabelValueDTO>>> getUserActivityByType(Authentication authentication) {
        String userEmail = authentication.getName();
        UserReportDTO dto = reportService.getUserAnalytics(userEmail, null, null);
        return ResponseEntity.ok(ApiResponse.success("User activity by type retrieved successfully", dto.getActivityByType()));
    }

    // 8. Activity Analytics
    @GetMapping("/activity/summary")
    public ResponseEntity<ApiResponse<ActivityReportDTO>> getActivityAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ActivityReportDTO dto = reportService.getActivityAnalytics(userEmail, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Activity analytics summary retrieved successfully", dto));
    }

    // 9. CSV Exports
    @GetMapping("/books/export/csv")
    public ResponseEntity<InputStreamResource> exportBooksCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportBooksCsv(userEmail);
        String filename = "books-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/licenses/export/csv")
    public ResponseEntity<InputStreamResource> exportLicensesCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportLicensesCsv(userEmail);
        String filename = "licenses-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/assets/export/csv")
    public ResponseEntity<InputStreamResource> exportAssetsCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportAssetsCsv(userEmail);
        String filename = "digital-assets-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/libraries/export/csv")
    public ResponseEntity<InputStreamResource> exportLibrariesCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportLibrariesCsv(userEmail);
        String filename = "libraries-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/users/export/csv")
    public ResponseEntity<InputStreamResource> exportUsersCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportUsersCsv(userEmail);
        String filename = "users-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/activity/export/csv")
    public ResponseEntity<InputStreamResource> exportActivityCsv(Authentication authentication) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportActivityCsv(userEmail);
        String filename = "activity-report-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    // 10. PDF Export
    @GetMapping("/summary/export/pdf")
    public ResponseEntity<InputStreamResource> exportSummaryPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ByteArrayInputStream in = reportExportService.exportSummaryPdf(userEmail, startDate, endDate);
        String filename = "municipal-library-summary-report-" + LocalDate.now() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
