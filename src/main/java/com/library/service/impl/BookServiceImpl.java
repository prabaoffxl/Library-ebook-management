package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.dto.PageResponse;
import com.library.entity.Book;
import com.library.entity.BookStatus;
import com.library.exception.BadRequestException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import com.library.service.FileStorageService;
import com.library.service.LicenseService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final FileStorageService fileStorageService;
    private final LicenseService licenseService;
    private final com.library.service.DigitalAssetService digitalAssetService;

    public BookServiceImpl(BookRepository bookRepository,
                           FileStorageService fileStorageService,
                           LicenseService licenseService,
                           com.library.service.DigitalAssetService digitalAssetService) {
        this.bookRepository = bookRepository;
        this.fileStorageService = fileStorageService;
        this.licenseService = licenseService;
        this.digitalAssetService = digitalAssetService;
    }

    @Override
    @Transactional
    public BookDTO createBook(BookDTO dto, String currentUserEmail) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new DuplicateResourceException("Book with ISBN " + dto.getIsbn() + " already exists");
        }

        Book book = new Book();
        mapDtoToEntity(dto, book);
        book.setCreatedBy(currentUserEmail);
        book.setUpdatedBy(currentUserEmail);

        Book savedBook = bookRepository.save(book);
        return mapEntityToDto(savedBook);
    }

    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapEntityToDto(book);
    }

    @Override
    public PageResponse<BookDTO> getAllBooks(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return mapPageToResponse(bookPage);
    }

    @Override
    public PageResponse<BookDTO> searchBooks(String query, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks(page, size, sortBy, sortDir);
        }
        Page<Book> bookPage = bookRepository.searchBooks(query.trim(), pageable);
        return mapPageToResponse(bookPage);
    }

    @Override
    public PageResponse<BookDTO> filterBooks(String category, String language, String status, String author, String publisher, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Specification<Book> spec = Specification.where(null);

        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
        }
        if (language != null && !language.trim().isEmpty()) {
            spec = spec.and((root, q, cb) -> cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase()));
        }
        if (author != null && !author.trim().isEmpty()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("author")), "%" + author.trim().toLowerCase() + "%"));
        }
        if (publisher != null && !publisher.trim().isEmpty()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("publisher")), "%" + publisher.trim().toLowerCase() + "%"));
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                BookStatus bookStatus = BookStatus.valueOf(status.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), bookStatus));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid book status: " + status);
            }
        }

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        return mapPageToResponse(bookPage);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long id, BookDTO dto, String currentUserEmail) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (!book.getIsbn().equalsIgnoreCase(dto.getIsbn()) && bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new DuplicateResourceException("Book with ISBN " + dto.getIsbn() + " already exists");
        }

        mapDtoToEntity(dto, book);
        book.setUpdatedBy(currentUserEmail);

        Book updatedBook = bookRepository.save(book);
        return mapEntityToDto(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (book.getFilePath() != null) {
            fileStorageService.deleteFile(book.getFilePath());
        }
        if (book.getCoverImage() != null) {
            fileStorageService.deleteFile(book.getCoverImage());
        }

        bookRepository.delete(book);
    }

    @Override
    @Transactional
    public BookDTO uploadEbookFile(Long bookId, MultipartFile file) {
        digitalAssetService.uploadAsset(bookId, com.library.entity.AssetType.EBOOK, file, "system");
        return getBookById(bookId);
    }

    @Override
    @Transactional
    public BookDTO uploadCoverImage(Long bookId, MultipartFile file) {
        digitalAssetService.uploadCover(bookId, file, "system");
        return getBookById(bookId);
    }

    @Override
    public Resource downloadEbookFile(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BadRequestException("Book is currently unavailable for download (Status: " + book.getStatus() + ")");
        }

        if (book.getFilePath() == null || book.getFilePath().trim().isEmpty()) {
            throw new ResourceNotFoundException("E-Book file has not been uploaded for book: " + book.getTitle());
        }

        // Validate license status
        licenseService.validateBookDownloadLicense(bookId);

        return fileStorageService.loadFileAsResource(book.getFilePath());
    }

    @Override
    public Resource loadCoverImageResource(String filename) {
        return fileStorageService.loadFileAsResource("storage/covers/" + filename);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookDTO> mapPageToResponse(Page<Book> bookPage) {
        List<BookDTO> content = bookPage.getContent().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isLast()
        );
    }

    private void mapDtoToEntity(BookDTO dto, Book book) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setCategory(dto.getCategory());
        book.setPublisher(dto.getPublisher());
        book.setPublicationYear(dto.getPublicationYear());
        book.setLanguage(dto.getLanguage());
        book.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            book.setStatus(dto.getStatus());
        }
    }

    private BookDTO mapEntityToDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setLanguage(book.getLanguage());
        dto.setDescription(book.getDescription());
        dto.setFileName(book.getFileName());
        dto.setFilePath(book.getFilePath());
        dto.setFileSize(book.getFileSize());
        dto.setFileType(book.getFileType());
        dto.setCoverImage(book.getCoverImage());
        dto.setStatus(book.getStatus());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }
}
