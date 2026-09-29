package com.library.dto.search;

public class BookSearchCriteriaDTO {
    private String query;
    private String category;
    private String author;
    private String language;
    private String publisher;
    private Integer publicationYear;
    private String status;
    private Long libraryId;
    private Boolean licenseAvailable;

    public BookSearchCriteriaDTO() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public Boolean getLicenseAvailable() {
        return licenseAvailable;
    }

    public void setLicenseAvailable(Boolean licenseAvailable) {
        this.licenseAvailable = licenseAvailable;
    }
}
