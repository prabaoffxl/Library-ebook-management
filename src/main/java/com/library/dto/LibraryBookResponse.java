package com.library.dto;

import com.library.entity.LibraryBookStatus;
import java.time.LocalDateTime;

public class LibraryBookResponse {

    private Long id;
    private Long libraryId;
    private String libraryName;
    private String libraryCode;
    private BookDTO book;
    private LibraryBookStatus availabilityStatus;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public LibraryBookResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getLibraryCode() {
        return libraryCode;
    }

    public void setLibraryCode(String libraryCode) {
        this.libraryCode = libraryCode;
    }

    public BookDTO getBook() {
        return book;
    }

    public void setBook(BookDTO book) {
        this.book = book;
    }

    public LibraryBookStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(LibraryBookStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
