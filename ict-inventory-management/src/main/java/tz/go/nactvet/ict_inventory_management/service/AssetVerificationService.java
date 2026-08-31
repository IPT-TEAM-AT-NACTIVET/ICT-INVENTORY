package tz.go.nactvet.ict_inventory_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.RejectRequest;
import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.exception.BadRequestException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional
public class AssetVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AssetVerificationService.class);

    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final AssetMapper assetMapper;

    public AssetVerificationService(AssetRepository assetRepository,
                                    AuditLogService auditLogService,
                                    AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.auditLogService = auditLogService;
        this.assetMapper = assetMapper;
    }

    public AssetResponse verify(Long id, String adminUsername, Long adminUserId) {
        Asset asset = getAssetOrThrow(id);

        if (asset.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("Asset is already verified");
        }
        if (asset.getVerificationStatus() == VerificationStatus.REJECTED) {
            throw new BadRequestException("Rejected asset must be corrected and resubmitted before it can be verified");
        }

        asset.setVerificationStatus(VerificationStatus.VERIFIED);
        asset.setRejectionReason(null);

        Asset saved = assetRepository.save(asset);
        auditLogService.log("VERIFY", "ASSET", saved.getId(), adminUsername, adminUserId,
                "Asset verified: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");

        log.info("Asset verified: {} ({}) by {}", saved.getDeviceName(), saved.getAssetNumber(), adminUsername);
        return assetMapper.toResponse(saved);
    }

    public AssetResponse reject(Long id, RejectRequest request, String adminUsername, Long adminUserId) {
        Asset asset = getAssetOrThrow(id);

        if (asset.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("Cannot reject an already verified asset");
        }

        asset.setRejectionReason(request.getRejectionReason());

        if (asset.getVerificationStatus() != VerificationStatus.REJECTED) {
            asset.setVerificationStatus(VerificationStatus.REJECTED);
        }

        Asset saved = assetRepository.save(asset);
        auditLogService.log("REJECT", "ASSET", saved.getId(), adminUsername, adminUserId,
                "Asset rejected: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + "). Reason: " + request.getRejectionReason());

        log.info("Asset rejected: {} ({}) by {}. Reason: {}", saved.getDeviceName(), saved.getAssetNumber(), adminUsername, request.getRejectionReason());
        return assetMapper.toResponse(saved);
    }

    private Asset getAssetOrThrow(Long id) {
        return assetRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }
}