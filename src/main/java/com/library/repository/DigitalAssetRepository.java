package com.library.repository;

import com.library.entity.AssetStatus;
import com.library.entity.AssetType;
import com.library.entity.DigitalAsset;
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
public interface DigitalAssetRepository extends JpaRepository<DigitalAsset, Long>, JpaSpecificationExecutor<DigitalAsset> {

    List<DigitalAsset> findByBookId(Long bookId);

    List<DigitalAsset> findByBookIdOrderByVersionDesc(Long bookId);

    Optional<DigitalAsset> findByBookIdAndAssetTypeAndStatus(Long bookId, AssetType assetType, AssetStatus status);

    boolean existsByBookIdAndAssetTypeAndChecksumAndStatus(Long bookId, AssetType assetType, String checksum, AssetStatus status);

    long countByStatus(AssetStatus status);

    long countByAssetType(AssetType assetType);

    @Query("SELECT SUM(a.fileSize) FROM DigitalAsset a WHERE a.status != 'DELETED'")
    Long getTotalStorageUsed();

    @Query("SELECT a FROM DigitalAsset a WHERE " +
           "LOWER(a.originalFileName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.storedFileName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.mimeType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.checksum) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.uploadedBy) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.book.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<DigitalAsset> searchAssets(@Param("query") String query, Pageable pageable);

    long countByAssetTypeAndStatus(AssetType assetType, AssetStatus status);

    @Query("SELECT a.assetType, COUNT(a), COALESCE(SUM(a.fileSize), 0) FROM DigitalAsset a WHERE a.status != 'DELETED' GROUP BY a.assetType")
    List<Object[]> countAndStorageByAssetType();

    @Query("SELECT a.fileExtension, COUNT(a) FROM DigitalAsset a WHERE a.status != 'DELETED' GROUP BY a.fileExtension ORDER BY COUNT(a) DESC")
    List<Object[]> countAssetsByExtension();

    @Query("SELECT lb.library.id, lb.library.name, COUNT(DISTINCT da), COALESCE(SUM(da.fileSize), 0) FROM DigitalAsset da JOIN LibraryBook lb ON da.book.id = lb.book.id WHERE da.status != 'DELETED' GROUP BY lb.library.id, lb.library.name")
    List<Object[]> countAndStorageByLibrary();

    List<DigitalAsset> findTop10ByOrderByCreatedAtDesc();
}
