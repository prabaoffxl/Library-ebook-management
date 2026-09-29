package com.library.controller;

import com.library.dto.*;
import com.library.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getAllLicenses() {
        List<LicenseResponse> licenses = licenseService.getAllLicenses();
        return ResponseEntity.ok(ApiResponse.success("Licenses retrieved successfully", licenses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> getLicenseById(@PathVariable Long id) {
        LicenseResponse license = licenseService.getLicenseById(id);
        return ResponseEntity.ok(ApiResponse.success("License retrieved successfully", license));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> searchLicenses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String licenseType,
            @RequestParam(required = false) String bookTitle
    ) {
        List<LicenseResponse> licenses = licenseService.searchLicenses(query, provider, status, licenseType, bookTitle);
        return ResponseEntity.ok(ApiResponse.success("License search results retrieved", licenses));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> filterLicenses(@RequestParam(required = false) String status) {
        List<LicenseResponse> licenses = licenseService.filterLicenses(status);
        return ResponseEntity.ok(ApiResponse.success("Filtered licenses retrieved", licenses));
    }

    @GetMapping("/book/{bookId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<List<LicenseResponse>>> getLicenseHistory(@PathVariable Long bookId) {
        List<LicenseResponse> history = licenseService.getLicenseHistory(bookId);
        return ResponseEntity.ok(ApiResponse.success("Book license history retrieved", history));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> createLicense(@Valid @RequestBody LicenseRequest request) {
        LicenseResponse createdLicense = licenseService.createLicense(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("License created successfully", createdLicense));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> updateLicense(@PathVariable Long id, @Valid @RequestBody LicenseRequest request) {
        LicenseResponse updatedLicense = licenseService.updateLicense(id, request);
        return ResponseEntity.ok(ApiResponse.success("License updated successfully", updatedLicense));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> renewLicense(@PathVariable Long id, @Valid @RequestBody LicenseRenewRequest request) {
        LicenseResponse renewedLicense = licenseService.renewLicense(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("License renewed successfully", renewedLicense));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> suspendLicense(@PathVariable Long id, @RequestBody(required = false) LicenseSuspendRequest request) {
        LicenseResponse suspendedLicense = licenseService.suspendLicense(id, request);
        return ResponseEntity.ok(ApiResponse.success("License suspended successfully", suspendedLicense));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LicenseResponse>> cancelLicense(@PathVariable Long id) {
        LicenseResponse cancelledLicense = licenseService.cancelLicense(id);
        return ResponseEntity.ok(ApiResponse.success("License cancelled successfully", cancelledLicense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLicense(@PathVariable Long id) {
        licenseService.deleteLicense(id);
        return ResponseEntity.ok(ApiResponse.success("License deleted successfully"));
    }
}
