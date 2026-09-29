package com.library.service.impl;

import com.library.dto.DashboardStatsDTO;
import com.library.entity.AssetStatus;
import com.library.entity.BookStatus;
import com.library.entity.LibraryStatus;
import com.library.entity.LicenseStatus;
import com.library.entity.UserStatus;
import com.library.repository.BookRepository;
import com.library.repository.DigitalAssetRepository;
import com.library.repository.LibraryRepository;
import com.library.repository.LicenseRepository;
import com.library.repository.UserRepository;
import com.library.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final LicenseRepository licenseRepository;
    private final DigitalAssetRepository digitalAssetRepository;
    private final LibraryRepository libraryRepository;

    public DashboardServiceImpl(BookRepository bookRepository,
                                UserRepository userRepository,
                                LicenseRepository licenseRepository,
                                DigitalAssetRepository digitalAssetRepository,
                                LibraryRepository libraryRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.licenseRepository = licenseRepository;
        this.digitalAssetRepository = digitalAssetRepository;
        this.libraryRepository = libraryRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        long totalBooks = bookRepository.count();
        long availableBooks = bookRepository.countByStatus(BookStatus.AVAILABLE);
        long unavailableBooks = bookRepository.countByStatus(BookStatus.UNAVAILABLE);
        long archivedBooks = bookRepository.countByStatus(BookStatus.ARCHIVED);
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();

        long totalLicenses = licenseRepository.count();
        long activeLicenses = licenseRepository.countByStatus(LicenseStatus.ACTIVE);
        long expiringSoonLicenses = licenseRepository.countByStatus(LicenseStatus.EXPIRING_SOON);
        long expiredLicenses = licenseRepository.countByStatus(LicenseStatus.EXPIRED);
        long pendingLicenses = licenseRepository.countByStatus(LicenseStatus.PENDING);
        long suspendedLicenses = licenseRepository.countByStatus(LicenseStatus.SUSPENDED);

        BigDecimal totalCost = licenseRepository.getTotalLicenseCost();
        if (totalCost == null) {
            totalCost = BigDecimal.ZERO;
        }

        long totalAssets = digitalAssetRepository.count();
        long activeAssets = digitalAssetRepository.countByStatus(AssetStatus.ACTIVE);
        long archivedAssets = digitalAssetRepository.countByStatus(AssetStatus.ARCHIVED);
        Long storageUsed = digitalAssetRepository.getTotalStorageUsed();
        long totalStorageUsedBytes = storageUsed != null ? storageUsed : 0L;

        long totalLibraries = libraryRepository.count();
        long activeLibraries = libraryRepository.countByStatus(LibraryStatus.ACTIVE);

        return new DashboardStatsDTO(
                totalBooks,
                availableBooks,
                unavailableBooks,
                archivedBooks,
                totalUsers,
                totalLicenses,
                activeLicenses,
                expiringSoonLicenses,
                expiredLicenses,
                pendingLicenses,
                suspendedLicenses,
                totalCost,
                totalAssets,
                activeAssets,
                archivedAssets,
                totalStorageUsedBytes,
                totalLibraries,
                activeLibraries,
                activeUsers
        );
    }
}
