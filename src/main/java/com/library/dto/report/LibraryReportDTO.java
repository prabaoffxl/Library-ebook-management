package com.library.dto.report;

public class LibraryReportDTO {

    private Long libraryId;
    private String libraryName;
    private String libraryCode;
    private long totalBooks;
    private long availableBooks;
    private long unavailableBooks;
    private long assignedLibrarians;
    private long registeredUsers;
    private long activeLicenses;
    private long expiringLicenses;
    private long digitalAssets;
    private long storageBytes;
    private String storageFormatted;
    private long recentActivityCount;

    public LibraryReportDTO() {
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

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(long availableBooks) {
        this.availableBooks = availableBooks;
    }

    public long getUnavailableBooks() {
        return unavailableBooks;
    }

    public void setUnavailableBooks(long unavailableBooks) {
        this.unavailableBooks = unavailableBooks;
    }

    public long getAssignedLibrarians() {
        return assignedLibrarians;
    }

    public void setAssignedLibrarians(long assignedLibrarians) {
        this.assignedLibrarians = assignedLibrarians;
    }

    public long getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(long registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public long getActiveLicenses() {
        return activeLicenses;
    }

    public void setActiveLicenses(long activeLicenses) {
        this.activeLicenses = activeLicenses;
    }

    public long getExpiringLicenses() {
        return expiringLicenses;
    }

    public void setExpiringLicenses(long expiringLicenses) {
        this.expiringLicenses = expiringLicenses;
    }

    public long getDigitalAssets() {
        return digitalAssets;
    }

    public void setDigitalAssets(long digitalAssets) {
        this.digitalAssets = digitalAssets;
    }

    public long getStorageBytes() {
        return storageBytes;
    }

    public void setStorageBytes(long storageBytes) {
        this.storageBytes = storageBytes;
    }

    public String getStorageFormatted() {
        return storageFormatted;
    }

    public void setStorageFormatted(String storageFormatted) {
        this.storageFormatted = storageFormatted;
    }

    public long getRecentActivityCount() {
        return recentActivityCount;
    }

    public void setRecentActivityCount(long recentActivityCount) {
        this.recentActivityCount = recentActivityCount;
    }
}
