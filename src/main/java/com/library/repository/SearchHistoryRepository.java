package com.library.repository;

import com.library.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Page<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SearchHistory s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    Optional<SearchHistory> findFirstByUserIdAndQueryIgnoreCase(Long userId, String query);
}
