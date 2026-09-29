package com.library.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeEbookFile(MultipartFile file);
    String storeCoverImage(MultipartFile file);
    Resource loadFileAsResource(String filePath);
    void deleteFile(String filePath);
}
