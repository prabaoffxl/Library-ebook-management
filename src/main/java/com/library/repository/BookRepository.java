package com.library.repository;

import com.library.entity.Book;
import com.library.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    boolean existsByIsbn(String isbn);
    Optional<Book> findByIsbn(String isbn);
    long countByStatus(BookStatus status);

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.language) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);

    Page<Book> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<Book> findByLanguageIgnoreCase(String language, Pageable pageable);
    Page<Book> findByAuthorIgnoreCase(String author, Pageable pageable);
    Page<Book> findByPublisherIgnoreCase(String publisher, Pageable pageable);
    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category ORDER BY COUNT(b) DESC")
    java.util.List<Object[]> countBooksByCategory();

    @Query("SELECT b.language, COUNT(b) FROM Book b GROUP BY b.language ORDER BY COUNT(b) DESC")
    java.util.List<Object[]> countBooksByLanguage();

    @Query("SELECT b.publicationYear, COUNT(b) FROM Book b WHERE b.publicationYear IS NOT NULL GROUP BY b.publicationYear ORDER BY b.publicationYear DESC")
    java.util.List<Object[]> countBooksByPublicationYear();

    @Query("SELECT lb.library.id, lb.library.name, COUNT(lb) FROM LibraryBook lb GROUP BY lb.library.id, lb.library.name")
    java.util.List<Object[]> countBooksByLibrary();

    java.util.List<Book> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT b.title FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<String> findTitleSuggestions(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT b.author FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<String> findAuthorSuggestions(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE LOWER(b.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<String> findCategorySuggestions(@Param("query") String query, Pageable pageable);

    @Query("SELECT b.author, COUNT(b) FROM Book b WHERE (:query IS NULL OR :query = '' OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))) GROUP BY b.author ORDER BY COUNT(b) DESC")
    Page<Object[]> countBooksByAuthor(@Param("query") String query, Pageable pageable);

    @Query("SELECT b.author, COUNT(b) FROM Book b GROUP BY b.author ORDER BY COUNT(b) DESC")
    java.util.List<Object[]> findPopularAuthors(Pageable pageable);
}

