package com.library.dto.reader;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReadingProgressRequest {

    @NotNull(message = "Current page is required")
    @Min(value = 1, message = "Current page must be at least 1")
    private Integer currentPage;

    @Min(value = 1, message = "Total pages must be at least 1")
    private Integer totalPages;

    public ReadingProgressRequest() {
    }

    public ReadingProgressRequest(Integer currentPage, Integer totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
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
}
