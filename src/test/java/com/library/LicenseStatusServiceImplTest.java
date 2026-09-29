package com.library;

import com.library.entity.*;
import com.library.repository.LicenseRepository;
import com.library.service.NotificationService;
import com.library.service.impl.LicenseStatusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LicenseStatusServiceImplTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LicenseStatusServiceImpl licenseStatusService;

    private Book testBook;
    private License testLicense;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(licenseStatusService, "warningDays", 30);
        ReflectionTestUtils.setField(licenseStatusService, "secondWarningDays", 7);

        testBook = new Book();
        testBook.setId(100L);
        testBook.setTitle("Sample E-Book");

        testLicense = new License();
        testLicense.setId(50L);
        testLicense.setBook(testBook);
        testLicense.setLicenseKey("LIC-TEST-123");
        testLicense.setStartDate(LocalDate.now().minusMonths(1));
        testLicense.setEndDate(LocalDate.now().plusDays(5)); // 5 days left -> 7-day warning
        testLicense.setStatus(LicenseStatus.ACTIVE);
    }

    @Test
    void testCalculateStatus_ExpiringSoon() {
        LicenseStatus status = licenseStatusService.calculateStatus(testLicense);
        assertEquals(LicenseStatus.EXPIRING_SOON, status);
    }

    @Test
    void testCalculateStatus_Expired() {
        testLicense.setEndDate(LocalDate.now().minusDays(1));
        LicenseStatus status = licenseStatusService.calculateStatus(testLicense);
        assertEquals(LicenseStatus.EXPIRED, status);
    }

    @Test
    void testRefreshAllLicenseStatuses_EmitsSecondWarning() {
        when(licenseRepository.findAll()).thenReturn(List.of(testLicense));

        licenseStatusService.refreshAllLicenseStatuses();

        verify(notificationService, times(1)).notifyAdminsAndAssignedLibrarians(
                eq(100L),
                contains("CRITICAL: License Expiring"),
                contains("LIC-TEST-123"),
                eq(NotificationType.LICENSE_EXPIRING),
                eq(NotificationPriority.CRITICAL),
                eq(50L),
                eq("LICENSE_EXPIRING_7_50")
        );
    }
}
