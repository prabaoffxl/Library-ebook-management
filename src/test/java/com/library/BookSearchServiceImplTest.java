package com.library;

import com.library.dto.search.*;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.impl.BookSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookSearchServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private BookSearchServiceImpl bookSearchService;

    private Book testBook;
    private User testUser;
    private Library testLibrary;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setName("Test User");
        testUser.setRole(Role.USER);

        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Mastering Java Programming");
        testBook.setAuthor("James Gosling");
        testBook.setIsbn("978-0134685991");
        testBook.setCategory("Computer Science");
        testBook.setLanguage("English");
        testBook.setPublisher("Oracle Press");
        testBook.setPublicationYear(2023);
        testBook.setStatus(BookStatus.AVAILABLE);

        testLibrary = new Library();
        testLibrary.setId(100L);
        testLibrary.setName("Central Municipal Branch");
        testLibrary.setCode("LIB-001");
        testLibrary.setCity("Chennai");
        testLibrary.setDistrict("Chennai");
    }

    @Test
    void testSearchBooks_Success() {
        BookSearchCriteriaDTO criteria = new BookSearchCriteriaDTO();
        criteria.setQuery("Java");
        criteria.setCategory("Computer Science");

        Page<Book> bookPage = new PageImpl<>(List.of(testBook), PageRequest.of(0, 10), 1);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));

        Page<BookSearchResultDTO> result = bookSearchService.searchBooks(criteria, PageRequest.of(0, 10, Sort.by("title")), "user@test.com");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Mastering Java Programming", result.getContent().get(0).getTitle());
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
    }

    @Test
    void testSearchBooks_InvalidSortProperty() {
        BookSearchCriteriaDTO criteria = new BookSearchCriteriaDTO();
        Pageable invalidPageable = PageRequest.of(0, 10, Sort.by("invalidColumnName"));

        assertThrows(IllegalArgumentException.class, () -> {
            bookSearchService.searchBooks(criteria, invalidPageable, null);
        });
    }

    @Test
    void testGetSearchSuggestions() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findTitleSuggestions(eq("Java"), any(Pageable.class))).thenReturn(List.of("Java Basics", "Java Advanced"));
        when(bookRepository.findAuthorSuggestions(eq("Java"), any(Pageable.class))).thenReturn(List.of("Java Author"));
        when(bookRepository.findCategorySuggestions(eq("Java"), any(Pageable.class))).thenReturn(List.of());

        List<String> suggestions = bookSearchService.getSearchSuggestions("Java");

        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.contains("Java Basics"));
        assertTrue(suggestions.contains("Java Author"));
    }

    @Test
    void testGetCategoriesAndLanguagesWithCounts() {
        Object[] catRow = new Object[]{"Computer Science", 15L};
        when(bookRepository.countBooksByCategory()).thenReturn(List.<Object[]>of(catRow));

        Object[] langRow = new Object[]{"English", 20L};
        when(bookRepository.countBooksByLanguage()).thenReturn(List.<Object[]>of(langRow));

        List<Object[]> catResult = bookSearchService.getCategoriesWithCounts();
        assertEquals(1, catResult.size());
        assertEquals("Computer Science", catResult.get(0)[0]);

        List<Object[]> langResult = bookSearchService.getLanguagesWithCounts();
        assertEquals(1, langResult.size());
        assertEquals("English", langResult.get(0)[0]);
    }

    @Test
    void testGetAuthorsWithCounts() {
        Object[] authorRow = new Object[]{"James Gosling", 5L};
        Page<Object[]> page = new PageImpl<>(List.<Object[]>of(authorRow));
        when(bookRepository.countBooksByAuthor(eq("James"), any(Pageable.class))).thenReturn(page);

        Page<AuthorDiscoveryDTO> result = bookSearchService.getAuthorsWithCounts("James", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("James Gosling", result.getContent().get(0).getAuthorName());
        assertEquals(5L, result.getContent().get(0).getBookCount());
    }

    @Test
    void testGetBookLibraryLocations() {
        LibraryBook lb = new LibraryBook();
        lb.setId(1000L);
        lb.setBook(testBook);
        lb.setLibrary(testLibrary);
        lb.setAvailabilityStatus(LibraryBookStatus.AVAILABLE);

        when(libraryBookRepository.findByBookId(10L)).thenReturn(List.of(lb));

        List<BookLibraryLocationDTO> locations = bookSearchService.getBookLibraryLocations(10L);

        assertNotNull(locations);
        assertEquals(1, locations.size());
        assertEquals("Central Municipal Branch", locations.get(0).getLibraryName());
        assertEquals("LIB-001", locations.get(0).getLibraryCode());
    }

    @Test
    void testUserSearchHistory_GetAndClear() {
        SearchHistory history = new SearchHistory(testUser, "Java Spring");
        history.setId(50L);
        history.setSearchedAt(LocalDateTime.now());

        Page<SearchHistory> historyPage = new PageImpl<>(List.of(history));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(eq(1L), any(Pageable.class))).thenReturn(historyPage);

        Page<SearchHistoryResponseDTO> result = bookSearchService.getUserSearchHistory("user@test.com", PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Spring", result.getContent().get(0).getQuery());

        bookSearchService.clearUserSearchHistory("user@test.com");
        verify(searchHistoryRepository, times(1)).deleteByUserId(1L);
    }
}
