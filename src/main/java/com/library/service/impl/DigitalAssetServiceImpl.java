package com.library.service.impl;

import com.library.dto.DigitalAssetResponse;
import com.library.dto.PageResponse;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.FileStorageException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.DigitalAssetRepository;
import com.library.service.ChecksumService;
import com.library.service.DigitalAssetService;
import com.library.service.FileStorageService;
import com.library.service.LicenseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DigitalAssetServiceImpl implements DigitalAssetService {

    private final DigitalAssetRepository digitalAssetRepository;
    private final BookRepository bookRepository;
    private final ChecksumService checksumService;
    private final FileStorageService fileStorageService;
    private final LicenseService licenseService;
    private final com.library.service.NotificationService notificationService;

    private final Path basePath;

    public DigitalAssetServiceImpl(
            DigitalAssetRepository digitalAssetRepository,
            BookRepository bookRepository,
            ChecksumService checksumService,
            FileStorageService fileStorageService,
            LicenseService licenseService,
            com.library.service.NotificationService notificationService,
            @Value("${storage.base-path:storage}") String basePathStr
    ) {
        this.digitalAssetRepository = digitalAssetRepository;
        this.bookRepository = bookRepository;
        this.checksumService = checksumService;
        this.fileStorageService = fileStorageService;
        this.licenseService = licenseService;
        this.notificationService = notificationService;
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public DigitalAssetResponse uploadAsset(Long bookId, AssetType assetType, MultipartFile file, String currentUserEmail) {
        return processAssetUpload(bookId, assetType, file, currentUserEmail, false);
    }

    @Override
    @Transactional
    public DigitalAssetResponse replaceEbook(Long bookId, MultipartFile file, String currentUserEmail) {
        return processAssetUpload(bookId, AssetType.EBOOK, file, currentUserEmail, true);
    }

    @Override
    @Transactional
    public DigitalAssetResponse uploadCover(Long bookId, MultipartFile file, String currentUserEmail) {
        return processAssetUpload(bookId, AssetType.COVER_IMAGE, file, currentUserEmail, true);
    }

    @Override
    @Transactional
    public DigitalAssetResponse uploadPreview(Long bookId, MultipartFile file, String currentUserEmail) {
        return processAssetUpload(bookId, AssetType.PREVIEW, file, currentUserEmail, false);
    }

    private DigitalAssetResponse processAssetUpload(Long bookId, AssetType assetType, MultipartFile file, String currentUserEmail, boolean isReplacement) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed");
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
        }

        String extension = getExtension(originalFilename);
        String mimeType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        validateAssetFormat(assetType, extension, mimeType);

        // Calculate SHA-256 Checksum
        String checksum = checksumService.calculateChecksum(file);

        // Check Duplicate Active Asset
        if (digitalAssetRepository.existsByBookIdAndAssetTypeAndChecksumAndStatus(bookId, assetType, checksum, AssetStatus.ACTIVE)) {
            throw new DuplicateResourceException("Identical digital asset already exists for this book.");
        }

        // Determine Version & Handle Existing Active Asset
        int nextVersion = 1;
        Optional<DigitalAsset> currentActiveOpt = digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(bookId, assetType, AssetStatus.ACTIVE);
        if (currentActiveOpt.isPresent()) {
            DigitalAsset currentActive = currentActiveOpt.get();
            nextVersion = currentActive.getVersion() + 1;
            currentActive.setStatus(isReplacement ? AssetStatus.REPLACED : AssetStatus.ARCHIVED);
            digitalAssetRepository.save(currentActive);
        } else {
            // Find max version for book & assetType
            List<DigitalAsset> existingAssets = digitalAssetRepository.findByBookIdOrderByVersionDesc(bookId);
            if (!existingAssets.isEmpty()) {
                nextVersion = existingAssets.stream()
                        .filter(a -> a.getAssetType() == assetType)
                        .mapToInt(DigitalAsset::getVersion)
                        .max().orElse(0) + 1;
            }
        }

        // Determine Directory Path: storage/{ebooks|covers|previews}/book-{id}/
        String subDirName = getSubDirectoryName(assetType);
        Path targetDirectory = basePath.resolve(subDirName).resolve("book-" + bookId);

        String storedFileName = UUID.randomUUID().toString() + "_v" + nextVersion + "_" + originalFilename;
        Path targetPath = targetDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store physical file for asset", ex);
        }

        String relativePath = basePath.relativize(targetPath).toString().replace('\\', '/');

        // Create & Save Entity
        DigitalAsset asset = new DigitalAsset();
        asset.setBook(book);
        asset.setAssetType(assetType);
        asset.setOriginalFileName(originalFilename);
        asset.setStoredFileName(storedFileName);
        asset.setFilePath(relativePath);
        asset.setMimeType(mimeType);
        asset.setFileExtension(extension);
        asset.setFileSize(file.getSize());
        asset.setChecksum(checksum);
        asset.setVersion(nextVersion);
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setUploadedBy(currentUserEmail != null ? currentUserEmail : "system");

        DigitalAsset savedAsset = digitalAssetRepository.save(asset);

        // Update book entity convenience metadata
        if (assetType == AssetType.EBOOK) {
            book.setFileName(originalFilename);
            book.setFilePath(relativePath);
            book.setFileSize(file.getSize());
            book.setFileType(extension.toUpperCase());
            bookRepository.save(book);
        } else if (assetType == AssetType.COVER_IMAGE) {
            book.setCoverImage(relativePath);
            bookRepository.save(book);
        }

        notificationService.notifyAdminsAndAssignedLibrarians(
                bookId,
                "Digital Asset Updated: " + book.getTitle(),
                "A new " + assetType + " asset (Version " + nextVersion + ", " + originalFilename + ") was uploaded for '" + book.getTitle() + "' by " + (currentUserEmail != null ? currentUserEmail : "system") + ".",
                com.library.entity.NotificationType.ASSET_UPDATED,
                com.library.entity.NotificationPriority.LOW,
                null,
                null
        );

        return mapEntityToResponse(savedAsset);
    }

    @Override
    public DigitalAssetResponse getAssetById(Long assetId) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));
        return mapEntityToResponse(asset);
    }

    @Override
    public List<DigitalAssetResponse> getAssetsByBookId(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
        return digitalAssetRepository.findByBookIdOrderByVersionDesc(bookId).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<DigitalAssetResponse> getAllAssets(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<DigitalAsset> assetPage = digitalAssetRepository.findAll(pageable);
        return mapPageToResponse(assetPage);
    }

    @Override
    public PageResponse<DigitalAssetResponse> searchAssets(String query, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        if (query == null || query.trim().isEmpty()) {
            return getAllAssets(page, size, sortBy, sortDir);
        }
        Page<DigitalAsset> assetPage = digitalAssetRepository.searchAssets(query.trim(), pageable);
        return mapPageToResponse(assetPage);
    }

    @Override
    public PageResponse<DigitalAssetResponse> filterAssets(String assetTypeStr, String statusStr, String mimeType, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Specification<DigitalAsset> spec = Specification.where(null);

        if (assetTypeStr != null && !assetTypeStr.trim().isEmpty()) {
            try {
                AssetType assetType = AssetType.valueOf(assetTypeStr.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("assetType"), assetType));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid asset type: " + assetTypeStr);
            }
        }

        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                AssetStatus status = AssetStatus.valueOf(statusStr.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid asset status: " + statusStr);
            }
        }

        if (mimeType != null && !mimeType.trim().isEmpty()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("mimeType")), "%" + mimeType.trim().toLowerCase() + "%"));
        }

        Page<DigitalAsset> assetPage = digitalAssetRepository.findAll(spec, pageable);
        return mapPageToResponse(assetPage);
    }

    @Override
    @Transactional
    public DigitalAssetResponse archiveAsset(Long assetId, String currentUserEmail) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));

        asset.setStatus(AssetStatus.ARCHIVED);
        DigitalAsset saved = digitalAssetRepository.save(asset);
        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional
    public DigitalAssetResponse restoreAsset(Long assetId, String currentUserEmail) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));

        // Archive any current active asset for same book and asset type
        Optional<DigitalAsset> currentActiveOpt = digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(
                asset.getBook().getId(), asset.getAssetType(), AssetStatus.ACTIVE);
        currentActiveOpt.ifPresent(current -> {
            current.setStatus(AssetStatus.ARCHIVED);
            digitalAssetRepository.save(current);
        });

        asset.setStatus(AssetStatus.ACTIVE);
        DigitalAsset saved = digitalAssetRepository.save(asset);
        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAsset(Long assetId, boolean permanent) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));

        if (permanent) {
            fileStorageService.deleteFile(asset.getFilePath());
            digitalAssetRepository.delete(asset);
        } else {
            asset.setStatus(AssetStatus.DELETED);
            digitalAssetRepository.save(asset);
        }
    }

    @Override
    public Resource downloadAssetFile(Long assetId) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));

        if (asset.getStatus() == AssetStatus.DELETED) {
            throw new BadRequestException("Asset has been deleted.");
        }

        // Enforce Version 4 License check for full EBOOK downloads
        if (asset.getAssetType() == AssetType.EBOOK) {
            licenseService.validateBookDownloadLicense(asset.getBook().getId());
        }

        return fileStorageService.loadFileAsResource(asset.getFilePath());
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<DigitalAssetResponse> mapPageToResponse(Page<DigitalAsset> assetPage) {
        List<DigitalAssetResponse> content = assetPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                assetPage.getNumber(),
                assetPage.getSize(),
                assetPage.getTotalElements(),
                assetPage.getTotalPages(),
                assetPage.isLast()
        );
    }

    private void validateAssetFormat(AssetType type, String extension, String mimeType) {
        List<String> validEbookExts = Arrays.asList("pdf", "epub");
        List<String> validCoverExts = Arrays.asList("jpg", "jpeg", "png", "webp");

        if (type == AssetType.EBOOK) {
            if (!validEbookExts.contains(extension)) {
                throw new BadRequestException("Invalid file format for E-Book. Allowed formats: .pdf, .epub");
            }
        } else if (type == AssetType.COVER_IMAGE) {
            if (!validCoverExts.contains(extension)) {
                throw new BadRequestException("Invalid file format for Cover Image. Allowed formats: .jpg, .png, .webp");
            }
        } else if (type == AssetType.PREVIEW) {
            if (!validEbookExts.contains(extension) && !validCoverExts.contains(extension)) {
                throw new BadRequestException("Invalid file format for Preview. Allowed formats: .pdf, .epub, .jpg, .png, .webp");
            }
        }
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i + 1).toLowerCase() : "";
    }

    private String getSubDirectoryName(AssetType type) {
        switch (type) {
            case EBOOK: return "ebooks";
            case COVER_IMAGE: return "covers";
            case PREVIEW: return "previews";
            default: return "misc";
        }
    }

    private DigitalAssetResponse mapEntityToResponse(DigitalAsset asset) {
        DigitalAssetResponse res = new DigitalAssetResponse();
        res.setId(asset.getId());
        res.setBookId(asset.getBook().getId());
        res.setBookTitle(asset.getBook().getTitle());
        res.setAssetType(asset.getAssetType());
        res.setOriginalFileName(asset.getOriginalFileName());
        res.setStoredFileName(asset.getStoredFileName());
        res.setFilePath(asset.getFilePath());
        res.setMimeType(asset.getMimeType());
        res.setFileExtension(asset.getFileExtension());
        res.setFileSize(asset.getFileSize());
        res.setChecksum(asset.getChecksum());
        res.setVersion(asset.getVersion());
        res.setStatus(asset.getStatus());
        res.setUploadedBy(asset.getUploadedBy());
        res.setCreatedAt(asset.getCreatedAt());
        res.setUpdatedAt(asset.getUpdatedAt());
        return res;
    }
}
