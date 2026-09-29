package com.library.service;

import com.library.dto.report.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReportSummaryDTO getOverallSummary(String userEmail, LocalDate startDate, LocalDate endDate);

    BookReportDTO getBookAnalytics(String userEmail, LocalDate startDate, LocalDate endDate);

    LicenseReportDTO getLicenseAnalytics(String userEmail, LocalDate startDate, LocalDate endDate);

    List<LicenseExpiryReportItemDTO> getLicenseExpiryReport(String userEmail, String statusFilter, Integer daysThreshold);

    AssetReportDTO getDigitalAssetAnalytics(String userEmail, LocalDate startDate, LocalDate endDate);

    List<LibraryReportDTO> getLibraryAnalytics(String userEmail);

    LibraryReportDTO getLibraryAnalyticsById(Long libraryId, String userEmail);

    UserReportDTO getUserAnalytics(String userEmail, LocalDate startDate, LocalDate endDate);

    ActivityReportDTO getActivityAnalytics(String userEmail, LocalDate startDate, LocalDate endDate);
}
