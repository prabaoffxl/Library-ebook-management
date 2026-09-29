package com.library.service.impl;

import com.library.dto.AssetIntegrityResponse;
import com.library.entity.DigitalAsset;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.DigitalAssetRepository;
import com.library.service.AssetIntegrityService;
import com.library.service.ChecksumService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
public class AssetIntegrityServiceImpl implements AssetIntegrityService {

    private final DigitalAssetRepository digitalAssetRepository;
    private final ChecksumService checksumService;

    public AssetIntegrityServiceImpl(DigitalAssetRepository digitalAssetRepository, ChecksumService checksumService) {
        this.digitalAssetRepository = digitalAssetRepository;
        this.checksumService = checksumService;
    }

    @Override
    public AssetIntegrityResponse verifyAssetIntegrity(Long assetId) {
        DigitalAsset asset = digitalAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Digital asset not found with id: " + assetId));

        File physicalFile = Paths.get(asset.getFilePath()).toAbsolutePath().normalize().toFile();
        if (!physicalFile.exists()) {
            return new AssetIntegrityResponse(
                    assetId,
                    asset.getChecksum(),
                    "FILE_MISSING",
                    false,
                    "Physical file is missing from storage location: " + asset.getFilePath()
            );
        }

        String actualChecksum = checksumService.calculateChecksum(physicalFile);
        boolean valid = asset.getChecksum().equalsIgnoreCase(actualChecksum);
        String message = valid ? "File integrity verified successfully (SHA-256 match)." : "File corruption detected! Checksum mismatch.";

        return new AssetIntegrityResponse(
                assetId,
                asset.getChecksum(),
                actualChecksum,
                valid,
                message
        );
    }
}
