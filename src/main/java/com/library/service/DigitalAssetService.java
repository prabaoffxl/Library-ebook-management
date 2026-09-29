package com.library.service;

import com.library.dto.DigitalAssetResponse;
import com.library.dto.PageResponse;
import com.library.entity.AssetType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DigitalAssetService {
    DigitalAssetResponse uploadAsset(Long bookId, AssetType assetType, MultipartFile file, String currentUserEmail);
    DigitalAssetResponse replaceEbook(Long bookId, MultipartFile file, String currentUserEmail);
    DigitalAssetResponse uploadCover(Long bookId, MultipartFile file, String currentUserEmail);
    DigitalAssetResponse uploadPreview(Long bookId, MultipartFile file, String currentUserEmail);
    DigitalAssetResponse getAssetById(Long assetId);
    List<DigitalAssetResponse> getAssetsByBookId(Long bookId);
    PageResponse<DigitalAssetResponse> getAllAssets(int page, int size, String sortBy, String sortDir);
    PageResponse<DigitalAssetResponse> searchAssets(String query, int page, int size, String sortBy, String sortDir);
    PageResponse<DigitalAssetResponse> filterAssets(String assetType, String status, String mimeType, int page, int size, String sortBy, String sortDir);
    DigitalAssetResponse archiveAsset(Long assetId, String currentUserEmail);
    DigitalAssetResponse restoreAsset(Long assetId, String currentUserEmail);
    void deleteAsset(Long assetId, boolean permanent);
    Resource downloadAssetFile(Long assetId);
}
