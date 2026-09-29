package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.PageResponse;
import com.library.dto.search.*;
import com.library.service.BookSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class BookSearchController {

    private final BookSearchService bookSearchService;

    @Autowired
    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    @GetMapping("/api/books/search/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(@RequestParam(required = false) String query) {
        List<String> suggestions = bookSearchService.getSearchSuggestions(query);
        return ResponseEntity.ok(ApiResponse.success("Search suggestions retrieved successfully", suggestions));
    }

    @GetMapping("/api/books/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategories() {
        List<Object[]> data = bookSearchService.getCategoriesWithCounts();
        List<Map<String, Object>> list = data.stream().map(row -> Map.<String, Object>of(
                "category", row[0] != null ? row[0] : "Uncategorized",
                "count", row[1]
        )).toList();
        return ResponseEntity.ok(ApiResponse.success("Categories with counts retrieved successfully", list));
    }

    @GetMapping("/api/books/languages")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLanguages() {
        List<Object[]> data = bookSearchService.getLanguagesWithCounts();
        List<Map<String, Object>> list = data.stream().map(row -> Map.<String, Object>of(
                "language", row[0] != null ? row[0] : "Unknown",
                "count", row[1]
        )).toList();
        return ResponseEntity.ok(ApiResponse.success("Languages with counts retrieved successfully", list));
    }

    @GetMapping("/api/books/authors")
    public ResponseEntity<ApiResponse<PageResponse<AuthorDiscoveryDTO>>> getAuthors(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuthorDiscoveryDTO> authorsPage = bookSearchService.getAuthorsWithCounts(query, pageable);
        PageResponse<AuthorDiscoveryDTO> response = new PageResponse<>(
                authorsPage.getContent(),
                authorsPage.getNumber(),
                authorsPage.getSize(),
                authorsPage.getTotalElements(),
                authorsPage.getTotalPages(),
                authorsPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Author discovery retrieved successfully", response));
    }

    @GetMapping("/api/books/{bookId}/libraries")
    public ResponseEntity<ApiResponse<List<BookLibraryLocationDTO>>> getBookLibraries(@PathVariable Long bookId) {
        List<BookLibraryLocationDTO> locations = bookSearchService.getBookLibraryLocations(bookId);
        return ResponseEntity.ok(ApiResponse.success("Book library locations retrieved successfully", locations));
    }

    @GetMapping("/api/books/popular")
    public ResponseEntity<ApiResponse<List<BookSearchResultDTO>>> getPopularBooks() {
        List<BookSearchResultDTO> popular = bookSearchService.getPopularBooks();
        return ResponseEntity.ok(ApiResponse.success("Popular books retrieved successfully", popular));
    }

    @GetMapping("/api/books/popular/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularCategories() {
        List<Object[]> data = bookSearchService.getPopularCategories();
        List<Map<String, Object>> list = data.stream().map(row -> Map.<String, Object>of(
                "category", row[0] != null ? row[0] : "Uncategorized",
                "count", row[1]
        )).toList();
        return ResponseEntity.ok(ApiResponse.success("Popular categories retrieved successfully", list));
    }

    @GetMapping("/api/books/popular/authors")
    public ResponseEntity<ApiResponse<List<AuthorDiscoveryDTO>>> getPopularAuthors() {
        List<AuthorDiscoveryDTO> popular = bookSearchService.getPopularAuthors();
        return ResponseEntity.ok(ApiResponse.success("Popular authors retrieved successfully", popular));
    }

    @GetMapping("/api/search/history")
    public ResponseEntity<ApiResponse<PageResponse<SearchHistoryResponseDTO>>> getUserSearchHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = authentication != null ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "searchedAt"));
        Page<SearchHistoryResponseDTO> historyPage = bookSearchService.getUserSearchHistory(email, pageable);
        PageResponse<SearchHistoryResponseDTO> response = new PageResponse<>(
                historyPage.getContent(),
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                historyPage.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("User search history retrieved successfully", response));
    }

    @DeleteMapping("/api/search/history")
    public ResponseEntity<ApiResponse<Void>> clearUserSearchHistory(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        bookSearchService.clearUserSearchHistory(email);
        return ResponseEntity.ok(ApiResponse.success("Search history cleared successfully"));
    }
}
