package com.library.controller;

import com.library.dto.*;
import com.library.exception.UnauthorizedAccessException;
import com.library.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libraries")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> getAllLibraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<LibraryResponse> response = libraryService.getAllLibraries(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Libraries retrieved successfully", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> getActiveLibraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<LibraryResponse> response = libraryService.getActiveLibraries(page, size);
        return ResponseEntity.ok(ApiResponse.success("Active libraries retrieved successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> searchLibraries(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<LibraryResponse> response = libraryService.searchLibraries(query, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryResponse>> getLibraryById(@PathVariable Long id) {
        LibraryResponse library = libraryService.getLibraryById(id);
        return ResponseEntity.ok(ApiResponse.success("Library details retrieved successfully", library));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LibraryResponse>> createLibrary(
            @Valid @RequestBody LibraryRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        LibraryResponse response = libraryService.createLibrary(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Library created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LibraryResponse>> updateLibrary(
            @PathVariable Long id,
            @Valid @RequestBody LibraryRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        verifyLibrarianBranchAccess(userEmail, id, authentication);

        LibraryResponse response = libraryService.updateLibrary(id, request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Library updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLibrary(@PathVariable Long id) {
        libraryService.deleteLibrary(id);
        return ResponseEntity.ok(ApiResponse.success("Library deleted successfully"));
    }

    @PostMapping("/{libraryId}/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<LibraryBookResponse>> assignBookToLibrary(
            @PathVariable Long libraryId,
            @PathVariable Long bookId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        verifyLibrarianBranchAccess(userEmail, libraryId, authentication);

        LibraryBookResponse response = libraryService.assignBookToLibrary(libraryId, bookId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book assigned to library successfully", response));
    }

    @DeleteMapping("/{libraryId}/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Void>> removeBookFromLibrary(
            @PathVariable Long libraryId,
            @PathVariable Long bookId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        verifyLibrarianBranchAccess(userEmail, libraryId, authentication);

        libraryService.removeBookFromLibrary(libraryId, bookId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Book removed from library successfully"));
    }

    @GetMapping("/{libraryId}/books")
    public ResponseEntity<ApiResponse<PageResponse<LibraryBookResponse>>> getLibraryBooks(
            @PathVariable Long libraryId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageResponse<LibraryBookResponse> response = libraryService.getLibraryBooks(libraryId, query, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Library books retrieved successfully", response));
    }

    @PostMapping("/{libraryId}/librarians/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LibrarianAssignmentResponse>> assignLibrarian(
            @PathVariable Long libraryId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        LibrarianAssignmentResponse response = libraryService.assignLibrarian(libraryId, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Librarian assigned to library successfully", response));
    }

    @DeleteMapping("/{libraryId}/librarians/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeLibrarian(
            @PathVariable Long libraryId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        libraryService.removeLibrarian(libraryId, userId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Librarian assignment removed successfully"));
    }

    @GetMapping("/{libraryId}/librarians")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<List<LibrarianAssignmentResponse>>> getLibrariansByLibrary(@PathVariable Long libraryId) {
        List<LibrarianAssignmentResponse> responses = libraryService.getLibrariansByLibrary(libraryId);
        return ResponseEntity.ok(ApiResponse.success("Assigned librarians retrieved successfully", responses));
    }

    private void verifyLibrarianBranchAccess(String userEmail, Long libraryId, Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return; // Admin has universal access
        }
        if (!libraryService.canLibrarianManageLibrary(userEmail, libraryId)) {
            throw new UnauthorizedAccessException("Access denied: You are not assigned to manage library branch #" + libraryId);
        }
    }
}
