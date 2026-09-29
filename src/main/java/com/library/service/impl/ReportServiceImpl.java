package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.dto.DigitalAssetResponse;
import com.library.dto.report.*;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.LicenseStatusService;
import com.library.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final LicenseRepository licenseRepository;
    private final DigitalAssetRepository digitalAssetRepository;
    private final UserActivityRepository userActivityRepository;
    private final NotificationRepository notificationRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LibrarianAssignmentRepository librarianAssignmentRepository;
    private final LicenseStatusService licenseStatusService;

    public ReportServiceImpl(UserRepository userRepository,
                             LibraryRepository libraryRepository,
                             BookRepository bookRepository,
                             LicenseRepository licenseRepository,
                             DigitalAssetRepository digitalAssetRepository,
                             UserActivityRepository userActivityRepository,
                             NotificationRepository notificationRepository,
                             LibraryBookRepository libraryBookRepository,
                             LibrarianAssignmentRepository librarianAssignmentRepository,
                             LicenseStatusService licenseStatusService) {
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
        this.licenseRepository = licenseRepository;
        this.digitalAssetRepository = digitalAssetRepository;
        this.userActivityRepository = userActivityRepository;
        this.notificationRepository = notificationRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.librarianAssignmentRepository = librarianAssignmentRepository;
        this.licenseStatusService = licenseStatusService;
    }

    private User validateAndGetUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() == Role.USER) {
            throw new UnauthorizedAccessException("Access denied. Patrons cannot access administrative reports.");
        }
        return user;
    }

    private Set<Long> getAssignedLibraryIdsForLibrarian(User user) {
        if (user.getRole() == Role.ADMIN) {
            return null; // Null means no filtering (all libraries)
        }
        List<LibrarianAssignment> assignments = librarianAssignmentRepository.findByUserIdAndStatus(user.getId(), LibrarianAssignmentStatus.ACTIVE);
        return assignments.stream().map(a -> a.getLibrary().getId()).collect(Collectors.toSet());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
    }

    @Override
    public ReportSummaryDTO getOverallSummary(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        Set<Long> libIds = getAssignedLibraryIdsForLibrarian(user);
        ReportSummaryDTO dto = new ReportSummaryDTO();

        dto.setTotalUsers(userRepository.count());
        dto.setActiveUsers(userRepository.countByStatus(UserStatus.ACTIVE));
        dto.setTotalLibraries(libraryRepository.count());

        dto.setTotalBooks(bookRepository.count());
        dto.setAvailableBooks(bookRepository.countByStatus(BookStatus.AVAILABLE));
        dto.setUnavailableBooks(bookRepository.countByStatus(BookStatus.UNAVAILABLE));

        dto.setTotalDigitalAssets(digitalAssetRepository.countByStatus(AssetStatus.ACTIVE));
        Long storageBytes = digitalAssetRepository.getTotalStorageUsed();
        long totalStorage = storageBytes != null ? storageBytes : 0L;
        dto.setTotalStorageBytes(totalStorage);
        dto.setTotalStorageFormatted(formatBytes(totalStorage));

        // License calculations
        List<License> licenses = licenseRepository.findAll();
        long activeLic = 0, expiringLic = 0, expiredLic = 0, renewedLic = 0, cancelledLic = 0;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (License l : licenses) {
            LicenseStatus currentStatus = licenseStatusService.calculateStatus(l);
            if (currentStatus == LicenseStatus.ACTIVE) activeLic++;
            else if (currentStatus == LicenseStatus.EXPIRING_SOON) expiringLic++;
            else if (currentStatus == LicenseStatus.EXPIRED) expiredLic++;
            else if (currentStatus == LicenseStatus.RENEWED) renewedLic++;
            else if (currentStatus == LicenseStatus.CANCELLED) cancelledLic++;

            if (l.getPrice() != null) {
                totalCost = totalCost.add(l.getPrice());
            }
        }

        dto.setActiveLicenses(activeLic);
        dto.setExpiringLicenses(expiringLic);
        dto.setExpiredLicenses(expiredLic);
        dto.setRenewedLicenses(renewedLic);
        dto.setCancelledLicenses(cancelledLic);
        dto.setTotalLicenseCost(totalCost);

        dto.setUnreadNotifications(notificationRepository.countByUserIdAndIsReadFalse(user.getId()));

        return dto;
    }

    @Override
    public BookReportDTO getBookAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        BookReportDTO dto = new BookReportDTO();
        dto.setTotalBooks(bookRepository.count());
        dto.setAvailableBooks(bookRepository.countByStatus(BookStatus.AVAILABLE));
        dto.setUnavailableBooks(bookRepository.countByStatus(BookStatus.UNAVAILABLE));

        dto.setBooksByCategory(mapObjectListToLabelValues(bookRepository.countBooksByCategory()));
        dto.setBooksByLanguage(mapObjectListToLabelValues(bookRepository.countBooksByLanguage()));
        dto.setBooksByPublicationYear(mapObjectListToLabelValues(bookRepository.countBooksByPublicationYear()));
        dto.setBooksByLibrary(mapObjectListToLabelValues(bookRepository.countBooksByLibrary()));

        List<Book> recent = bookRepository.findTop10ByOrderByCreatedAtDesc();
        dto.setRecentlyAddedBooks(recent.stream().map(this::mapBookToDto).collect(Collectors.toList()));

        dto.setMostViewedBooks(mapObjectListToLabelValues(userActivityRepository.findMostViewedBooks()));
        dto.setMostDownloadedBooks(mapObjectListToLabelValues(userActivityRepository.findMostDownloadedBooks()));

        return dto;
    }

    @Override
    public LicenseReportDTO getLicenseAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        LicenseReportDTO dto = new LicenseReportDTO();
        List<License> licenses = licenseRepository.findAll();

        long active = 0, pending = 0, expiring = 0, expired = 0, suspended = 0, renewed = 0, cancelled = 0;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (License l : licenses) {
            LicenseStatus status = licenseStatusService.calculateStatus(l);
            if (status == LicenseStatus.ACTIVE) active++;
            else if (status == LicenseStatus.PENDING) pending++;
            else if (status == LicenseStatus.EXPIRING_SOON) expiring++;
            else if (status == LicenseStatus.EXPIRED) expired++;
            else if (status == LicenseStatus.SUSPENDED) suspended++;
            else if (status == LicenseStatus.RENEWED) renewed++;
            else if (status == LicenseStatus.CANCELLED) cancelled++;

            if (l.getPrice() != null) {
                totalCost = totalCost.add(l.getPrice());
            }
        }

        dto.setTotalLicenses(licenses.size());
        dto.setActiveLicenses(active);
        dto.setPendingLicenses(pending);
        dto.setExpiringSoonLicenses(expiring);
        dto.setExpiredLicenses(expired);
        dto.setSuspendedLicenses(suspended);
        dto.setRenewedLicenses(renewed);
        dto.setCancelledLicenses(cancelled);
        dto.setTotalCost(totalCost);

        dto.setLicensesByType(mapObjectListToLabelValues(licenseRepository.countLicensesByType()));
        dto.setLicensesByProvider(mapObjectListToLabelValues(licenseRepository.countLicensesByProvider()));
        dto.setLicensesByLibrary(mapObjectListToLabelValues(licenseRepository.countLicensesByLibrary()));

        // Cost by Provider as fallback/supplement
        dto.setCostByMonth(mapObjectListToLabelValues(licenseRepository.countLicensesByProvider()));
        dto.setExpirationTrends(mapObjectListToLabelValues(licenseRepository.countLicensesByStatus()));

        return dto;
    }

    @Override
    public List<LicenseExpiryReportItemDTO> getLicenseExpiryReport(String userEmail, String statusFilter, Integer daysThreshold) {
        validateAndGetUser(userEmail);
        List<License> licenses = licenseRepository.findAll();
        LocalDate today = LocalDate.now();
        List<LicenseExpiryReportItemDTO> result = new ArrayList<>();

        for (License l : licenses) {
            LicenseStatus status = licenseStatusService.calculateStatus(l);
            long daysLeft = l.getEndDate() != null ? ChronoUnit.DAYS.between(today, l.getEndDate()) : 0;

            boolean matches = true;
            if (statusFilter != null && !statusFilter.isBlank()) {
                String sf = statusFilter.trim().toUpperCase();
                if (sf.equals("EXPIRING_7")) {
                    matches = (daysLeft >= 0 && daysLeft <= 7);
                } else if (sf.equals("EXPIRING_30")) {
                    matches = (daysLeft >= 0 && daysLeft <= 30);
                } else if (sf.equals("EXPIRED")) {
                    matches = (status == LicenseStatus.EXPIRED || daysLeft < 0);
                } else if (sf.equals("ACTIVE")) {
                    matches = (status == LicenseStatus.ACTIVE);
                } else if (sf.equals("SUSPENDED")) {
                    matches = (status == LicenseStatus.SUSPENDED);
                }
            } else if (daysThreshold != null) {
                matches = (daysLeft >= 0 && daysLeft <= daysThreshold);
            }

            if (matches) {
                LicenseExpiryReportItemDTO item = new LicenseExpiryReportItemDTO();
                item.setId(l.getId());
                item.setLicenseKey(l.getLicenseKey());
                if (l.getBook() != null) {
                    item.setBookId(l.getBook().getId());
                    item.setBookTitle(l.getBook().getTitle());
                }
                item.setProvider(l.getProvider());
                item.setLicenseType(l.getLicenseType());
                item.setStartDate(l.getStartDate());
                item.setEndDate(l.getEndDate());
                item.setStatus(status);
                item.setDaysRemaining(daysLeft);
                item.setMaxUsers(l.getMaxUsers());
                item.setCurrentUsers(l.getCurrentUsers());
                item.setPrice(l.getPrice());
                result.add(item);
            }
        }

        result.sort(Comparator.comparingLong(LicenseExpiryReportItemDTO::getDaysRemaining));
        return result;
    }

    @Override
    public AssetReportDTO getDigitalAssetAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        AssetReportDTO dto = new AssetReportDTO();
        dto.setTotalAssets(digitalAssetRepository.count());
        dto.setActiveAssets(digitalAssetRepository.countByStatus(AssetStatus.ACTIVE));
        dto.setArchivedAssets(digitalAssetRepository.countByStatus(AssetStatus.ARCHIVED));
        dto.setReplacedAssets(digitalAssetRepository.countByStatus(AssetStatus.REPLACED));
        dto.setDeletedAssets(digitalAssetRepository.countByStatus(AssetStatus.DELETED));

        dto.setEbookAssets(digitalAssetRepository.countByAssetType(AssetType.EBOOK));
        dto.setCoverAssets(digitalAssetRepository.countByAssetType(AssetType.COVER_IMAGE));
        dto.setPreviewAssets(digitalAssetRepository.countByAssetType(AssetType.PREVIEW));

        Long storageBytes = digitalAssetRepository.getTotalStorageUsed();
        long totalStorage = storageBytes != null ? storageBytes : 0L;
        dto.setTotalStorageBytes(totalStorage);
        dto.setTotalStorageFormatted(formatBytes(totalStorage));

        dto.setStorageByAssetType(mapObjectListToLabelValues(digitalAssetRepository.countAndStorageByAssetType()));
        dto.setAssetsByLibrary(mapObjectListToLabelValues(digitalAssetRepository.countAndStorageByLibrary()));
        dto.setAssetsByFileType(mapObjectListToLabelValues(digitalAssetRepository.countAssetsByExtension()));

        List<DigitalAsset> recent = digitalAssetRepository.findTop10ByOrderByCreatedAtDesc();
        dto.setRecentlyUploadedAssets(recent.stream().map(this::mapAssetToDto).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public List<LibraryReportDTO> getLibraryAnalytics(String userEmail) {
        User user = validateAndGetUser(userEmail);
        Set<Long> assignedLibIds = getAssignedLibraryIdsForLibrarian(user);

        List<Library> libraries = libraryRepository.findAll();
        List<LibraryReportDTO> result = new ArrayList<>();

        for (Library lib : libraries) {
            if (assignedLibIds != null && !assignedLibIds.contains(lib.getId())) {
                continue; // Skip libraries not assigned to librarian
            }
            result.add(buildLibraryReportDTO(lib));
        }

        return result;
    }

    @Override
    public LibraryReportDTO getLibraryAnalyticsById(Long libraryId, String userEmail) {
        User user = validateAndGetUser(userEmail);
        Set<Long> assignedLibIds = getAssignedLibraryIdsForLibrarian(user);

        if (assignedLibIds != null && !assignedLibIds.contains(libraryId)) {
            throw new UnauthorizedAccessException("You do not have permission to view report for library ID: " + libraryId);
        }

        Library lib = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + libraryId));

        return buildLibraryReportDTO(lib);
    }

    private LibraryReportDTO buildLibraryReportDTO(Library lib) {
        LibraryReportDTO dto = new LibraryReportDTO();
        dto.setLibraryId(lib.getId());
        dto.setLibraryName(lib.getName());
        dto.setLibraryCode(lib.getCode());

        dto.setTotalBooks(libraryBookRepository.countByLibraryId(lib.getId()));
        dto.setAvailableBooks(libraryBookRepository.countByLibraryIdAndAvailabilityStatus(lib.getId(), LibraryBookStatus.AVAILABLE));
        dto.setUnavailableBooks(libraryBookRepository.countByLibraryIdAndAvailabilityStatus(lib.getId(), LibraryBookStatus.UNAVAILABLE));

        List<LibrarianAssignment> assignments = librarianAssignmentRepository.findByLibraryIdAndStatus(lib.getId(), LibrarianAssignmentStatus.ACTIVE);
        dto.setAssignedLibrarians(assignments.size());

        dto.setRegisteredUsers(userRepository.countByStatus(UserStatus.ACTIVE));

        // Active & Expiring licenses for books in this library
        List<LibraryBook> libBooks = libraryBookRepository.findByLibraryId(lib.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        long activeLic = 0, expiringLic = 0;
        long totalAssetCount = 0;
        long totalStorage = 0;

        for (LibraryBook lb : libBooks) {
            Book b = lb.getBook();
            List<License> bLicenses = licenseRepository.findByBookId(b.getId());
            for (License l : bLicenses) {
                LicenseStatus st = licenseStatusService.calculateStatus(l);
                if (st == LicenseStatus.ACTIVE) activeLic++;
                else if (st == LicenseStatus.EXPIRING_SOON) expiringLic++;
            }

            List<DigitalAsset> bAssets = digitalAssetRepository.findByBookId(b.getId());
            for (DigitalAsset da : bAssets) {
                if (da.getStatus() != AssetStatus.DELETED) {
                    totalAssetCount++;
                    totalStorage += da.getFileSize();
                }
            }
        }

        dto.setActiveLicenses(activeLic);
        dto.setExpiringLicenses(expiringLic);
        dto.setDigitalAssets(totalAssetCount);
        dto.setStorageBytes(totalStorage);
        dto.setStorageFormatted(formatBytes(totalStorage));
        dto.setRecentActivityCount(userActivityRepository.countByLibraryId(lib.getId()));

        return dto;
    }

    @Override
    public UserReportDTO getUserAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        UserReportDTO dto = new UserReportDTO();
        dto.setTotalUsers(userRepository.count());
        dto.setActiveUsers(userRepository.countByStatus(UserStatus.ACTIVE));
        dto.setInactiveUsers(userRepository.countByStatus(UserStatus.INACTIVE));
        dto.setSuspendedUsers(userRepository.countByStatus(UserStatus.SUSPENDED));

        dto.setUsersByRole(mapObjectListToLabelValues(userRepository.countUsersByRole()));
        dto.setUsersByLibrary(mapObjectListToLabelValues(userRepository.countUsersByLibrary()));
        dto.setActivityByType(mapObjectListToLabelValues(userActivityRepository.countByActivityType()));

        return dto;
    }

    @Override
    public ActivityReportDTO getActivityAnalytics(String userEmail, LocalDate startDate, LocalDate endDate) {
        validateAndGetUser(userEmail);
        validateDateRange(startDate, endDate);

        ActivityReportDTO dto = new ActivityReportDTO();
        dto.setTotalActivities(userActivityRepository.count());
        dto.setActivityByType(mapObjectListToLabelValues(userActivityRepository.countByActivityType()));
        dto.setActivityByDay(mapObjectListToLabelValues(userActivityRepository.countActivityByDay()));
        dto.setActivityByMonth(mapObjectListToLabelValues(userActivityRepository.countActivityByDay()));
        dto.setMostActiveUsers(mapObjectListToLabelValues(userActivityRepository.findMostActiveUsers()));
        dto.setMostViewedBooks(mapObjectListToLabelValues(userActivityRepository.findMostViewedBooks()));
        dto.setMostDownloadedBooks(mapObjectListToLabelValues(userActivityRepository.findMostDownloadedBooks()));

        return dto;
    }

    private List<LabelValueDTO> mapObjectListToLabelValues(List<Object[]> rawList) {
        if (rawList == null) return new ArrayList<>();
        List<LabelValueDTO> result = new ArrayList<>();
        for (Object[] obj : rawList) {
            if (obj != null && obj.length >= 2) {
                String label = obj[0] != null ? obj[0].toString() : "Unknown";
                Number val = obj[1] instanceof Number ? (Number) obj[1] : 0;
                result.add(new LabelValueDTO(label, val));
            }
        }
        return result;
    }

    private BookDTO mapBookToDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setStatus(book.getStatus());
        dto.setCreatedAt(book.getCreatedAt());
        return dto;
    }

    private DigitalAssetResponse mapAssetToDto(DigitalAsset asset) {
        DigitalAssetResponse dto = new DigitalAssetResponse();
        dto.setId(asset.getId());
        dto.setBookId(asset.getBook().getId());
        dto.setBookTitle(asset.getBook().getTitle());
        dto.setAssetType(asset.getAssetType());
        dto.setOriginalFileName(asset.getOriginalFileName());
        dto.setStoredFileName(asset.getStoredFileName());
        dto.setFileSize(asset.getFileSize());
        dto.setVersion(asset.getVersion());
        dto.setStatus(asset.getStatus());
        dto.setCreatedAt(asset.getCreatedAt());
        return dto;
    }

    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
