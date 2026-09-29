package com.library.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ReportExportService {

    ByteArrayInputStream exportBooksCsv(String userEmail);

    ByteArrayInputStream exportLicensesCsv(String userEmail);

    ByteArrayInputStream exportAssetsCsv(String userEmail);

    ByteArrayInputStream exportLibrariesCsv(String userEmail);

    ByteArrayInputStream exportUsersCsv(String userEmail);

    ByteArrayInputStream exportActivityCsv(String userEmail);

    ByteArrayInputStream exportSummaryPdf(String userEmail, LocalDate startDate, LocalDate endDate);
}
