package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.PageResponse;
import com.library.dto.UserActivityResponse;
import com.library.service.UserActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserActivityController {

    private final UserActivityService userActivityService;

    public UserActivityController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/api/users/me/activity")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityResponse>>> getMyActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        PageResponse<UserActivityResponse> response = userActivityService.getUserActivities(userEmail, page, size);
        return ResponseEntity.ok(ApiResponse.success("User activity history retrieved successfully", response));
    }

    @GetMapping("/api/activities/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityResponse>>> getSystemRecentActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<UserActivityResponse> response = userActivityService.getAllRecentActivities(page, size);
        return ResponseEntity.ok(ApiResponse.success("System recent activities retrieved successfully", response));
    }

    @GetMapping("/api/activities/library/{libraryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<PageResponse<UserActivityResponse>>> getLibraryActivities(
            @PathVariable Long libraryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<UserActivityResponse> response = userActivityService.getLibraryActivities(libraryId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Library branch activities retrieved successfully", response));
    }
}
