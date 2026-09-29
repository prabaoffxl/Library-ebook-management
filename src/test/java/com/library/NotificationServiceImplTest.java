package com.library;

import com.library.dto.NotificationResponse;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LibraryBookRepository libraryBookRepository;

    @Mock
    private LibrarianAssignmentRepository librarianAssignmentRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User adminUser;
    private User patronUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        patronUser = new User();
        patronUser.setId(2L);
        patronUser.setName("Patron");
        patronUser.setEmail("patron@test.com");
        patronUser.setRole(Role.USER);
    }

    @Test
    void testCreateNotification_PreventDuplicateKey() {
        when(notificationRepository.existsByNotificationKey("EXP_KEY_123")).thenReturn(true);

        notificationService.createNotification(2L, "Title", "Message", NotificationType.LICENSE_EXPIRING,
                NotificationPriority.HIGH, null, null, "EXP_KEY_123");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testCreateNotification_Success() {
        when(notificationRepository.existsByNotificationKey("EXP_KEY_456")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(patronUser));

        notificationService.createNotification(2L, "Expiry Warning", "License expiring soon", NotificationType.LICENSE_EXPIRING,
                NotificationPriority.HIGH, null, null, "EXP_KEY_456");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetUnreadCount() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(patronUser));
        when(notificationRepository.countByUserIdAndIsReadFalse(2L)).thenReturn(5L);

        long count = notificationService.getUnreadCount("patron@test.com");

        assertEquals(5L, count);
    }

    @Test
    void testMarkAsRead() {
        Notification notification = new Notification(patronUser, "Alert", "Test msg", NotificationType.SYSTEM);
        notification.setId(10L);

        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        NotificationResponse response = notificationService.markAsRead(10L, "patron@test.com");

        assertTrue(response.isRead());
        assertNotNull(response.getReadAt());
    }
}
