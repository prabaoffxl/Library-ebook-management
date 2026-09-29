package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.BookService;
import com.library.service.LibraryService;
import com.library.service.NotificationService;
import com.library.service.UserActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final LibrarianAssignmentRepository librarianAssignmentRepository;
    private final UserActivityService userActivityService;
    private final NotificationService notificationService;
    private final BookService bookService;

    public LibraryServiceImpl(LibraryRepository libraryRepository,
                              BookRepository bookRepository,
                              LibraryBookRepository libraryBookRepository,
                              UserRepository userRepository,
                              LibrarianAssignmentRepository librarianAssignmentRepository,
                              UserActivityService userActivityService,
                              NotificationService notificationService,
                              BookService bookService) {
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.userRepository = userRepository;
        this.librarianAssignmentRepository = librarianAssignmentRepository;
        this.userActivityService = userActivityService;
        this.notificationService = notificationService;
        this.bookService = bookService;
    }

    @Override
    @Transactional
    public LibraryResponse createLibrary(LibraryRequest request, String currentUserEmail) {
        if (libraryRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Library with code '" + request.getCode() + "' already exists.");
        }

        Library library = new Library();
        mapRequestToEntity(request, library);
        Library saved = libraryRepository.save(library);

        userActivityService.logActivity(currentUserEmail, ActivityType.SEARCH, null, saved.getId(), "Created new library branch: " + saved.getName());

        return mapEntityToResponse(saved);
    }

    @Override
    @Transactional
    public LibraryResponse updateLibrary(Long id, LibraryRequest request, String currentUserEmail) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));

        if (!library.getCode().equalsIgnoreCase(request.getCode()) && libraryRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Library with code '" + request.getCode() + "' already exists.");
        }

        mapRequestToEntity(request, library);
        Library saved = libraryRepository.save(library);

        userActivityService.logActivity(currentUserEmail, ActivityType.PROFILE_UPDATE, null, saved.getId(), "Updated library details: " + saved.getName());

        return mapEntityToResponse(saved);
    }

    @Override
    public LibraryResponse getLibraryById(Long id) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));
        return mapEntityToResponse(library);
    }

    @Override
    public PageResponse<LibraryResponse> getAllLibraries(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Library> libraryPage = libraryRepository.findAll(pageable);
        return mapPageToResponse(libraryPage);
    }

    @Override
    public PageResponse<LibraryResponse> getActiveLibraries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Specification<Library> spec = (root, q, cb) -> cb.equal(root.get("status"), LibraryStatus.ACTIVE);
        Page<Library> libraryPage = libraryRepository.findAll(spec, pageable);
        return mapPageToResponse(libraryPage);
    }

    @Override
    public PageResponse<LibraryResponse> searchLibraries(String query, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        if (query == null || query.trim().isEmpty()) {
            return getAllLibraries(page, size, sortBy, sortDir);
        }
        Page<Library> libraryPage = libraryRepository.searchLibraries(query.trim(), pageable);
        return mapPageToResponse(libraryPage);
    }

    @Override
    @Transactional
    public void deleteLibrary(Long id) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + id));
        libraryRepository.delete(library);
    }

    @Override
    @Transactional
    public LibraryBookResponse assignBookToLibrary(Long libraryId, Long bookId, String currentUserEmail) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + libraryId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (libraryBookRepository.existsByLibraryIdAndBookId(libraryId, bookId)) {
            throw new DuplicateResourceException("Book '" + book.getTitle() + "' is already assigned to library '" + library.getName() + "'.");
        }

        LibraryBook libraryBook = new LibraryBook(library, book, LibraryBookStatus.AVAILABLE);
        LibraryBook saved = libraryBookRepository.save(libraryBook);

        userActivityService.logActivity(currentUserEmail, ActivityType.BOOK_VIEW, bookId, libraryId, "Assigned book to library " + library.getName());

        return mapLibraryBookToResponse(saved);
    }

    @Override
    @Transactional
    public void removeBookFromLibrary(Long libraryId, Long bookId, String currentUserEmail) {
        if (!libraryBookRepository.existsByLibraryIdAndBookId(libraryId, bookId)) {
            throw new ResourceNotFoundException("Book is not assigned to this library.");
        }
        libraryBookRepository.deleteByLibraryIdAndBookId(libraryId, bookId);

        userActivityService.logActivity(currentUserEmail, ActivityType.BOOK_VIEW, bookId, libraryId, "Removed book from library #" + libraryId);
    }

    @Override
    public PageResponse<LibraryBookResponse> getLibraryBooks(Long libraryId, String query, String statusStr, int page, int size, String sortBy, String sortDir) {
        if (!libraryRepository.existsById(libraryId)) {
            throw new ResourceNotFoundException("Library not found with id: " + libraryId);
        }

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<LibraryBook> lbPage;

        if (query != null && !query.trim().isEmpty()) {
            lbPage = libraryBookRepository.searchLibraryBooks(libraryId, query.trim(), pageable);
        } else if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                LibraryBookStatus status = LibraryBookStatus.valueOf(statusStr.trim().toUpperCase());
                lbPage = libraryBookRepository.findByLibraryIdAndAvailabilityStatus(libraryId, status, pageable);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid availability status: " + statusStr);
            }
        } else {
            lbPage = libraryBookRepository.findByLibraryId(libraryId, pageable);
        }

        List<LibraryBookResponse> content = lbPage.getContent().stream()
                .map(this::mapLibraryBookToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                lbPage.getNumber(),
                lbPage.getSize(),
                lbPage.getTotalElements(),
                lbPage.getTotalPages(),
                lbPage.isLast()
        );
    }

    @Override
    @Transactional
    public LibrarianAssignmentResponse assignLibrarian(Long libraryId, Long userId, String currentUserEmail) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found with id: " + libraryId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.LIBRARIAN && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("User must have LIBRARIAN or ADMIN role to be assigned to a library.");
        }

        Optional<LibrarianAssignment> existingOpt = librarianAssignmentRepository.findByUserIdAndLibraryId(userId, libraryId);
        LibrarianAssignment assignment;

        if (existingOpt.isPresent()) {
            assignment = existingOpt.get();
            assignment.setStatus(LibrarianAssignmentStatus.ACTIVE);
        } else {
            assignment = new LibrarianAssignment(user, library, LibrarianAssignmentStatus.ACTIVE);
        }

        LibrarianAssignment saved = librarianAssignmentRepository.save(assignment);

        notificationService.createNotification(
                user.getId(),
                "Assigned to Library Branch",
                "You have been assigned as a librarian for " + library.getName() + " (" + library.getCode() + ").",
                NotificationType.SYSTEM
        );

        userActivityService.logActivity(currentUserEmail, ActivityType.SEARCH, null, libraryId, "Assigned librarian " + user.getEmail() + " to library " + library.getName());

        return mapAssignmentToResponse(saved);
    }

    @Override
    @Transactional
    public void removeLibrarian(Long libraryId, Long userId, String currentUserEmail) {
        LibrarianAssignment assignment = librarianAssignmentRepository.findByUserIdAndLibraryId(userId, libraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian assignment not found for user " + userId + " and library " + libraryId));

        assignment.setStatus(LibrarianAssignmentStatus.INACTIVE);
        librarianAssignmentRepository.save(assignment);

        userActivityService.logActivity(currentUserEmail, ActivityType.SEARCH, null, libraryId, "Removed librarian #" + userId + " assignment from library #" + libraryId);
    }

    @Override
    public List<LibrarianAssignmentResponse> getLibrariansByLibrary(Long libraryId) {
        if (!libraryRepository.existsById(libraryId)) {
            throw new ResourceNotFoundException("Library not found with id: " + libraryId);
        }

        return librarianAssignmentRepository.findByLibraryIdAndStatus(libraryId, LibrarianAssignmentStatus.ACTIVE).stream()
                .map(this::mapAssignmentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canLibrarianManageLibrary(String userEmail, Long libraryId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return false;
        if (user.getRole() == Role.ADMIN) return true;
        if (user.getRole() != Role.LIBRARIAN) return false;

        return librarianAssignmentRepository.existsByUserIdAndLibraryIdAndStatus(user.getId(), libraryId, LibrarianAssignmentStatus.ACTIVE);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<LibraryResponse> mapPageToResponse(Page<Library> libraryPage) {
        List<LibraryResponse> content = libraryPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                libraryPage.getNumber(),
                libraryPage.getSize(),
                libraryPage.getTotalElements(),
                libraryPage.getTotalPages(),
                libraryPage.isLast()
        );
    }

    private void mapRequestToEntity(LibraryRequest req, Library library) {
        library.setName(req.getName());
        library.setCode(req.getCode().toUpperCase().trim());
        library.setAddress(req.getAddress());
        library.setCity(req.getCity());
        library.setDistrict(req.getDistrict());
        library.setState(req.getState());
        library.setPincode(req.getPincode());
        library.setEmail(req.getEmail());
        library.setPhone(req.getPhone());
        if (req.getStatus() != null) {
            library.setStatus(req.getStatus());
        }
    }

    private LibraryResponse mapEntityToResponse(Library library) {
        LibraryResponse res = new LibraryResponse();
        res.setId(library.getId());
        res.setName(library.getName());
        res.setCode(library.getCode());
        res.setAddress(library.getAddress());
        res.setCity(library.getCity());
        res.setDistrict(library.getDistrict());
        res.setState(library.getState());
        res.setPincode(library.getPincode());
        res.setEmail(library.getEmail());
        res.setPhone(library.getPhone());
        res.setStatus(library.getStatus());

        long total = libraryBookRepository.countByLibraryId(library.getId());
        long available = libraryBookRepository.countByLibraryIdAndAvailabilityStatus(library.getId(), LibraryBookStatus.AVAILABLE);
        long unavailable = libraryBookRepository.countByLibraryIdAndAvailabilityStatus(library.getId(), LibraryBookStatus.UNAVAILABLE);

        res.setTotalBooks(total);
        res.setAvailableBooks(available);
        res.setUnavailableBooks(unavailable);

        res.setCreatedAt(library.getCreatedAt());
        res.setUpdatedAt(library.getUpdatedAt());
        return res;
    }

    private LibraryBookResponse mapLibraryBookToResponse(LibraryBook lb) {
        LibraryBookResponse res = new LibraryBookResponse();
        res.setId(lb.getId());
        res.setLibraryId(lb.getLibrary().getId());
        res.setLibraryName(lb.getLibrary().getName());
        res.setLibraryCode(lb.getLibrary().getCode());
        res.setBook(bookService.getBookById(lb.getBook().getId()));
        res.setAvailabilityStatus(lb.getAvailabilityStatus());
        res.setAddedAt(lb.getAddedAt());
        res.setUpdatedAt(lb.getUpdatedAt());
        return res;
    }

    private LibrarianAssignmentResponse mapAssignmentToResponse(LibrarianAssignment assignment) {
        LibrarianAssignmentResponse res = new LibrarianAssignmentResponse();
        res.setId(assignment.getId());
        res.setUserId(assignment.getUser().getId());
        res.setUserName(assignment.getUser().getName());
        res.setUserEmail(assignment.getUser().getEmail());
        res.setLibraryId(assignment.getLibrary().getId());
        res.setLibraryName(assignment.getLibrary().getName());
        res.setLibraryCode(assignment.getLibrary().getCode());
        res.setStatus(assignment.getStatus());
        res.setAssignedAt(assignment.getAssignedAt());
        return res;
    }
}
