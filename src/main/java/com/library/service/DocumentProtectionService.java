package com.library.service;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

public interface DocumentProtectionService {

    HttpHeaders prepareSecureStreamingHeaders(String filename, long contentLength);

    Resource applyProtection(Resource resource, String userEmail, String bookTitle);
}
