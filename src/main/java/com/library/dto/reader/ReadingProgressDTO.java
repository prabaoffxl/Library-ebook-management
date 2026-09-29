package com.library.dto.reader;

import java.time.LocalDateTime;

public class ReadingProgressDTO {

    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverImage;
    private Integer currentPage;
    private Integer totalPages;
    private Double progressPercentage;
    private LocalDateTime lastReadAt;

    public ReadingProgressDTO() {
    }

    public ReadingProgressDTO(Long id, Long userId, Long bookId, String bookTitle, String bookAuthor, String coverImage, Integer currentPage, Integer totalPages, Double progressPercentage, LocalDateTime lastReadAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.coverImage = coverImage;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.progressPercentage = progressPercentage;
        this.lastReadAt = lastReadAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
