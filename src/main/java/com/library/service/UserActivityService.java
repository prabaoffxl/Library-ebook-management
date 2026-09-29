package com.library.service;

import com.library.dto.PageResponse;
import com.library.dto.UserActivityResponse;
import com.library.entity.ActivityType;

public interface UserActivityService {
    void logActivity(String userEmail, ActivityType activityType, Long bookId, Long libraryId, String details);
    PageResponse<UserActivityResponse> getUserActivities(String userEmail, int page, int size);
    PageResponse<UserActivityResponse> getLibraryActivities(Long libraryId, int page, int size);
    PageResponse<UserActivityResponse> getAllRecentActivities(int page, int size);
}
