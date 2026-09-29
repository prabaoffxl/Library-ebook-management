package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LicenseRepository;
import com.library.service.LicenseService;
import com.library.service.LicenseStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final BookRepository bookRepository;
    private final LicenseStatusService licenseStatusService;
    private final com.library.service.NotificationService notificationService;

    public LicenseServiceImpl(LicenseRepository licenseRepository, BookRepository bookRepository,
                              LicenseStatusService licenseStatusService,
                              com.library.service.NotificationService notificationService) {
        this.licenseRepository = licenseRepository;
        this.bookRepository = bookRepository;
        this.licenseStatusService = licenseStatusService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public LicenseResponse createLicense(LicenseRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (licenseRepository.existsByLicenseKey(request.getLicenseKey())) {
            throw new DuplicateResourceException("License key already exists: " + request.getLicenseKey());
        }

        validateLicenseDates(request.getStartDate(), request.getEndDate());

        License license = new License();
        license.setBook(book);
        license.setLicenseKey(request.getLicenseKey());
        license.setLicenseType(request.getLicenseType());
        license.setProvider(request.getProvider());
        license.setPurchaseDate(request.getPurchaseDate());
        license.setStartDate(request.getStartDate());
        license.setEndDate(request.getEndDate());
        license.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        license.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        license.setTerms(request.getTerms());

        applyMaxUsersRule(license, request.getLicenseType(), request.getMaxUsers());

        LicenseStatus calculatedStatus = licenseStatusService.calculateStatus(license);
        license.setStatus(calculatedStatus);

        License savedLicense = licenseRepository.save(license);
        return mapEntityToResponse(savedLicense);
    }

    @Override
    public LicenseResponse getLicenseById(Long id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found with id: " + id));
        license.setStatus(licenseStatusService.calculateStatus(license));
        return mapEntityToResponse(license);
    }

    @Override
    public List<LicenseResponse> getAllLicenses() {
        return licenseRepository.findAll().stream()
                .map(l -> {
                    l.setStatus(licenseStatusService.calculateStatus(l));
                    return mapEntityToResponse(l);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LicenseResponse> searchLicenses(String query, String provider, String statusStr, String licenseTypeStr, String bookTitle) {
        String keySearch = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String providerSearch = (provider != null && !provider.trim().isEmpty()) ? provider.trim() : null;
        String bookTitleSearch = (bookTitle != null && !bookTitle.trim().isEmpty()) ? bookTitle.trim() : null;

        LicenseStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = LicenseStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid license status: " + statusStr);
            }
        }

        LicenseType licenseType = null;
        if (licenseTypeStr != null && !licenseTypeStr.trim().isEmpty()) {
            try {
                licenseType = LicenseType.valueOf(licenseTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid license type: " + licenseTypeStr);
            }
        }

        return licenseRepository.searchLicenses(keySearch, providerSearch, bookTitleSearch, status, licenseType).stream()
                .map(l -> {
                    l.setStatus(licenseStatusService.calculateStatus(l));
                    return mapEntityToResponse(l);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LicenseResponse> filterLicenses(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return getAllLicenses();
        }
        try {
            LicenseStatus status = LicenseStatus.valueOf(statusStr.trim().toUpperCase());
            return licenseRepository.findByStatus(status).stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid license status: " + statusStr);
        }
    }

    @Override
    public List<LicenseResponse> getLicenseHistory(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        return licenseRepository.findByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LicenseResponse updateLicense(Long id, LicenseRequest request) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found with id: " + id));

        if (!license.getLicenseKey().equalsIgnoreCase(request.getLicenseKey()) &&
                licenseRepository.existsByLicenseKey(request.getLicenseKey())) {
            throw new DuplicateResourceException("License key already exists: " + request.getLicenseKey());
        }

        validateLicenseDates(request.getStartDate(), request.getEndDate());

        if (!license.getBook().getId().equals(request.getBookId())) {
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));
            license.setBook(book);
        }

        license.setLicenseKey(request.getLicenseKey());
        license.setLicenseType(request.getLicenseType());
        license.setProvider(request.getProvider());
        license.setPurchaseDate(request.getPurchaseDate());
        license.setStartDate(request.getStartDate());
        license.setEndDate(request.getEndDate());
        license.setPrice(request.getPrice());
        license.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        license.setTerms(request.getTerms());

        applyMaxUsersRule(license, request.getLicenseType(), request.getMaxUsers());

        license.setStatus(licenseStatusService.calculateStatus(license));
        License updated = licenseRepository.save(license);
        return mapEntityToResponse(updated);
    }

    @Override
    @Transactional
    public LicenseResponse renewLicense(Long id, LicenseRenewRequest request) {
        License oldLicense = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found with id: " + id));

        validateLicenseDates(request.getStartDate(), request.getEndDate());

        // Mark previous license as RENEWED
        oldLicense.setStatus(LicenseStatus.RENEWED);
        licenseRepository.save(oldLicense);

        // Create new license linked to previous
        License newLicense = new License();
        newLicense.setBook(oldLicense.getBook());
        
        String newKey = (request.getLicenseKey() != null && !request.getLicenseKey().trim().isEmpty()) ?
                request.getLicenseKey().trim() : oldLicense.getLicenseKey() + "-RN" + (System.currentTimeMillis() % 1000);

        if (licenseRepository.existsByLicenseKey(newKey)) {
            newKey = "LIC-RN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        newLicense.setLicenseKey(newKey);
        newLicense.setLicenseType(oldLicense.getLicenseType());
        newLicense.setProvider(oldLicense.getProvider());
        newLicense.setPurchaseDate(LocalDate.now());
        newLicense.setStartDate(request.getStartDate());
        newLicense.setEndDate(request.getEndDate());
        newLicense.setMaxUsers(oldLicense.getMaxUsers());
        newLicense.setPrice(request.getPrice());
        newLicense.setCurrency(oldLicense.getCurrency());
        newLicense.setTerms(request.getTerms() != null ? request.getTerms() : oldLicense.getTerms());
        newLicense.setPreviousLicense(oldLicense);

        LicenseStatus calculatedStatus = licenseStatusService.calculateStatus(newLicense);
        newLicense.setStatus(calculatedStatus);

        License savedNew = licenseRepository.save(newLicense);

        notificationService.notifyAdminsAndAssignedLibrarians(
                savedNew.getBook().getId(),
                "License Renewed: " + savedNew.getBook().getTitle(),
                "License " + savedNew.getLicenseKey() + " for '" + savedNew.getBook().getTitle() + "' was successfully renewed until " + savedNew.getEndDate() + ".",
                com.library.entity.NotificationType.LICENSE_RENEWED,
                com.library.entity.NotificationPriority.MEDIUM,
                savedNew.getId(),
                null
        );

        return mapEntityToResponse(savedNew);
    }

    @Override
    @Transactional
    public LicenseResponse suspendLicense(Long id, LicenseSuspendRequest request) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found with id: " + id));

        license.setStatus(LicenseStatus.SUSPENDED);
        if (request != null && request.getReason() != null) {
            license.setSuspensionReason(request.getReason());
        }

        License saved = licenseRepository.save(license);

        notificationService.notifyAdminsAndAssignedLibrarians(
                saved.getBook().getId(),
                "License Suspended: " + saved.getBook().getTitle(),
                "License " + saved.getLicenseKey() + " for '" + saved.getBook().getTitle() + "' has been suspended. Reason: " + (saved.getSuspensionReason() != null ? saved.getSuspensionReason() : "N/A"),
                com.library.entity.NotificationType.LICENSE_SUSPENDED,
                com.library.entity.NotificationPriority.HIGH,
                saved.getId(),
                null
        );

        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional
    public LicenseResponse cancelLicense(Long id) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("License not found with id: " + id));

        license.setStatus(LicenseStatus.CANCELLED);
        License saved = licenseRepository.save(license);

        notificationService.notifyAdminsAndAssignedLibrarians(
                saved.getBook().getId(),
                "License Cancelled: " + saved.getBook().getTitle(),
                "License " + saved.getLicenseKey() + " for '" + saved.getBook().getTitle() + "' has been cancelled.",
                com.library.entity.NotificationType.LICENSE_CANCELLED,
                com.library.entity.NotificationPriority.HIGH,
                saved.getId(),
                null
        );

        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLicense(Long id) {
        if (!licenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("License not found with id: " + id);
        }
        licenseRepository.deleteById(id);
    }

    @Override
    public void validateBookDownloadLicense(Long bookId) {
        List<License> licenses = licenseRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        if (licenses.isEmpty()) {
            throw new BadRequestException("No active license exists for this e-book.");
        }

        // Find current/latest license
        License latest = licenses.get(0);
        LicenseStatus status = licenseStatusService.calculateStatus(latest);

        if (status == LicenseStatus.EXPIRED) {
            throw new BadRequestException("E-book license has expired.");
        }
        if (status == LicenseStatus.SUSPENDED) {
            throw new BadRequestException("E-book license is currently suspended.");
        }
        if (status == LicenseStatus.CANCELLED) {
            throw new BadRequestException("E-book license has been cancelled.");
        }
        if (status == LicenseStatus.PENDING) {
            throw new BadRequestException("E-book license start date has not been reached.");
        }
    }

    private void validateLicenseDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("License start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }
    }

    private void applyMaxUsersRule(License license, LicenseType type, Integer requestMaxUsers) {
        if (type == LicenseType.SINGLE_USER) {
            license.setMaxUsers(1);
        } else if (type == LicenseType.UNLIMITED) {
            license.setMaxUsers(9999);
        } else if (type == LicenseType.MULTI_USER) {
            if (requestMaxUsers != null && requestMaxUsers > 1) {
                license.setMaxUsers(requestMaxUsers);
            } else {
                license.setMaxUsers(10); // default multi-user limit if not provided
            }
        }
    }

    private LicenseResponse mapEntityToResponse(License license) {
        LicenseResponse res = new LicenseResponse();
        res.setId(license.getId());
        res.setBookId(license.getBook().getId());
        res.setBookTitle(license.getBook().getTitle());
        res.setLicenseKey(license.getLicenseKey());
        res.setLicenseType(license.getLicenseType());
        res.setProvider(license.getProvider());
        res.setPurchaseDate(license.getPurchaseDate());
        res.setStartDate(license.getStartDate());
        res.setEndDate(license.getEndDate());
        res.setStatus(license.getStatus());
        res.setMaxUsers(license.getMaxUsers());
        res.setCurrentUsers(license.getCurrentUsers());
        res.setPrice(license.getPrice());
        res.setCurrency(license.getCurrency());
        res.setTerms(license.getTerms());
        res.setSuspensionReason(license.getSuspensionReason());
        if (license.getPreviousLicense() != null) {
            res.setPreviousLicenseId(license.getPreviousLicense().getId());
        }
        res.setCreatedAt(license.getCreatedAt());
        res.setUpdatedAt(license.getUpdatedAt());
        return res;
    }
}
