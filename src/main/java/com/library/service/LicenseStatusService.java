package com.library.service;

import com.library.entity.License;
import com.library.entity.LicenseStatus;

public interface LicenseStatusService {
    LicenseStatus calculateStatus(License license);
    LicenseStatus calculateStatus(License license, int warningDays);
    void refreshAllLicenseStatuses();
}
