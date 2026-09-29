package com.library.dto.report;

import com.library.dto.DigitalAssetResponse;
import java.util.ArrayList;
import java.util.List;

public class AssetReportDTO {

    private long totalAssets;
    private long activeAssets;
    private long archivedAssets;
    private long replacedAssets;
    private long deletedAssets;
    private long ebookAssets;
    private long coverAssets;
    private long previewAssets;
    private long totalStorageBytes;
    private String totalStorageFormatted;
    private List<LabelValueDTO> storageByAssetType = new ArrayList<>();
    private List<LabelValueDTO> assetsByLibrary = new ArrayList<>();
    private List<LabelValueDTO> assetsByFileType = new ArrayList<>();
    private List<DigitalAssetResponse> recentlyUploadedAssets = new ArrayList<>();

    public AssetReportDTO() {
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

    public long getReplacedAssets() {
        return replacedAssets;
    }

    public void setReplacedAssets(long replacedAssets) {
        this.replacedAssets = replacedAssets;
    }

    public long getDeletedAssets() {
        return deletedAssets;
    }

    public void setDeletedAssets(long deletedAssets) {
        this.deletedAssets = deletedAssets;
    }

    public long getEbookAssets() {
        return ebookAssets;
    }

    public void setEbookAssets(long ebookAssets) {
        this.ebookAssets = ebookAssets;
    }

    public long getCoverAssets() {
        return coverAssets;
    }

    public void setCoverAssets(long coverAssets) {
        this.coverAssets = coverAssets;
    }

    public long getPreviewAssets() {
        return previewAssets;
    }

    public void setPreviewAssets(long previewAssets) {
        this.previewAssets = previewAssets;
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

    public List<LabelValueDTO> getStorageByAssetType() {
        return storageByAssetType;
    }

    public void setStorageByAssetType(List<LabelValueDTO> storageByAssetType) {
        this.storageByAssetType = storageByAssetType;
    }

    public List<LabelValueDTO> getAssetsByLibrary() {
        return assetsByLibrary;
    }

    public void setAssetsByLibrary(List<LabelValueDTO> assetsByLibrary) {
        this.assetsByLibrary = assetsByLibrary;
    }

    public List<LabelValueDTO> getAssetsByFileType() {
        return assetsByFileType;
    }

    public void setAssetsByFileType(List<LabelValueDTO> assetsByFileType) {
        this.assetsByFileType = assetsByFileType;
    }

    public List<DigitalAssetResponse> getRecentlyUploadedAssets() {
        return recentlyUploadedAssets;
    }

    public void setRecentlyUploadedAssets(List<DigitalAssetResponse> recentlyUploadedAssets) {
        this.recentlyUploadedAssets = recentlyUploadedAssets;
    }
}
