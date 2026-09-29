package com.library.dto;

public class AssetIntegrityResponse {
    private Long assetId;
    private String expectedChecksum;
    private String actualChecksum;
    private boolean valid;
    private String message;

    public AssetIntegrityResponse() {
    }

    public AssetIntegrityResponse(Long assetId, String expectedChecksum, String actualChecksum, boolean valid, String message) {
        this.assetId = assetId;
        this.expectedChecksum = expectedChecksum;
        this.actualChecksum = actualChecksum;
        this.valid = valid;
        this.message = message;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getExpectedChecksum() {
        return expectedChecksum;
    }

    public void setExpectedChecksum(String expectedChecksum) {
        this.expectedChecksum = expectedChecksum;
    }

    public String getActualChecksum() {
        return actualChecksum;
    }

    public void setActualChecksum(String actualChecksum) {
        this.actualChecksum = actualChecksum;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
