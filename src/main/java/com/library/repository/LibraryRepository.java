package com.library.repository;

import com.library.entity.Library;
import com.library.entity.LibraryStatus;
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
public interface LibraryRepository extends JpaRepository<Library, Long>, JpaSpecificationExecutor<Library> {

    Optional<Library> findByCode(String code);

    boolean existsByCode(String code);

    long countByStatus(LibraryStatus status);

    List<Library> findByStatus(LibraryStatus status);

    @Query("SELECT l FROM Library l WHERE " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.district) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.pincode) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Library> searchLibraries(@Param("query") String query, Pageable pageable);
}
