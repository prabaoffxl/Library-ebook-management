package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.PageResponse;
import com.library.entity.NotificationPriority;
import com.library.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, String title, String message, NotificationType type);
    void createNotification(Long userId, String title, String message, NotificationType type, NotificationPriority priority, Long bookId, Long licenseId, String notificationKey);
    void notifyAdminsAndAssignedLibrarians(Long bookId, String title, String message, NotificationType type, NotificationPriority priority, Long licenseId, String keyPrefix);
    void notifyAdmins(String title, String message, NotificationType type, NotificationPriority priority);
    PageResponse<NotificationResponse> getUserNotifications(String userEmail, int page, int size);
    PageResponse<NotificationResponse> getUserNotifications(String userEmail, Boolean unreadOnly, NotificationType type, int page, int size);
    List<NotificationResponse> getUnreadNotifications(String userEmail);
    long getUnreadCount(String userEmail);
    NotificationResponse markAsRead(Long notificationId, String userEmail);
    void markAllAsRead(String userEmail);
    void deleteNotification(Long notificationId, String userEmail);
}
