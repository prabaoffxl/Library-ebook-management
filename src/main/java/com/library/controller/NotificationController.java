package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.NotificationCountResponse;
import com.library.dto.NotificationResponse;
import com.library.dto.PageResponse;
import com.library.entity.NotificationType;
import com.library.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUserNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(userEmail, unreadOnly, type, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(Authentication authentication) {
        String userEmail = authentication.getName();
        List<NotificationResponse> responses = notificationService.getUnreadNotifications(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", responses));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getUnreadCount(Authentication authentication) {
        String userEmail = authentication.getName();
        long count = notificationService.getUnreadCount(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", new NotificationCountResponse(count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        NotificationResponse response = notificationService.markAsRead(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String userEmail = authentication.getName();
        notificationService.markAllAsRead(userEmail);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        notificationService.deleteNotification(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }
}
