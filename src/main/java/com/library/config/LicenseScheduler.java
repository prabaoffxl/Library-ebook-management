package com.library.config;

import com.library.service.LicenseStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LicenseScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LicenseScheduler.class);
    private final LicenseStatusService licenseStatusService;

    public LicenseScheduler(LicenseStatusService licenseStatusService) {
        this.licenseStatusService = licenseStatusService;
    }

    // Run automatically on application startup
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        logger.info("Initializing automated license status check on application startup...");
        licenseStatusService.refreshAllLicenseStatuses();
    }

    // Run scheduled job daily at 01:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkLicenseStatuses() {
        logger.info("Running scheduled daily e-book license status update...");
        licenseStatusService.refreshAllLicenseStatuses();
        logger.info("Scheduled license status update completed.");
    }
}
