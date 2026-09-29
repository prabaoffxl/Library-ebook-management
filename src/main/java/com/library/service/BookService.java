package com.library.service;

import com.library.dto.BookDTO;
import com.library.dto.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    BookDTO createBook(BookDTO dto, String currentUserEmail);
    BookDTO getBookById(Long id);
    PageResponse<BookDTO> getAllBooks(int page, int size, String sortBy, String sortDir);
    PageResponse<BookDTO> searchBooks(String query, int page, int size, String sortBy, String sortDir);
    PageResponse<BookDTO> filterBooks(String category, String language, String status, String author, String publisher, int page, int size, String sortBy, String sortDir);
    BookDTO updateBook(Long id, BookDTO dto, String currentUserEmail);
    void deleteBook(Long id);
    BookDTO uploadEbookFile(Long bookId, MultipartFile file);
    BookDTO uploadCoverImage(Long bookId, MultipartFile file);
    Resource downloadEbookFile(Long bookId);
    Resource loadCoverImageResource(String filename);
}
