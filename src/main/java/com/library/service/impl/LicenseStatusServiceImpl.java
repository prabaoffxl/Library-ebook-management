package com.library.service.impl;

import com.library.entity.License;
import com.library.entity.LicenseStatus;
import com.library.entity.NotificationPriority;
import com.library.entity.NotificationType;
import com.library.repository.LicenseRepository;
import com.library.service.LicenseStatusService;
import com.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LicenseStatusServiceImpl implements LicenseStatusService {

    private static final Logger logger = LoggerFactory.getLogger(LicenseStatusServiceImpl.class);

    private final LicenseRepository licenseRepository;
    private final NotificationService notificationService;

    @Value("${license.expiry.warning-days:30}")
    private int warningDays;

    @Value("${license.expiry.second-warning-days:7}")
    private int secondWarningDays;

    public LicenseStatusServiceImpl(LicenseRepository licenseRepository, NotificationService notificationService) {
        this.licenseRepository = licenseRepository;
        this.notificationService = notificationService;
    }

    @Override
    public LicenseStatus calculateStatus(License license) {
        return calculateStatus(license, this.warningDays);
    }

    @Override
    public LicenseStatus calculateStatus(License license, int customWarningDays) {
        if (license == null) {
            return LicenseStatus.EXPIRED;
        }

        // Manual override statuses that should not be auto-overwritten
        if (license.getStatus() == LicenseStatus.SUSPENDED ||
            license.getStatus() == LicenseStatus.CANCELLED ||
            license.getStatus() == LicenseStatus.RENEWED) {
            return license.getStatus();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = license.getStartDate();
        LocalDate endDate = license.getEndDate();

        if (startDate != null && today.isBefore(startDate)) {
            return LicenseStatus.PENDING;
        }

        if (endDate != null && today.isAfter(endDate)) {
            return LicenseStatus.EXPIRED;
        }

        if (endDate != null && !today.isAfter(endDate) && !today.isBefore(startDate)) {
            LocalDate warningThreshold = endDate.minusDays(customWarningDays);
            if (!today.isBefore(warningThreshold)) {
                return LicenseStatus.EXPIRING_SOON;
            }
            return LicenseStatus.ACTIVE;
        }

        return LicenseStatus.ACTIVE;
    }

    @Override
    @Transactional
    public void refreshAllLicenseStatuses() {
        List<License> licenses = licenseRepository.findAll();
        LocalDate today = LocalDate.now();

        for (License license : licenses) {
            try {
                LicenseStatus calculated = calculateStatus(license);
                if (license.getStatus() != calculated) {
                    license.setStatus(calculated);
                    licenseRepository.save(license);
                }

                // Check for notifications if active/expiring/expired
                if (license.getStatus() == LicenseStatus.SUSPENDED || 
                    license.getStatus() == LicenseStatus.CANCELLED || 
                    license.getStatus() == LicenseStatus.RENEWED) {
                    continue;
                }

                LocalDate endDate = license.getEndDate();
                if (endDate == null) {
                    continue;
                }

                long daysLeft = ChronoUnit.DAYS.between(today, endDate);
                String bookTitle = license.getBook() != null ? license.getBook().getTitle() : "Unknown Book";
                Long bookId = license.getBook() != null ? license.getBook().getId() : null;

                if (daysLeft < 0 || calculated == LicenseStatus.EXPIRED) {
                    // Expired notification
                    String keyPrefix = "LICENSE_EXPIRED_" + license.getId();
                    notificationService.notifyAdminsAndAssignedLibrarians(
                            bookId,
                            "License Expired: " + bookTitle,
                            "License " + license.getLicenseKey() + " for '" + bookTitle + "' has expired on " + endDate + ".",
                            NotificationType.LICENSE_EXPIRED,
                            NotificationPriority.CRITICAL,
                            license.getId(),
                            keyPrefix
                    );
                } else if (daysLeft <= secondWarningDays) {
                    // 7-day warning (CRITICAL)
                    String keyPrefix = "LICENSE_EXPIRING_7_" + license.getId();
                    notificationService.notifyAdminsAndAssignedLibrarians(
                            bookId,
                            "CRITICAL: License Expiring in " + daysLeft + " Days",
                            "License " + license.getLicenseKey() + " for '" + bookTitle + "' will expire on " + endDate + " (" + daysLeft + " days remaining). Please renew immediately.",
                            NotificationType.LICENSE_EXPIRING,
                            NotificationPriority.CRITICAL,
                            license.getId(),
                            keyPrefix
                    );
                } else if (daysLeft <= warningDays) {
                    // 30-day warning (HIGH)
                    String keyPrefix = "LICENSE_EXPIRING_30_" + license.getId();
                    notificationService.notifyAdminsAndAssignedLibrarians(
                            bookId,
                            "License Expiring Soon: " + bookTitle,
                            "License " + license.getLicenseKey() + " for '" + bookTitle + "' will expire on " + endDate + " (" + daysLeft + " days remaining).",
                            NotificationType.LICENSE_EXPIRING,
                            NotificationPriority.HIGH,
                            license.getId(),
                            keyPrefix
                    );
                }
            } catch (Exception ex) {
                logger.error("Error refreshing status or sending expiry notification for license ID {}", license.getId(), ex);
            }
        }
    }
}
