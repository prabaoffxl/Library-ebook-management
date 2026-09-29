package com.library.dto.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LicenseReportDTO {

    private long totalLicenses;
    private long activeLicenses;
    private long pendingLicenses;
    private long expiringSoonLicenses;
    private long expiredLicenses;
    private long suspendedLicenses;
    private long renewedLicenses;
    private long cancelledLicenses;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private List<LabelValueDTO> licensesByType = new ArrayList<>();
    private List<LabelValueDTO> licensesByProvider = new ArrayList<>();
    private List<LabelValueDTO> licensesByLibrary = new ArrayList<>();
    private List<LabelValueDTO> costByMonth = new ArrayList<>();
    private List<LabelValueDTO> expirationTrends = new ArrayList<>();

    public LicenseReportDTO() {
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

    public long getPendingLicenses() {
        return pendingLicenses;
    }

    public void setPendingLicenses(long pendingLicenses) {
        this.pendingLicenses = pendingLicenses;
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

    public long getSuspendedLicenses() {
        return suspendedLicenses;
    }

    public void setSuspendedLicenses(long suspendedLicenses) {
        this.suspendedLicenses = suspendedLicenses;
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public List<LabelValueDTO> getLicensesByType() {
        return licensesByType;
    }

    public void setLicensesByType(List<LabelValueDTO> licensesByType) {
        this.licensesByType = licensesByType;
    }

    public List<LabelValueDTO> getLicensesByProvider() {
        return licensesByProvider;
    }

    public void setLicensesByProvider(List<LabelValueDTO> licensesByProvider) {
        this.licensesByProvider = licensesByProvider;
    }

    public List<LabelValueDTO> getLicensesByLibrary() {
        return licensesByLibrary;
    }

    public void setLicensesByLibrary(List<LabelValueDTO> licensesByLibrary) {
        this.licensesByLibrary = licensesByLibrary;
    }

    public List<LabelValueDTO> getCostByMonth() {
        return costByMonth;
    }

    public void setCostByMonth(List<LabelValueDTO> costByMonth) {
        this.costByMonth = costByMonth;
    }

    public List<LabelValueDTO> getExpirationTrends() {
        return expirationTrends;
    }

    public void setExpirationTrends(List<LabelValueDTO> expirationTrends) {
        this.expirationTrends = expirationTrends;
    }
}
