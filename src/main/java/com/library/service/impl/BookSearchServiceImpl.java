package com.library.service.impl;

import com.library.dto.search.*;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.BookSearchService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class BookSearchServiceImpl implements BookSearchService {

    private final BookRepository bookRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    @Autowired
    public BookSearchServiceImpl(BookRepository bookRepository,
                                LibraryBookRepository libraryBookRepository,
                                LicenseRepository licenseRepository,
                                UserRepository userRepository,
                                SearchHistoryRepository searchHistoryRepository) {
        this.bookRepository = bookRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Override
    public Page<BookSearchResultDTO> searchBooks(BookSearchCriteriaDTO criteria, Pageable pageable, String currentUserEmail) {
        Pageable sanitizedPageable = sanitizePageable(pageable);

        if (StringUtils.hasText(currentUserEmail) && criteria != null && StringUtils.hasText(criteria.getQuery())) {
            recordSearchHistory(currentUserEmail, criteria.getQuery().trim());
        }

        Specification<Book> spec = createSpecification(criteria);
        Page<Book> bookPage = bookRepository.findAll(spec, sanitizedPageable);

        return bookPage.map(this::mapToSearchResultDTO);
    }

    private void recordSearchHistory(String email, String queryStr) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<SearchHistory> historyOpt = searchHistoryRepository.findFirstByUserIdAndQueryIgnoreCase(user.getId(), queryStr);
            SearchHistory history;
            if (historyOpt.isPresent()) {
                history = historyOpt.get();
                history.setSearchedAt(LocalDateTime.now());
            } else {
                history = new SearchHistory(user, queryStr);
            }
            searchHistoryRepository.save(history);
        }
    }

    private Specification<Book> createSpecification(BookSearchCriteriaDTO criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria != null) {
                if (StringUtils.hasText(criteria.getQuery())) {
                    String q = "%" + criteria.getQuery().trim().toLowerCase() + "%";
                    Predicate pTitle = cb.like(cb.lower(root.get("title")), q);
                    Predicate pAuthor = cb.like(cb.lower(root.get("author")), q);
                    Predicate pIsbn = cb.like(cb.lower(root.get("isbn")), q);
                    Predicate pCategory = cb.like(cb.lower(root.get("category")), q);
                    Predicate pPublisher = cb.like(cb.lower(root.get("publisher")), q);
                    Predicate pLanguage = cb.like(cb.lower(root.get("language")), q);
                    predicates.add(cb.or(pTitle, pAuthor, pIsbn, pCategory, pPublisher, pLanguage));
                }

                if (StringUtils.hasText(criteria.getCategory())) {
                    predicates.add(cb.equal(cb.lower(root.get("category")), criteria.getCategory().trim().toLowerCase()));
                }

                if (StringUtils.hasText(criteria.getAuthor())) {
                    predicates.add(cb.like(cb.lower(root.get("author")), "%" + criteria.getAuthor().trim().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(criteria.getLanguage())) {
                    predicates.add(cb.equal(cb.lower(root.get("language")), criteria.getLanguage().trim().toLowerCase()));
                }

                if (StringUtils.hasText(criteria.getPublisher())) {
                    predicates.add(cb.like(cb.lower(root.get("publisher")), "%" + criteria.getPublisher().trim().toLowerCase() + "%"));
                }

                if (criteria.getPublicationYear() != null) {
                    predicates.add(cb.equal(root.get("publicationYear"), criteria.getPublicationYear()));
                }

                if (StringUtils.hasText(criteria.getStatus())) {
                    try {
                        BookStatus status = BookStatus.valueOf(criteria.getStatus().trim().toUpperCase());
                        predicates.add(cb.equal(root.get("status"), status));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                if (criteria.getLibraryId() != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<LibraryBook> lbRoot = subquery.from(LibraryBook.class);
                    subquery.select(lbRoot.get("book").get("id"));
                    subquery.where(cb.equal(lbRoot.get("library").get("id"), criteria.getLibraryId()));
                    predicates.add(root.get("id").in(subquery));
                }

                if (Boolean.TRUE.equals(criteria.getLicenseAvailable())) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<License> lRoot = subquery.from(License.class);
                    subquery.select(lRoot.get("book").get("id"));
                    subquery.where(cb.and(
                            cb.equal(lRoot.get("book").get("id"), root.get("id")),
                            cb.equal(lRoot.get("status"), LicenseStatus.ACTIVE)
                    ));
                    predicates.add(root.get("id").in(subquery));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        Pageable top10 = PageRequest.of(0, 10);
        String q = query.trim();

        List<String> titles = bookRepository.findTitleSuggestions(q, top10);
        List<String> authors = bookRepository.findAuthorSuggestions(q, top10);
        List<String> categories = bookRepository.findCategorySuggestions(q, top10);

        Set<String> suggestions = new LinkedHashSet<>();
        suggestions.addAll(titles);
        suggestions.addAll(authors);
        suggestions.addAll(categories);

        return suggestions.stream().limit(10).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getCategoriesWithCounts() {
        return bookRepository.countBooksByCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getLanguagesWithCounts() {
        return bookRepository.countBooksByLanguage();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorDiscoveryDTO> getAuthorsWithCounts(String query, Pageable pageable) {
        Pageable sanitized = sanitizePageable(pageable);
        Page<Object[]> page = bookRepository.countBooksByAuthor(query != null ? query.trim() : null, sanitized);
        return page.map(obj -> new AuthorDiscoveryDTO((String) obj[0], (Long) obj[1]));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookLibraryLocationDTO> getBookLibraryLocations(Long bookId) {
        List<LibraryBook> lbList = libraryBookRepository.findByBookId(bookId);
        return lbList.stream().map(lb -> new BookLibraryLocationDTO(
                lb.getLibrary().getId(),
                lb.getLibrary().getName(),
                lb.getLibrary().getCode(),
                lb.getLibrary().getCity(),
                lb.getLibrary().getDistrict(),
                lb.getAvailabilityStatus() != null ? lb.getAvailabilityStatus().name() : "AVAILABLE",
                lb.getAddedAt()
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSearchResultDTO> getPopularBooks() {
        List<Book> topBooks = bookRepository.findTop10ByOrderByCreatedAtDesc();
        return topBooks.stream().map(this::mapToSearchResultDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPopularCategories() {
        return bookRepository.countBooksByCategory();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuthorDiscoveryDTO> getPopularAuthors() {
        Pageable top10 = PageRequest.of(0, 10);
        List<Object[]> popular = bookRepository.findPopularAuthors(top10);
        return popular.stream().map(obj -> new AuthorDiscoveryDTO((String) obj[0], (Long) obj[1])).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchHistoryResponseDTO> getUserSearchHistory(String currentUserEmail, Pageable pageable) {
        if (!StringUtils.hasText(currentUserEmail)) {
            return Page.empty();
        }
        Optional<User> userOpt = userRepository.findByEmail(currentUserEmail);
        if (userOpt.isEmpty()) {
            return Page.empty();
        }
        Pageable sanitized = sanitizePageable(pageable);
        Page<SearchHistory> page = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userOpt.get().getId(), sanitized);
        return page.map(sh -> new SearchHistoryResponseDTO(sh.getId(), sh.getQuery(), sh.getSearchedAt()));
    }

    @Override
    public void clearUserSearchHistory(String currentUserEmail) {
        if (StringUtils.hasText(currentUserEmail)) {
            userOpt(currentUserEmail).ifPresent(user -> searchHistoryRepository.deleteByUserId(user.getId()));
        }
    }

    private Optional<User> userOpt(String email) {
        return userRepository.findByEmail(email);
    }

    private BookSearchResultDTO mapToSearchResultDTO(Book book) {
        BookSearchResultDTO dto = new BookSearchResultDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setLanguage(book.getLanguage());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setDescription(book.getDescription());
        dto.setStatus(book.getStatus() != null ? book.getStatus().name() : "AVAILABLE");
        dto.setCoverImage(book.getCoverImage());

        List<BookLibraryLocationDTO> locations = getBookLibraryLocations(book.getId());
        dto.setAvailableLibraries(locations);
        dto.setAvailableLibrariesCount(locations.size());

        List<License> licenses = licenseRepository.findByBookId(book.getId());
        boolean active = licenses.stream().anyMatch(l -> l.getStatus() == LicenseStatus.ACTIVE);
        dto.setLicenseAvailable(active);
        if (active) {
            dto.setLicenseStatus("ACTIVE");
        } else if (!licenses.isEmpty()) {
            dto.setLicenseStatus(licenses.get(0).getStatus().name());
        } else {
            dto.setLicenseStatus("NO_LICENSE");
        }

        return dto;
    }

    private Pageable sanitizePageable(Pageable pageable) {
        int page = pageable != null && pageable.getPageNumber() >= 0 ? pageable.getPageNumber() : 0;
        int size = pageable != null ? pageable.getPageSize() : 10;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;

        if (pageable == null || pageable.getSort().isUnsorted()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Set<String> allowedFields = Set.of("title", "author", "publicationYear", "createdAt", "status", "id", "searchedAt");
        List<Sort.Order> validOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (allowedFields.contains(order.getProperty())) {
                validOrders.add(order);
            } else {
                throw new IllegalArgumentException("Invalid sort property: " + order.getProperty());
            }
        }
        if (validOrders.isEmpty()) {
            validOrders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        }

        return PageRequest.of(page, size, Sort.by(validOrders));
    }
}
