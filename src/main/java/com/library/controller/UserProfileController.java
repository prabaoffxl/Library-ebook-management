package com.library.controller;

import com.library.dto.*;
import com.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        UserProfileResponse response = userService.getUserProfile(userEmail);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        UserProfileResponse response = userService.updateUserProfile(userEmail, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication != null ? authentication.getName() : "system";
        UserProfileResponse response = userService.updateUserStatus(id, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponse.success("User account status updated successfully", response));
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getUsersPaginated(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        PageResponse<UserProfileResponse> response = userService.getUsersPaginated(query, role, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }
}
