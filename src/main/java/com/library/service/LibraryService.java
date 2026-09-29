package com.library.service;

import com.library.dto.*;

import java.util.List;

public interface LibraryService {
    LibraryResponse createLibrary(LibraryRequest request, String currentUserEmail);
    LibraryResponse updateLibrary(Long id, LibraryRequest request, String currentUserEmail);
    LibraryResponse getLibraryById(Long id);
    PageResponse<LibraryResponse> getAllLibraries(int page, int size, String sortBy, String sortDir);
    PageResponse<LibraryResponse> getActiveLibraries(int page, int size);
    PageResponse<LibraryResponse> searchLibraries(String query, int page, int size, String sortBy, String sortDir);
    void deleteLibrary(Long id);

    LibraryBookResponse assignBookToLibrary(Long libraryId, Long bookId, String currentUserEmail);
    void removeBookFromLibrary(Long libraryId, Long bookId, String currentUserEmail);
    PageResponse<LibraryBookResponse> getLibraryBooks(Long libraryId, String query, String status, int page, int size, String sortBy, String sortDir);

    LibrarianAssignmentResponse assignLibrarian(Long libraryId, Long userId, String currentUserEmail);
    void removeLibrarian(Long libraryId, Long userId, String currentUserEmail);
    List<LibrarianAssignmentResponse> getLibrariansByLibrary(Long libraryId);

    boolean canLibrarianManageLibrary(String userEmail, Long libraryId);
}
