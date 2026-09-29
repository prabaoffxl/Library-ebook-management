package com.library.service.impl;

import com.library.dto.PageResponse;
import com.library.dto.UserActivityResponse;
import com.library.entity.ActivityType;
import com.library.entity.Book;
import com.library.entity.Library;
import com.library.entity.User;
import com.library.entity.UserActivity;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.LibraryRepository;
import com.library.repository.UserActivityRepository;
import com.library.repository.UserRepository;
import com.library.service.UserActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;

    public UserActivityServiceImpl(UserActivityRepository userActivityRepository,
                                  UserRepository userRepository,
                                  BookRepository bookRepository,
                                  LibraryRepository libraryRepository) {
        this.userActivityRepository = userActivityRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.libraryRepository = libraryRepository;
    }

    @Override
    @Transactional
    public void logActivity(String userEmail, ActivityType activityType, Long bookId, Long libraryId, String details) {
        if (userEmail == null || userEmail.trim().isEmpty() || "system".equalsIgnoreCase(userEmail)) {
            return;
        }

        userRepository.findByEmail(userEmail).ifPresent(user -> {
            Book book = null;
            if (bookId != null) {
                book = bookRepository.findById(bookId).orElse(null);
            }

            Library library = null;
            if (libraryId != null) {
                library = libraryRepository.findById(libraryId).orElse(null);
            }

            UserActivity activity = new UserActivity(user, activityType, book, library, details);
            userActivityRepository.save(activity);
        });
    }

    @Override
    public PageResponse<UserActivityResponse> getUserActivities(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activityPage = userActivityRepository.findByUserIdOrderByTimestampDesc(user.getId(), pageable);
        return mapPageToResponse(activityPage);
    }

    @Override
    public PageResponse<UserActivityResponse> getLibraryActivities(Long libraryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activityPage = userActivityRepository.findByLibraryIdOrderByTimestampDesc(libraryId, pageable);
        return mapPageToResponse(activityPage);
    }

    @Override
    public PageResponse<UserActivityResponse> getAllRecentActivities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserActivity> activityPage = userActivityRepository.findAllByOrderByTimestampDesc(pageable);
        return mapPageToResponse(activityPage);
    }

    private PageResponse<UserActivityResponse> mapPageToResponse(Page<UserActivity> activityPage) {
        List<UserActivityResponse> content = activityPage.getContent().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                activityPage.getNumber(),
                activityPage.getSize(),
                activityPage.getTotalElements(),
                activityPage.getTotalPages(),
                activityPage.isLast()
        );
    }

    private UserActivityResponse mapEntityToResponse(UserActivity activity) {
        UserActivityResponse res = new UserActivityResponse();
        res.setId(activity.getId());
        res.setUserId(activity.getUser().getId());
        res.setUserName(activity.getUser().getName());
        res.setUserEmail(activity.getUser().getEmail());
        res.setActivityType(activity.getActivityType());
        if (activity.getBook() != null) {
            res.setBookId(activity.getBook().getId());
            res.setBookTitle(activity.getBook().getTitle());
        }
        if (activity.getLibrary() != null) {
            res.setLibraryId(activity.getLibrary().getId());
            res.setLibraryName(activity.getLibrary().getName());
        }
        res.setDetails(activity.getDetails());
        res.setTimestamp(activity.getTimestamp());
        return res;
    }
}
