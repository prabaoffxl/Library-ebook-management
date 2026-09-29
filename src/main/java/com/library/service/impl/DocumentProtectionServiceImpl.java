package com.library.service.impl;

import com.library.service.DocumentProtectionService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class DocumentProtectionServiceImpl implements DocumentProtectionService {

    @Override
    public HttpHeaders prepareSecureStreamingHeaders(String filename, long contentLength) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (filename != null ? filename : "document.pdf") + "\"");
        headers.set(HttpHeaders.CACHE_CONTROL, "private, no-transform, max-age=0, must-revalidate");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Accept-Ranges", "bytes");
        if (contentLength > 0) {
            headers.setContentLength(contentLength);
        }
        return headers;
    }

    @Override
    public Resource applyProtection(Resource resource, String userEmail, String bookTitle) {
        // Return original resource safely for streaming without modifying physical source files on disk
        return resource;
    }
}
