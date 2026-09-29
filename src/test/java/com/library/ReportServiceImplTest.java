package com.library;

import com.library.dto.report.*;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.LicenseStatusService;
import com.library.service.impl.ReportExportServiceImpl;
import com.library.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private DigitalAssetRepository digitalAssetRepository;

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private LibrarianAssignmentRepository librarianAssignmentRepository;

    @Mock
    private LicenseStatusService licenseStatusService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User adminUser;
    private User librarianUser;
    private User patronUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        librarianUser = new User();
        librarianUser.setId(2L);
        librarianUser.setEmail("librarian@test.com");
        librarianUser.setRole(Role.LIBRARIAN);

        patronUser = new User();
        patronUser.setId(3L);
        patronUser.setEmail("patron@test.com");
        patronUser.setRole(Role.USER);
    }

    @Test
    void testGetOverallSummary_AdminSuccess() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(8L);
        when(libraryRepository.count()).thenReturn(3L);
        when(bookRepository.count()).thenReturn(25L);
        when(bookRepository.countByStatus(BookStatus.AVAILABLE)).thenReturn(20L);
        when(bookRepository.countByStatus(BookStatus.UNAVAILABLE)).thenReturn(5L);
        when(digitalAssetRepository.countByStatus(AssetStatus.ACTIVE)).thenReturn(15L);
        when(digitalAssetRepository.getTotalStorageUsed()).thenReturn(104857600L); // 100 MB

        License license = new License();
        license.setId(1L);
        license.setPrice(new BigDecimal("1500.00"));
        license.setStatus(LicenseStatus.ACTIVE);

        when(licenseRepository.findAll()).thenReturn(List.of(license));
        when(licenseStatusService.calculateStatus(license)).thenReturn(LicenseStatus.ACTIVE);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(2L);

        ReportSummaryDTO summary = reportService.getOverallSummary("admin@test.com", null, null);

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalUsers());
        assertEquals(8L, summary.getActiveUsers());
        assertEquals(3L, summary.getTotalLibraries());
        assertEquals(25L, summary.getTotalBooks());
        assertEquals(1L, summary.getActiveLicenses());
        assertEquals(new BigDecimal("1500.00"), summary.getTotalLicenseCost());
        assertTrue(summary.getTotalStorageFormatted().contains("MB"));
    }

    @Test
    void testGetOverallSummary_PatronForbidden() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(patronUser));

        assertThrows(UnauthorizedAccessException.class, () -> {
            reportService.getOverallSummary("patron@test.com", null, null);
        });
    }

    @Test
    void testGetOverallSummary_InvalidDateRange() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(5);

        assertThrows(BadRequestException.class, () -> {
            reportService.getOverallSummary("admin@test.com", start, end);
        });
    }

    @Test
    void testGetLicenseExpiryReport() {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        Book book = new Book();
        book.setId(10L);
        book.setTitle("Test E-Book");

        License license = new License();
        license.setId(100L);
        license.setLicenseKey("LIC-999");
        license.setBook(book);
        license.setProvider("Oxford");
        license.setLicenseType(LicenseType.SINGLE_USER);
        license.setStartDate(LocalDate.now().minusMonths(1));
        license.setEndDate(LocalDate.now().plusDays(5));
        license.setPrice(new BigDecimal("500.00"));

        when(licenseRepository.findAll()).thenReturn(List.of(license));
        when(licenseStatusService.calculateStatus(license)).thenReturn(LicenseStatus.EXPIRING_SOON);

        List<LicenseExpiryReportItemDTO> report = reportService.getLicenseExpiryReport("admin@test.com", "EXPIRING_7", null);

        assertNotNull(report);
        assertEquals(1, report.size());
        assertEquals("LIC-999", report.get(0).getLicenseKey());
        assertEquals(5L, report.get(0).getDaysRemaining());
    }

    @Test
    void testPdfExportService() {
        com.library.service.ReportService mockReportService = mock(com.library.service.ReportService.class);
        ReportSummaryDTO summary = new ReportSummaryDTO();
        summary.setTotalUsers(5);
        summary.setTotalBooks(10);
        summary.setTotalStorageFormatted("15.5 MB");

        when(mockReportService.getOverallSummary("admin@test.com", null, null)).thenReturn(summary);

        ReportExportServiceImpl exportService = new ReportExportServiceImpl(mockReportService);
        ByteArrayInputStream pdfStream = exportService.exportSummaryPdf("admin@test.com", null, null);

        assertNotNull(pdfStream);
        assertTrue(pdfStream.available() > 0);
    }
}
