package com.library.service;

import com.library.dto.reader.ReadingProgressDTO;
import com.library.dto.reader.ReadingProgressRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BookReadingService {

    ResponseEntity<Resource> streamEbook(Long bookId, String rangeHeader, String userEmail);

    ResponseEntity<Resource> streamPreview(Long bookId, String rangeHeader, String userEmail);

    ReadingProgressDTO getReadingProgress(Long bookId, String userEmail);

    ReadingProgressDTO updateReadingProgress(Long bookId, ReadingProgressRequest request, String userEmail);

    List<ReadingProgressDTO> getUserReadingList(String userEmail);
}
