package com.library.dto.search;

import java.time.LocalDateTime;

public class BookLibraryLocationDTO {
    private Long libraryId;
    private String libraryName;
    private String libraryCode;
    private String city;
    private String district;
    private String availabilityStatus;
    private LocalDateTime addedAt;

    public BookLibraryLocationDTO() {
    }

    public BookLibraryLocationDTO(Long libraryId, String libraryName, String libraryCode, String city, String district, String availabilityStatus, LocalDateTime addedAt) {
        this.libraryId = libraryId;
        this.libraryName = libraryName;
        this.libraryCode = libraryCode;
        this.city = city;
        this.district = district;
        this.availabilityStatus = availabilityStatus;
        this.addedAt = addedAt;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
