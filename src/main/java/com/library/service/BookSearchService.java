package com.library.service;

import com.library.dto.search.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookSearchService {

    Page<BookSearchResultDTO> searchBooks(BookSearchCriteriaDTO criteria, Pageable pageable, String currentUserEmail);

    List<String> getSearchSuggestions(String query);

    List<Object[]> getCategoriesWithCounts();

    List<Object[]> getLanguagesWithCounts();

    Page<AuthorDiscoveryDTO> getAuthorsWithCounts(String query, Pageable pageable);

    List<BookLibraryLocationDTO> getBookLibraryLocations(Long bookId);

    List<BookSearchResultDTO> getPopularBooks();

    List<Object[]> getPopularCategories();

    List<AuthorDiscoveryDTO> getPopularAuthors();

    Page<SearchHistoryResponseDTO> getUserSearchHistory(String currentUserEmail, Pageable pageable);

    void clearUserSearchHistory(String currentUserEmail);
}
