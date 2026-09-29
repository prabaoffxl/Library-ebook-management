package com.library.dto;

import com.library.entity.Notification;
import com.library.entity.NotificationPriority;
import com.library.entity.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private Long relatedBookId;
    private Long relatedLicenseId;
    private boolean isRead;
    private LocalDateTime readAt;
    private boolean generatedBySystem;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public static NotificationResponse fromEntity(Notification entity) {
        if (entity == null) {
            return null;
        }
        NotificationResponse dto = new NotificationResponse();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setPriority(entity.getPriority());
        if (entity.getRelatedBook() != null) {
            dto.setRelatedBookId(entity.getRelatedBook().getId());
        }
        if (entity.getRelatedLicense() != null) {
            dto.setRelatedLicenseId(entity.getRelatedLicense().getId());
        }
        dto.setRead(entity.isRead());
        dto.setReadAt(entity.getReadAt());
        dto.setGeneratedBySystem(entity.isGeneratedBySystem());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public Long getRelatedBookId() {
        return relatedBookId;
    }

    public void setRelatedBookId(Long relatedBookId) {
        this.relatedBookId = relatedBookId;
    }

    public Long getRelatedLicenseId() {
        return relatedLicenseId;
    }

    public void setRelatedLicenseId(Long relatedLicenseId) {
        this.relatedLicenseId = relatedLicenseId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public boolean isGeneratedBySystem() {
        return generatedBySystem;
    }

    public void setGeneratedBySystem(boolean generatedBySystem) {
        this.generatedBySystem = generatedBySystem;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
