package com.library.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.InputStream;

public interface ChecksumService {
    String calculateChecksum(InputStream inputStream);
    String calculateChecksum(MultipartFile file);
    String calculateChecksum(File file);
}
