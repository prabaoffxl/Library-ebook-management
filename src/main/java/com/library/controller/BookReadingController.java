package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.reader.ReadingProgressDTO;
import com.library.dto.reader.ReadingProgressRequest;
import com.library.service.BookReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class BookReadingController {

    private final BookReadingService bookReadingService;

    @Autowired
    public BookReadingController(BookReadingService bookReadingService) {
        this.bookReadingService = bookReadingService;
    }

    @GetMapping("/api/books/{bookId}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<Resource> readEbook(
            @PathVariable Long bookId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return bookReadingService.streamEbook(bookId, rangeHeader, userEmail);
    }

    @GetMapping("/api/books/{bookId}/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<Resource> previewEbook(
            @PathVariable Long bookId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return bookReadingService.streamPreview(bookId, rangeHeader, userEmail);
    }

    @GetMapping("/api/books/{bookId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<ReadingProgressDTO>> getReadingProgress(
            @PathVariable Long bookId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ReadingProgressDTO progress = bookReadingService.getReadingProgress(bookId, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Reading progress retrieved successfully", progress));
    }

    @PutMapping("/api/books/{bookId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<ReadingProgressDTO>> updateReadingProgress(
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingProgressRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ReadingProgressDTO progress = bookReadingService.updateReadingProgress(bookId, request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Reading progress updated successfully", progress));
    }

    @GetMapping("/api/users/me/reading-progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'USER')")
    public ResponseEntity<ApiResponse<List<ReadingProgressDTO>>> getUserReadingList(Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<ReadingProgressDTO> readingList = bookReadingService.getUserReadingList(userEmail);
        return ResponseEntity.ok(ApiResponse.success("User reading list retrieved successfully", readingList));
    }
}
