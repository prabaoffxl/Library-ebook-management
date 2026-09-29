package com.library.service.impl;

import com.library.dto.NotificationResponse;
import com.library.dto.PageResponse;
import com.library.entity.*;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LicenseRepository licenseRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LibrarianAssignmentRepository librarianAssignmentRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   BookRepository bookRepository,
                                   LicenseRepository licenseRepository,
                                   LibraryBookRepository libraryBookRepository,
                                   LibrarianAssignmentRepository librarianAssignmentRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.licenseRepository = licenseRepository;
        this.libraryBookRepository = libraryBookRepository;
        this.librarianAssignmentRepository = librarianAssignmentRepository;
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String message, NotificationType type) {
        createNotification(userId, title, message, type, NotificationPriority.MEDIUM, null, null, null);
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String message, NotificationType type,
                                   NotificationPriority priority, Long bookId, Long licenseId, String notificationKey) {
        if (notificationKey != null && !notificationKey.isBlank() && notificationRepository.existsByNotificationKey(notificationKey)) {
            // Prevent duplicate notification
            return;
        }

        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification(user, title, message, type, priority);
            if (bookId != null) {
                bookRepository.findById(bookId).ifPresent(notification::setRelatedBook);
            }
            if (licenseId != null) {
                licenseRepository.findById(licenseId).ifPresent(notification::setRelatedLicense);
            }
            if (notificationKey != null && !notificationKey.isBlank()) {
                notification.setNotificationKey(notificationKey);
            }
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void notifyAdminsAndAssignedLibrarians(Long bookId, String title, String message, NotificationType type,
                                                  NotificationPriority priority, Long licenseId, String keyPrefix) {
        Set<User> targetUsers = new HashSet<>();

        // 1. Add all Admins
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.getRole() == Role.ADMIN) {
                targetUsers.add(u);
            }
        }

        // 2. Add Librarians assigned to libraries where the book is held
        if (bookId != null) {
            List<LibraryBook> libBooks = libraryBookRepository.findByBookId(bookId);
            for (LibraryBook lb : libBooks) {
                Long libId = lb.getLibrary().getId();
                List<LibrarianAssignment> assignments = librarianAssignmentRepository.findByLibraryIdAndStatus(libId, LibrarianAssignmentStatus.ACTIVE);
                for (LibrarianAssignment la : assignments) {
                    targetUsers.add(la.getUser());
                }
            }
        } else {
            // If no specific book, include all active librarians
            for (User u : allUsers) {
                if (u.getRole() == Role.LIBRARIAN) {
                    targetUsers.add(u);
                }
            }
        }

        // Send notification to each unique target user
        for (User targetUser : targetUsers) {
            String notifKey = null;
            if (keyPrefix != null && !keyPrefix.isBlank()) {
                notifKey = keyPrefix + "_" + targetUser.getId();
            }
            createNotification(targetUser.getId(), title, message, type, priority, bookId, licenseId, notifKey);
        }
    }

    @Override
    @Transactional
    public void notifyAdmins(String title, String message, NotificationType type, NotificationPriority priority) {
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.getRole() == Role.ADMIN) {
                createNotification(u.getId(), title, message, type, priority, null, null, null);
            }
        }
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(String userEmail, int page, int size) {
        return getUserNotifications(userEmail, null, null, page, size);
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(String userEmail, Boolean unreadOnly, NotificationType type, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifPage;

        if (Boolean.TRUE.equals(unreadOnly)) {
            notifPage = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), false, pageable);
        } else if (type != null) {
            notifPage = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type, pageable);
        } else {
            notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }

        List<NotificationResponse> content = notifPage.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                notifPage.getNumber(),
                notifPage.getSize(),
                notifPage.getTotalElements(),
                notifPage.getTotalPages(),
                notifPage.isLast()
        );
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new UnauthorizedAccessException("You do not have permission to access this notification");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Notification> unreadList = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification notification : unreadList) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadList);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, String userEmail) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new UnauthorizedAccessException("You do not have permission to delete this notification");
        }

        notificationRepository.delete(notification);
    }
}
