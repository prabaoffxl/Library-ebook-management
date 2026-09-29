package com.library.service;

import com.library.dto.AssetIntegrityResponse;

public interface AssetIntegrityService {
    AssetIntegrityResponse verifyAssetIntegrity(Long assetId);
}
