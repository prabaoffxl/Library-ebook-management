package com.library.repository;

import com.library.entity.LibraryBook;
import com.library.entity.LibraryBookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long>, JpaSpecificationExecutor<LibraryBook> {

    Optional<LibraryBook> findByLibraryIdAndBookId(Long libraryId, Long bookId);

    boolean existsByLibraryIdAndBookId(Long libraryId, Long bookId);

    void deleteByLibraryIdAndBookId(Long libraryId, Long bookId);

    Page<LibraryBook> findByLibraryId(Long libraryId, Pageable pageable);

    Page<LibraryBook> findByLibraryIdAndAvailabilityStatus(Long libraryId, LibraryBookStatus status, Pageable pageable);

    List<LibraryBook> findByBookId(Long bookId);

    long countByLibraryId(Long libraryId);

    long countByLibraryIdAndAvailabilityStatus(Long libraryId, LibraryBookStatus status);

    @Query("SELECT lb FROM LibraryBook lb WHERE lb.library.id = :libraryId AND (" +
           "LOWER(lb.book.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(lb.book.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(lb.book.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(lb.book.category) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<LibraryBook> searchLibraryBooks(@Param("libraryId") Long libraryId, @Param("query") String query, Pageable pageable);
}
