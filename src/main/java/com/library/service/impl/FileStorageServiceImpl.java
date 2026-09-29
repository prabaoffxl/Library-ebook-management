package com.library.service.impl;

import com.library.exception.FileStorageException;
import com.library.exception.ResourceNotFoundException;
import com.library.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final java.nio.file.Path ebookStorageLocation;
    private final java.nio.file.Path coverStorageLocation;

    private static final List<String> ALLOWED_EBOOK_EXTENSIONS = Arrays.asList("pdf", "epub");
    private static final List<String> ALLOWED_COVER_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    public FileStorageServiceImpl(
            @Value("${app.storage.ebooks-dir:storage/ebooks}") String ebooksDir,
            @Value("${app.storage.covers-dir:storage/covers}") String coversDir
    ) {
        this.ebookStorageLocation = Paths.get(ebooksDir).toAbsolutePath().normalize();
        this.coverStorageLocation = Paths.get(coversDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.ebookStorageLocation);
            Files.createDirectories(this.coverStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directories where uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeEbookFile(MultipartFile file) {
        return storeFile(file, ebookStorageLocation, ALLOWED_EBOOK_EXTENSIONS, "E-Book (PDF or EPUB)");
    }

    @Override
    public String storeCoverImage(MultipartFile file) {
        return storeFile(file, coverStorageLocation, ALLOWED_COVER_EXTENSIONS, "Cover Image (JPG, PNG, or WEBP)");
    }

    private String storeFile(MultipartFile file, java.nio.file.Path targetLocation, List<String> allowedExtensions, String fileTypeDescription) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed");
        
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }

        String fileExtension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFilename.substring(i + 1).toLowerCase();
        }

        if (!allowedExtensions.contains(fileExtension)) {
            throw new FileStorageException("Invalid file type. Allowed formats for " + fileTypeDescription + ": " + allowedExtensions);
        }

        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            java.nio.file.Path targetPath = targetLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetLocation.relativize(targetPath).toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ResourceNotFoundException("File path is empty or null");
        }

        try {
            java.nio.file.Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable at: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found at: " + filePath);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        try {
            java.nio.file.Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            // Ignore deletion errors or log warning
        }
    }
}
