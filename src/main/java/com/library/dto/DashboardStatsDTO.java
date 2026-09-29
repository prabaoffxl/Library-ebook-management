package com.library.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private long totalBooks;
    private long availableBooks;
    private long unavailableBooks;
    private long archivedBooks;
    private long totalUsers;
    private long totalLicenses;
    private long activeLicenses;
    private long expiringSoonLicenses;
    private long expiredLicenses;
    private long pendingLicenses;
    private long suspendedLicenses;
    private BigDecimal totalLicenseCost = BigDecimal.ZERO;
    private long totalAssets;
    private long activeAssets;
    private long archivedAssets;
    private long totalStorageUsedBytes;
    private long totalLibraries;
    private long activeLibraries;
    private long activeUsers;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long totalBooks, long availableBooks, long unavailableBooks, long archivedBooks,
                             long totalUsers, long totalLicenses, long activeLicenses, long expiringSoonLicenses,
                             long expiredLicenses, long pendingLicenses, long suspendedLicenses, BigDecimal totalLicenseCost) {
        this(totalBooks, availableBooks, unavailableBooks, archivedBooks, totalUsers, totalLicenses, activeLicenses, expiringSoonLicenses, expiredLicenses, pendingLicenses, suspendedLicenses, totalLicenseCost, 0, 0, 0, 0, 0, 0, 0);
    }

    public DashboardStatsDTO(long totalBooks, long availableBooks, long unavailableBooks, long archivedBooks,
                             long totalUsers, long totalLicenses, long activeLicenses, long expiringSoonLicenses,
                             long expiredLicenses, long pendingLicenses, long suspendedLicenses, BigDecimal totalLicenseCost,
                             long totalAssets, long activeAssets, long archivedAssets, long totalStorageUsedBytes) {
        this(totalBooks, availableBooks, unavailableBooks, archivedBooks, totalUsers, totalLicenses, activeLicenses, expiringSoonLicenses, expiredLicenses, pendingLicenses, suspendedLicenses, totalLicenseCost, totalAssets, activeAssets, archivedAssets, totalStorageUsedBytes, 0, 0, 0);
    }

    public DashboardStatsDTO(long totalBooks, long availableBooks, long unavailableBooks, long archivedBooks,
                             long totalUsers, long totalLicenses, long activeLicenses, long expiringSoonLicenses,
                             long expiredLicenses, long pendingLicenses, long suspendedLicenses, BigDecimal totalLicenseCost,
                             long totalAssets, long activeAssets, long archivedAssets, long totalStorageUsedBytes,
                             long totalLibraries, long activeLibraries, long activeUsers) {
        this.totalBooks = totalBooks;
        this.availableBooks = availableBooks;
        this.unavailableBooks = unavailableBooks;
        this.archivedBooks = archivedBooks;
        this.totalUsers = totalUsers;
        this.totalLicenses = totalLicenses;
        this.activeLicenses = activeLicenses;
        this.expiringSoonLicenses = expiringSoonLicenses;
        this.expiredLicenses = expiredLicenses;
        this.pendingLicenses = pendingLicenses;
        this.suspendedLicenses = suspendedLicenses;
        this.totalLicenseCost = totalLicenseCost != null ? totalLicenseCost : BigDecimal.ZERO;
        this.totalAssets = totalAssets;
        this.activeAssets = activeAssets;
        this.archivedAssets = archivedAssets;
        this.totalStorageUsedBytes = totalStorageUsedBytes;
        this.totalLibraries = totalLibraries;
        this.activeLibraries = activeLibraries;
        this.activeUsers = activeUsers;
    }

    // Getters and Setters
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

    public long getArchivedBooks() {
        return archivedBooks;
    }

    public void setArchivedBooks(long archivedBooks) {
        this.archivedBooks = archivedBooks;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalLicenses() {
        return totalLicenses;
    }

    public void setTotalLicenses(long totalLicenses) {
        this.totalLicenses = totalLicenses;
    }

    public long getActiveLicenses() {
        return activeLicenses;
    }

    public void setActiveLicenses(long activeLicenses) {
        this.activeLicenses = activeLicenses;
    }

    public long getExpiringSoonLicenses() {
        return expiringSoonLicenses;
    }

    public void setExpiringSoonLicenses(long expiringSoonLicenses) {
        this.expiringSoonLicenses = expiringSoonLicenses;
    }

    public long getExpiredLicenses() {
        return expiredLicenses;
    }

    public void setExpiredLicenses(long expiredLicenses) {
        this.expiredLicenses = expiredLicenses;
    }

    public long getPendingLicenses() {
        return pendingLicenses;
    }

    public void setPendingLicenses(long pendingLicenses) {
        this.pendingLicenses = pendingLicenses;
    }

    public long getSuspendedLicenses() {
        return suspendedLicenses;
    }

    public void setSuspendedLicenses(long suspendedLicenses) {
        this.suspendedLicenses = suspendedLicenses;
    }

    public BigDecimal getTotalLicenseCost() {
        return totalLicenseCost;
    }

    public void setTotalLicenseCost(BigDecimal totalLicenseCost) {
        this.totalLicenseCost = totalLicenseCost;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public long getActiveAssets() {
        return activeAssets;
    }

    public void setActiveAssets(long activeAssets) {
        this.activeAssets = activeAssets;
    }

    public long getArchivedAssets() {
        return archivedAssets;
    }

    public void setArchivedAssets(long archivedAssets) {
        this.archivedAssets = archivedAssets;
    }

    public long getTotalStorageUsedBytes() {
        return totalStorageUsedBytes;
    }

    public void setTotalStorageUsedBytes(long totalStorageUsedBytes) {
        this.totalStorageUsedBytes = totalStorageUsedBytes;
    }

    public long getTotalLibraries() {
        return totalLibraries;
    }

    public void setTotalLibraries(long totalLibraries) {
        this.totalLibraries = totalLibraries;
    }

    public long getActiveLibraries() {
        return activeLibraries;
    }

    public void setActiveLibraries(long activeLibraries) {
        this.activeLibraries = activeLibraries;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }
}
