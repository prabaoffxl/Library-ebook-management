package com.library.repository;

import com.library.entity.License;
import com.library.entity.LicenseStatus;
import com.library.entity.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    boolean existsByLicenseKey(String licenseKey);

    Optional<License> findByLicenseKey(String licenseKey);

    long countByStatus(LicenseStatus status);

    List<License> findByBookId(Long bookId);

    List<License> findByBookIdOrderByCreatedAtDesc(Long bookId);

    List<License> findByStatus(LicenseStatus status);

    @Query("SELECT SUM(l.price) FROM License l")
    BigDecimal getTotalLicenseCost();

    @Query("SELECT l FROM License l WHERE " +
           "(:licenseKey IS NULL OR LOWER(l.licenseKey) LIKE LOWER(CONCAT('%', :licenseKey, '%'))) AND " +
           "(:provider IS NULL OR LOWER(l.provider) LIKE LOWER(CONCAT('%', :provider, '%'))) AND " +
           "(:bookTitle IS NULL OR LOWER(l.book.title) LIKE LOWER(CONCAT('%', :bookTitle, '%'))) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:licenseType IS NULL OR l.licenseType = :licenseType)")
    List<License> searchLicenses(
            @Param("licenseKey") String licenseKey,
            @Param("provider") String provider,
            @Param("bookTitle") String bookTitle,
            @Param("status") LicenseStatus status,
            @Param("licenseType") LicenseType licenseType
    );

    @Query("SELECT l.licenseType, COUNT(l) FROM License l GROUP BY l.licenseType")
    List<Object[]> countLicensesByType();

    @Query("SELECT l.provider, COUNT(l) FROM License l GROUP BY l.provider ORDER BY COUNT(l) DESC")
    List<Object[]> countLicensesByProvider();

    @Query("SELECT lb.library.id, lb.library.name, COUNT(DISTINCT l) FROM License l JOIN LibraryBook lb ON l.book.id = lb.book.id GROUP BY lb.library.id, lb.library.name")
    List<Object[]> countLicensesByLibrary();

    @Query("SELECT l.status, COUNT(l) FROM License l GROUP BY l.status")
    List<Object[]> countLicensesByStatus();
}
