package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.BookDTO;
import com.library.dto.PageResponse;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.library.dto.search.BookSearchCriteriaDTO;
import com.library.dto.search.BookSearchResultDTO;
import com.library.service.BookSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookSearchService bookSearchService;

    public BookController(BookService bookService, BookSearchService bookSearchService) {
        this.bookService = bookService;
        this.bookSearchService = bookSearchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<BookDTO> books = bookService.getAllBooks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<BookSearchResultDTO>>> searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer publicationYear,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long libraryId,
            @RequestParam(required = false) Boolean licenseAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication
    ) {
        BookSearchCriteriaDTO criteria = new BookSearchCriteriaDTO();
        criteria.setQuery(query);
        criteria.setCategory(category);
        criteria.setAuthor(author);
        criteria.setLanguage(language);
        criteria.setPublisher(publisher);
        criteria.setPublicationYear(publicationYear);
        criteria.setStatus(status);
        criteria.setLibraryId(libraryId);
        criteria.setLicenseAvailable(licenseAvailable);

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        String currentUserEmail = authentication != null ? authentication.getName() : null;
        Page<BookSearchResultDTO> resultPage = bookSearchService.searchBooks(criteria, pageable, currentUserEmail);

        PageResponse<BookSearchResultDTO> response = new PageResponse<>(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<BookDTO>>> filterBooks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<BookDTO> books = bookService.filterBooks(category, language, status, author, publisher, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Filtered books retrieved successfully", books));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookDTO dto, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        BookDTO createdBook = bookService.createBook(dto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", createdBook));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO dto, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "system";
        BookDTO updatedBook = bookService.updateBook(id, dto, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updatedBook));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
    }

    @PostMapping("/{id}/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookDTO>> uploadEbookFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        BookDTO updatedBook = bookService.uploadEbookFile(id, file);
        return ResponseEntity.ok(ApiResponse.success("E-Book file uploaded successfully", updatedBook));
    }

    @PostMapping("/{id}/cover")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BookDTO>> uploadCoverImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        BookDTO updatedBook = bookService.uploadCoverImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Cover image uploaded successfully", updatedBook));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadEbookFile(@PathVariable Long id) {
        Resource resource = bookService.downloadEbookFile(id);
        BookDTO book = bookService.getBookById(id);

        String contentType = "application/octet-stream";
        if (book.getFileType() != null && book.getFileType().equalsIgnoreCase("PDF")) {
            contentType = "application/pdf";
        } else if (book.getFileType() != null && book.getFileType().equalsIgnoreCase("EPUB")) {
            contentType = "application/epub+zip";
        }

        String downloadFilename = book.getFileName() != null ? book.getFileName() : "ebook_" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                .body(resource);
    }

    @GetMapping("/cover/{filename:.+}")
    public ResponseEntity<Resource> getCoverImage(@PathVariable String filename) {
        Resource resource = bookService.loadCoverImageResource(filename);
        String contentType = "image/jpeg";
        if (filename.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.toLowerCase().endsWith(".webp")) {
            contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
