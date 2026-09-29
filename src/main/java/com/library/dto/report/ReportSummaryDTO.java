package com.library.dto.report;

import java.math.BigDecimal;

public class ReportSummaryDTO {

    private long totalUsers;
    private long activeUsers;
    private long totalLibraries;
    private long totalBooks;
    private long availableBooks;
    private long unavailableBooks;
    private long totalDigitalAssets;
    private long totalStorageBytes;
    private String totalStorageFormatted;
    private long activeLicenses;
    private long expiringLicenses;
    private long expiredLicenses;
    private long renewedLicenses;
    private long cancelledLicenses;
    private BigDecimal totalLicenseCost = BigDecimal.ZERO;
    private long unreadNotifications;

    public ReportSummaryDTO() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalLibraries() {
        return totalLibraries;
    }

    public void setTotalLibraries(long totalLibraries) {
        this.totalLibraries = totalLibraries;
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

    public long getTotalDigitalAssets() {
        return totalDigitalAssets;
    }

    public void setTotalDigitalAssets(long totalDigitalAssets) {
        this.totalDigitalAssets = totalDigitalAssets;
    }

    public long getTotalStorageBytes() {
        return totalStorageBytes;
    }

    public void setTotalStorageBytes(long totalStorageBytes) {
        this.totalStorageBytes = totalStorageBytes;
    }

    public String getTotalStorageFormatted() {
        return totalStorageFormatted;
    }

    public void setTotalStorageFormatted(String totalStorageFormatted) {
        this.totalStorageFormatted = totalStorageFormatted;
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

    public long getExpiredLicenses() {
        return expiredLicenses;
    }

    public void setExpiredLicenses(long expiredLicenses) {
        this.expiredLicenses = expiredLicenses;
    }

    public long getRenewedLicenses() {
        return renewedLicenses;
    }

    public void setRenewedLicenses(long renewedLicenses) {
        this.renewedLicenses = renewedLicenses;
    }

    public long getCancelledLicenses() {
        return cancelledLicenses;
    }

    public void setCancelledLicenses(long cancelledLicenses) {
        this.cancelledLicenses = cancelledLicenses;
    }

    public BigDecimal getTotalLicenseCost() {
        return totalLicenseCost;
    }

    public void setTotalLicenseCost(BigDecimal totalLicenseCost) {
        this.totalLicenseCost = totalLicenseCost;
    }

    public long getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(long unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }
}
