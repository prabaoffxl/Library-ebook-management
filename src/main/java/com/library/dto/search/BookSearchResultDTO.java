package com.library.dto.search;

import java.util.List;

public class BookSearchResultDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String language;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private String status;
    private String coverImage;
    private int availableLibrariesCount;
    private List<BookLibraryLocationDTO> availableLibraries;
    private boolean licenseAvailable;
    private String licenseStatus;

    public BookSearchResultDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public int getAvailableLibrariesCount() {
        return availableLibrariesCount;
    }

    public void setAvailableLibrariesCount(int availableLibrariesCount) {
        this.availableLibrariesCount = availableLibrariesCount;
    }

    public List<BookLibraryLocationDTO> getAvailableLibraries() {
        return availableLibraries;
    }

    public void setAvailableLibraries(List<BookLibraryLocationDTO> availableLibraries) {
        this.availableLibraries = availableLibraries;
    }

    public boolean isLicenseAvailable() {
        return licenseAvailable;
    }

    public void setLicenseAvailable(boolean licenseAvailable) {
        this.licenseAvailable = licenseAvailable;
    }

    public String getLicenseStatus() {
        return licenseStatus;
    }

    public void setLicenseStatus(String licenseStatus) {
        this.licenseStatus = licenseStatus;
    }
}
