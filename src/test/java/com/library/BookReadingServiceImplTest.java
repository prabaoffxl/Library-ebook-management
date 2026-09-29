package com.library;

import com.library.dto.reader.ReadingProgressDTO;
import com.library.dto.reader.ReadingProgressRequest;
import com.library.entity.*;
import com.library.exception.BadRequestException;
import com.library.exception.UnauthorizedAccessException;
import com.library.repository.*;
import com.library.service.*;
import com.library.service.impl.BookReadingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookReadingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private DigitalAssetRepository digitalAssetRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseStatusService licenseStatusService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DocumentProtectionService documentProtectionService;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private ReadingProgressRepository readingProgressRepository;

    @InjectMocks
    private BookReadingServiceImpl bookReadingService;

    private User testUser;
    private Book testBook;
    private DigitalAsset testAsset;
    private License testLicense;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("patron@test.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(Role.USER);

        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Tamil Literature History");
        testBook.setAuthor("K. A. Nilakanta Sastri");
        testBook.setStatus(BookStatus.AVAILABLE);

        testAsset = new DigitalAsset();
        testAsset.setId(100L);
        testAsset.setBook(testBook);
        testAsset.setAssetType(AssetType.EBOOK);
        testAsset.setStatus(AssetStatus.ACTIVE);
        testAsset.setFilePath("storage/ebooks/sample.pdf");
        testAsset.setStoredFileName("sample.pdf");
        testAsset.setFileSize(2048L);

        testLicense = new License();
        testLicense.setId(500L);
        testLicense.setBook(testBook);
        testLicense.setLicenseKey("LIC-ACTIVE-001");
        testLicense.setStatus(LicenseStatus.ACTIVE);
        testLicense.setLicenseType(LicenseType.SINGLE_USER);
        testLicense.setMaxUsers(1);
        testLicense.setCurrentUsers(0);
        testLicense.setStartDate(LocalDate.now().minusMonths(1));
        testLicense.setEndDate(LocalDate.now().plusMonths(5));
    }

    @Test
    void testStreamEbook_Success() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(10L, AssetType.EBOOK, AssetStatus.ACTIVE)).thenReturn(Optional.of(testAsset));
        when(licenseRepository.findByBookIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(testLicense));
        when(licenseStatusService.calculateStatus(testLicense)).thenReturn(LicenseStatus.ACTIVE);

        Resource mockResource = new ByteArrayResource("PDF Content".getBytes());
        when(fileStorageService.loadFileAsResource(testAsset.getFilePath())).thenReturn(mockResource);
        when(documentProtectionService.applyProtection(any(), anyString(), anyString())).thenReturn(mockResource);
        when(documentProtectionService.prepareSecureStreamingHeaders(anyString(), anyLong())).thenReturn(new HttpHeaders());

        ResponseEntity<Resource> response = bookReadingService.streamEbook(10L, null, "patron@test.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userActivityService, times(1)).logActivity(eq("patron@test.com"), eq(ActivityType.BOOK_READ), eq(10L), any(), anyString());
    }

    @Test
    void testStreamEbook_ExpiredLicense_Denied() {
        testLicense.setStatus(LicenseStatus.EXPIRED);
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(10L, AssetType.EBOOK, AssetStatus.ACTIVE)).thenReturn(Optional.of(testAsset));
        when(licenseRepository.findByBookIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(testLicense));
        when(licenseStatusService.calculateStatus(testLicense)).thenReturn(LicenseStatus.EXPIRED);

        assertThrows(BadRequestException.class, () -> {
            bookReadingService.streamEbook(10L, null, "patron@test.com");
        });
    }

    @Test
    void testStreamEbook_MaxUsersReached_Denied() {
        testLicense.setCurrentUsers(1);
        testLicense.setMaxUsers(1);
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(10L, AssetType.EBOOK, AssetStatus.ACTIVE)).thenReturn(Optional.of(testAsset));
        when(licenseRepository.findByBookIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(testLicense));
        when(licenseStatusService.calculateStatus(testLicense)).thenReturn(LicenseStatus.ACTIVE);

        assertThrows(BadRequestException.class, () -> {
            bookReadingService.streamEbook(10L, null, "patron@test.com");
        });
    }

    @Test
    void testStreamPreview_Success() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(10L, AssetType.PREVIEW, AssetStatus.ACTIVE)).thenReturn(Optional.empty());
        when(digitalAssetRepository.findByBookIdAndAssetTypeAndStatus(10L, AssetType.EBOOK, AssetStatus.ACTIVE)).thenReturn(Optional.of(testAsset));

        Resource mockResource = new ByteArrayResource("Preview Content".getBytes());
        when(fileStorageService.loadFileAsResource(testAsset.getFilePath())).thenReturn(mockResource);
        when(documentProtectionService.applyProtection(any(), anyString(), anyString())).thenReturn(mockResource);
        when(documentProtectionService.prepareSecureStreamingHeaders(anyString(), anyLong())).thenReturn(new HttpHeaders());

        ResponseEntity<Resource> response = bookReadingService.streamPreview(10L, null, "patron@test.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateReadingProgress_Success() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));

        ReadingProgress existing = new ReadingProgress(testUser, testBook, 1, 100);
        when(readingProgressRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(existing));
        when(readingProgressRepository.save(any(ReadingProgress.class))).thenAnswer(i -> i.getArgument(0));

        ReadingProgressRequest request = new ReadingProgressRequest(25, 100);
        ReadingProgressDTO dto = bookReadingService.updateReadingProgress(10L, request, "patron@test.com");

        assertNotNull(dto);
        assertEquals(25, dto.getCurrentPage());
        assertEquals(100, dto.getTotalPages());
        assertEquals(25.0, dto.getProgressPercentage());
    }

    @Test
    void testUpdateReadingProgress_InvalidPage_Rejection() {
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));

        ReadingProgressRequest invalidReq = new ReadingProgressRequest(0, 100);

        assertThrows(BadRequestException.class, () -> {
            bookReadingService.updateReadingProgress(10L, invalidReq, "patron@test.com");
        });
    }

    @Test
    void testGetUserReadingList_Success() {
        ReadingProgress prog = new ReadingProgress(testUser, testBook, 10, 50);
        when(userRepository.findByEmail("patron@test.com")).thenReturn(Optional.of(testUser));
        when(readingProgressRepository.findByUserIdOrderByLastReadAtDesc(1L)).thenReturn(List.of(prog));

        List<ReadingProgressDTO> list = bookReadingService.getUserReadingList("patron@test.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Tamil Literature History", list.get(0).getBookTitle());
        assertEquals(20.0, list.get(0).getProgressPercentage());
    }
}
