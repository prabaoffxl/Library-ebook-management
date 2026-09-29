package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.AssetIntegrityResponse;
import com.library.dto.DigitalAssetResponse;
import com.library.dto.PageResponse;
import com.library.entity.AssetType;
import com.library.service.AssetIntegrityService;
import com.library.service.DigitalAssetService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class DigitalAssetController {

    private final DigitalAssetService digitalAssetService;
    private final AssetIntegrityService assetIntegrityService;

    public DigitalAssetController(DigitalAssetService digitalAssetService, AssetIntegrityService assetIntegrityService) {
        this.digitalAssetService = digitalAssetService;
        this.assetIntegrityService = assetIntegrityService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> uploadAsset(
            @RequestParam("bookId") Long bookId,
            @RequestParam("type") AssetType type,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.uploadAsset(bookId, type, file, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Digital asset uploaded successfully", response));
    }

    @PostMapping("/replace-ebook/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> replaceEbook(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.replaceEbook(bookId, file, userEmail);
        return ResponseEntity.ok(ApiResponse.success("E-Book asset replaced successfully", response));
    }

    @PostMapping("/upload-cover/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> uploadCover(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.uploadCover(bookId, file, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Cover asset uploaded successfully", response));
    }

    @PostMapping("/upload-preview/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> uploadPreview(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.uploadPreview(bookId, file, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Preview asset uploaded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> getAssetById(@PathVariable Long id) {
        DigitalAssetResponse response = digitalAssetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success("Digital asset retrieved successfully", response));
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<List<DigitalAssetResponse>>> getAssetsByBookId(@PathVariable Long bookId) {
        List<DigitalAssetResponse> responses = digitalAssetService.getAssetsByBookId(bookId);
        return ResponseEntity.ok(ApiResponse.success("Book assets retrieved successfully", responses));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<DigitalAssetResponse>>> getAllAssets(
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String mimeType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponse<DigitalAssetResponse> responses;
        if ((assetType != null && !assetType.trim().isEmpty()) ||
            (status != null && !status.trim().isEmpty()) ||
            (mimeType != null && !mimeType.trim().isEmpty())) {
            responses = digitalAssetService.filterAssets(assetType, status, mimeType, page, size, sortBy, sortDir);
        } else {
            responses = digitalAssetService.getAllAssets(page, size, sortBy, sortDir);
        }
        return ResponseEntity.ok(ApiResponse.success("Digital assets retrieved successfully", responses));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<PageResponse<DigitalAssetResponse>>> searchAssets(
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false, name = "query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String searchTerm = (q != null && !q.trim().isEmpty()) ? q : query;
        PageResponse<DigitalAssetResponse> responses = digitalAssetService.searchAssets(searchTerm, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", responses));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> archiveAsset(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.archiveAsset(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Digital asset archived successfully", response));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> restoreAsset(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        DigitalAssetResponse response = digitalAssetService.restoreAsset(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Digital asset restored successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean permanent
    ) {
        digitalAssetService.deleteAsset(id, permanent);
        return ResponseEntity.ok(ApiResponse.success("Digital asset deleted successfully"));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<Resource> downloadAssetFile(@PathVariable Long id) {
        Resource resource = digitalAssetService.downloadAssetFile(id);
        DigitalAssetResponse asset = digitalAssetService.getAssetById(id);

        String contentType = asset.getMimeType() != null ? asset.getMimeType() : "application/octet-stream";
        String downloadFilename = asset.getOriginalFileName() != null ? asset.getOriginalFileName() : asset.getStoredFileName();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/integrity")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<AssetIntegrityResponse>> verifyAssetIntegrity(@PathVariable Long id) {
        AssetIntegrityResponse response = assetIntegrityService.verifyAssetIntegrity(id);
        return ResponseEntity.ok(ApiResponse.success("Asset integrity verification completed", response));
    }
}
