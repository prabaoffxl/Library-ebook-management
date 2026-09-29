package com.library.service.impl;

import com.library.dto.reader.ReadingProgressDTO;
import com.library.dto.reader.ReadingProgressRequest;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookReadingServiceImpl implements BookReadingService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final DigitalAssetRepository digitalAssetRepository;
    private final LicenseRepository licenseRepository;
    private final LicenseStatusService licenseStatusService;
    private final FileStorageService fileStorageService;
    private final DocumentProtectionService documentProtectionService;
    private final UserActivityService userActivityService;
    private final ReadingProgressRepository readingProgressRepository;

    @Autowired
    public BookReadingServiceImpl(UserRepository userRepository,
                                 BookRepository bookRepository,
                                 DigitalAssetRepository digitalAssetRepository,
                                 LicenseRepository licenseRepository,
                                 LicenseStatusService licenseStatusService,
                                 FileStorageService fileStorageService,
                                 DocumentProtectionService documentProtectionService,
                                 UserActivityService userActivityService,
                                 ReadingProgressRepository readingProgressRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.digitalAssetRepository = digitalAssetRepository;
        this.licenseRepository = licenseRepository;
        this.licenseStatusService = licenseStatusService;
        this.fileStorageService = fileStorageService;
        this.documentProtectionService = documentProtectionService;
        this.userActivityService = userActivityService;
        this.readingProgressRepository = readingProgressRepository;
    }

    @Override
    public ResponseEntity<Resource> streamEbook(Long bookId, String rangeHeader, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedAccessException("User account is not active.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        if (book.getStatus() == BookStatus.ARCHIVED) {
            throw new BadRequestException("This book is archived and unavailable for reading.");
        }

        DigitalAsset asset = digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(bookId, AssetType.EBOOK, AssetStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active digital e-book asset found for this title."));

        validateLicenseForReading(bookId);

        userActivityService.logActivity(userEmail, ActivityType.BOOK_READ, bookId, null, "Read e-book: " + book.getTitle());

        Resource fileResource = fileStorageService.loadFileAsResource(asset.getFilePath());
        Resource protectedResource = documentProtectionService.applyProtection(fileResource, userEmail, book.getTitle());

        long fileLength = asset.getFileSize();
        if (fileLength <= 0 && fileResource.exists()) {
            try {
                fileLength = fileResource.contentLength();
            } catch (IOException ignored) {
            }
        }

        return buildStreamingResponse(protectedResource, fileLength, rangeHeader, asset.getStoredFileName());
    }

    @Override
    public ResponseEntity<Resource> streamPreview(Long bookId, String rangeHeader, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedAccessException("User account is not active.");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Optional<DigitalAsset> previewAssetOpt = digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(bookId, AssetType.PREVIEW, AssetStatus.ACTIVE);
        DigitalAsset asset;
        if (previewAssetOpt.isPresent()) {
            asset = previewAssetOpt.get();
        } else {
            asset = digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(bookId, AssetType.EBOOK, AssetStatus.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("No preview or e-book asset found for this title."));
        }

        Resource fileResource = fileStorageService.loadFileAsResource(asset.getFilePath());
        Resource protectedResource = documentProtectionService.applyProtection(fileResource, userEmail, book.getTitle());

        long fileLength = asset.getFileSize();
        if (fileLength <= 0 && fileResource.exists()) {
            try {
                fileLength = fileResource.contentLength();
            } catch (IOException ignored) {
            }
        }

        return buildStreamingResponse(protectedResource, fileLength, rangeHeader, asset.getStoredFileName());
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingProgressDTO getReadingProgress(Long bookId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Optional<ReadingProgress> progressOpt = readingProgressRepository.findByUserIdAndBookId(user.getId(), bookId);
        if (progressOpt.isPresent()) {
            return mapToDTO(progressOpt.get());
        }

        return new ReadingProgressDTO(null, user.getId(), book.getId(), book.getTitle(), book.getAuthor(), book.getCoverImage(), 1, 1, 0.0, LocalDateTime.now());
    }

    @Override
    public ReadingProgressDTO updateReadingProgress(Long bookId, ReadingProgressRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (request == null || request.getCurrentPage() == null || request.getCurrentPage() < 1) {
            throw new BadRequestException("Current page must be at least 1.");
        }

        int currentPage = request.getCurrentPage();
        int totalPages = request.getTotalPages() != null && request.getTotalPages() > 0 ? request.getTotalPages() : 1;

        if (currentPage > totalPages && totalPages > 1) {
            currentPage = totalPages;
        }

        Optional<ReadingProgress> progressOpt = readingProgressRepository.findByUserIdAndBookId(user.getId(), bookId);
        ReadingProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            progress.setCurrentPage(currentPage);
            progress.setTotalPages(totalPages);
            progress.calculateProgress();
            progress.setLastReadAt(LocalDateTime.now());
        } else {
            progress = new ReadingProgress(user, book, currentPage, totalPages);
        }

        ReadingProgress saved = readingProgressRepository.save(progress);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingProgressDTO> getUserReadingList(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not authenticated"));

        List<ReadingProgress> list = readingProgressRepository.findByUserIdOrderByLastReadAtDesc(user.getId());
        return list.stream().map(this::mapToDTO).toList();
    }

    private void validateLicenseForReading(Long bookId) {
        List<License> licenses = licenseRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        if (licenses.isEmpty()) {
            throw new BadRequestException("A valid license is required to access this e-book.");
        }

        License latest = licenses.get(0);
        LicenseStatus status = licenseStatusService.calculateStatus(latest);

        if (status != LicenseStatus.ACTIVE && status != LicenseStatus.EXPIRING_SOON) {
            throw new BadRequestException("A valid license is required to access this e-book.");
        }

        if (latest.getLicenseType() != LicenseType.UNLIMITED && latest.getMaxUsers() != null) {
            if (latest.getCurrentUsers() >= latest.getMaxUsers()) {
                throw new BadRequestException("Concurrent user limit (" + latest.getMaxUsers() + ") reached for this e-book license.");
            }
        }
    }

    private ResponseEntity<Resource> buildStreamingResponse(Resource resource, long fileLength, String rangeHeader, String fileName) {
        if (rangeHeader != null && rangeHeader.startsWith("bytes=") && fileLength > 0) {
            try {
                String rangeValue = rangeHeader.substring(6).trim();
                long start = 0;
                long end = fileLength - 1;
                if (rangeValue.contains("-")) {
                    String[] parts = rangeValue.split("-", -1);
                    if (!parts[0].isEmpty()) {
                        start = Long.parseLong(parts[0]);
                    }
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        end = Long.parseLong(parts[1]);
                    }
                }
                if (end >= fileLength) {
                    end = fileLength - 1;
                }
                if (start <= end && start < fileLength) {
                    long rangeLength = end - start + 1;
                    HttpHeaders headers = documentProtectionService.prepareSecureStreamingHeaders(fileName, rangeLength);
                    headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength);
                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .headers(headers)
                            .body(resource);
                }
            } catch (Exception ignored) {
            }
        }

        HttpHeaders headers = documentProtectionService.prepareSecureStreamingHeaders(fileName, fileLength);
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private ReadingProgressDTO mapToDTO(ReadingProgress progress) {
        return new ReadingProgressDTO(
                progress.getId(),
                progress.getUser().getId(),
                progress.getBook().getId(),
                progress.getBook().getTitle(),
                progress.getBook().getAuthor(),
                progress.getBook().getCoverImage(),
                progress.getCurrentPage(),
                progress.getTotalPages(),
                progress.getProgressPercentage(),
                progress.getLastReadAt()
        );
    }
}
