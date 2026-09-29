package com.library.repository;

import com.library.entity.ActivityType;
import com.library.entity.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    Page<UserActivity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<UserActivity> findByLibraryIdOrderByTimestampDesc(Long libraryId, Pageable pageable);

    Page<UserActivity> findByActivityTypeOrderByTimestampDesc(ActivityType activityType, Pageable pageable);

    Page<UserActivity> findAllByOrderByTimestampDesc(Pageable pageable);

    List<UserActivity> findTop10ByUserIdOrderByTimestampDesc(Long userId);

    long countByLibraryId(Long libraryId);

    @Query("SELECT ua.activityType, COUNT(ua) FROM UserActivity ua GROUP BY ua.activityType")
    List<Object[]> countByActivityType();

    @Query("SELECT DATE(ua.timestamp), COUNT(ua) FROM UserActivity ua GROUP BY DATE(ua.timestamp) ORDER BY DATE(ua.timestamp) DESC")
    List<Object[]> countActivityByDay();

    @Query("SELECT ua.user.name, COUNT(ua) FROM UserActivity ua GROUP BY ua.user.id, ua.user.name ORDER BY COUNT(ua) DESC")
    List<Object[]> findMostActiveUsers();

    @Query("SELECT ua.book.id, ua.book.title, COUNT(ua) FROM UserActivity ua WHERE ua.activityType = com.library.entity.ActivityType.BOOK_VIEW AND ua.book IS NOT NULL GROUP BY ua.book.id, ua.book.title ORDER BY COUNT(ua) DESC")
    List<Object[]> findMostViewedBooks();

    @Query("SELECT ua.book.id, ua.book.title, COUNT(ua) FROM UserActivity ua WHERE ua.activityType = com.library.entity.ActivityType.BOOK_DOWNLOAD AND ua.book IS NOT NULL GROUP BY ua.book.id, ua.book.title ORDER BY COUNT(ua) DESC")
    List<Object[]> findMostDownloadedBooks();
}
