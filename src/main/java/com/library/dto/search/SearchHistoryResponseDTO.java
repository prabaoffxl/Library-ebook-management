package com.library.dto.search;

import java.time.LocalDateTime;

public class SearchHistoryResponseDTO {
    private Long id;
    private String query;
    private LocalDateTime searchedAt;

    public SearchHistoryResponseDTO() {
    }

    public SearchHistoryResponseDTO(Long id, String query, LocalDateTime searchedAt) {
        this.id = id;
        this.query = query;
        this.searchedAt = searchedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
}
