package com.library.service.impl;

import com.library.exception.FileStorageException;
import com.library.service.ChecksumService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ChecksumServiceImpl implements ChecksumService {

    private static final String ALGORITHM = "SHA-256";

    @Override
    public String calculateChecksum(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new FileStorageException("SHA-256 algorithm not available", ex);
        } catch (IOException ex) {
            throw new FileStorageException("Error reading input stream for checksum calculation", ex);
        }
    }

    @Override
    public String calculateChecksum(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot calculate checksum for empty file.");
        }
        try (InputStream is = file.getInputStream()) {
            return calculateChecksum(is);
        } catch (IOException ex) {
            throw new FileStorageException("Error reading file stream for checksum calculation", ex);
        }
    }

    @Override
    public String calculateChecksum(File file) {
        if (file == null || !file.exists()) {
            throw new FileStorageException("Cannot calculate checksum for non-existent file.");
        }
        try (InputStream is = new FileInputStream(file)) {
            return calculateChecksum(is);
        } catch (IOException ex) {
            throw new FileStorageException("Error reading file for checksum calculation", ex);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
