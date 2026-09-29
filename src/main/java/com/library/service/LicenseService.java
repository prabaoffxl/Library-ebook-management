package com.library.service;

import com.library.dto.*;
import java.util.List;

public interface LicenseService {
    LicenseResponse createLicense(LicenseRequest request);
    LicenseResponse getLicenseById(Long id);
    List<LicenseResponse> getAllLicenses();
    List<LicenseResponse> searchLicenses(String query, String provider, String status, String licenseType, String bookTitle);
    List<LicenseResponse> filterLicenses(String status);
    List<LicenseResponse> getLicenseHistory(Long bookId);
    LicenseResponse updateLicense(Long id, LicenseRequest request);
    LicenseResponse renewLicense(Long id, LicenseRenewRequest request);
    LicenseResponse suspendLicense(Long id, LicenseSuspendRequest request);
    LicenseResponse cancelLicense(Long id);
    void deleteLicense(Long id);
    void validateBookDownloadLicense(Long bookId);
}
