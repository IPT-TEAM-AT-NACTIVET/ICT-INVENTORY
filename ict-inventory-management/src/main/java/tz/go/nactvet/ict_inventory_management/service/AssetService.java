package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetRequest;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffAssetRequest;
import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.exception.BadRequestException;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;

@Service
@Transactional
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final AuditLogService auditLogService;
    private final AssetMapper assetMapper;

    public AssetService(AssetRepository assetRepository,
                        DeviceTypeRepository deviceTypeRepository,
                        UserRepository userRepository,
                        ZoneRepository zoneRepository,
                        AuditLogService auditLogService,
                        AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.auditLogService = auditLogService;
        this.assetMapper = assetMapper;
    }

    public AssetResponse createByStaff(StaffAssetRequest request, Long currentUserId) {
        assertUniqueNumbers(request.getAssetNumber(), request.getSerialNumber());

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + request.getDeviceTypeId()));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        requireSetupComplete(user);

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        Asset asset = new Asset();
        asset.setAssetNumber(request.getAssetNumber());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setDeviceName(request.getDeviceName());
        asset.setDeviceType(deviceType);
        asset.setUser(user);
        asset.setZone(zone);
        asset.setOffice(normalizeOffice(request.getOffice()));
        asset.setOwnershipType(request.getOwnershipType());
        asset.setDeviceStatus(request.getDeviceStatus());
        asset.setVerificationStatus(VerificationStatus.PENDING);

        Asset saved = assetRepository.save(asset);
        auditLogService.log("CREATE", "ASSET", saved.getId(), user.getUsername(), user.getId(),
                "Asset registered: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset created by staff: {} ({}) by user {}", saved.getDeviceName(), saved.getAssetNumber(), user.getUsername());
        return assetMapper.toResponse(saved);
    }

    public AssetResponse createByAdmin(AssetRequest request) {
        assertUniqueNumbers(request.getAssetNumber(), request.getSerialNumber());

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + request.getDeviceTypeId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        Asset asset = new Asset();
        asset.setAssetNumber(request.getAssetNumber());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setDeviceName(request.getDeviceName());
        asset.setDeviceType(deviceType);
        asset.setUser(user);
        asset.setZone(zone);
        asset.setOffice(normalizeOffice(request.getOffice()));
        asset.setOwnershipType(request.getOwnershipType());
        asset.setDeviceStatus(request.getDeviceStatus());
        asset.setVerificationStatus(VerificationStatus.PENDING);

        Asset saved = assetRepository.save(asset);
        auditLogService.log("CREATE", "ASSET", saved.getId(), "ADMIN", null,
                "Asset registered by admin: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset created by admin: {} ({})", saved.getDeviceName(), saved.getAssetNumber());
        return assetMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> findAllPaged(int page, int size) {
        Page<Asset> assetPage = assetRepository.findByOrderByCreatedAtDesc(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> findFiltered(int page, int size, String assetNumber, String serialNumber,
            String deviceName, Long deviceTypeId, String employeeId, String userName, Long userId, Long directorateId,
            Long sectionId, Long unitId, Long zoneId, String office, OwnershipType ownershipType,
            DeviceStatus deviceStatus, VerificationStatus verificationStatus) {
        Page<Asset> assetPage = assetRepository.findByFilters(
                blankToNull(assetNumber), blankToNull(serialNumber), blankToNull(deviceName),
                deviceTypeId, blankToNull(employeeId), blankToNull(userName), userId, directorateId, sectionId, unitId,
                zoneId, blankToNull(office), ownershipType, deviceStatus, verificationStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findAll() {
        return assetRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssetResponse findById(Long id) {
        Asset asset = assetRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
        return assetMapper.toResponse(asset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findByUserId(Long userId) {
        return assetRepository.findByUserId(userId)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findByDeviceTypeId(Long deviceTypeId) {
        return assetRepository.findByDeviceTypeId(deviceTypeId)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findByVerificationStatus(VerificationStatus status) {
        return assetRepository.findByVerificationStatus(status)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AssetResponse updateByAdmin(Long id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        updateAssetFields(asset, request, true);

        Asset saved = assetRepository.save(asset);
        auditLogService.log("UPDATE", "ASSET", saved.getId(), "ADMIN", null,
                "Asset updated: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset updated by admin: {} ({})", saved.getDeviceName(), saved.getAssetNumber());
        return assetMapper.toResponse(saved);
    }

    public AssetResponse updateByStaff(Long id, AssetUpdateRequest request, Long currentUserId) {
        Asset asset = assetRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        if (!asset.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own assets");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        requireSetupComplete(user);

        if (asset.getVerificationStatus() == VerificationStatus.VERIFIED) {
            throw new BadRequestException("Cannot modify a verified asset. Please contact administrator.");
        }

        updateAssetFields(asset, request, false);

        if (asset.getVerificationStatus() == VerificationStatus.REJECTED) {
            asset.setVerificationStatus(VerificationStatus.PENDING);
            asset.setRejectionReason(null);
        }

        Asset saved = assetRepository.save(asset);
        auditLogService.log("UPDATE", "ASSET", saved.getId(),
                user != null ? user.getUsername() : "unknown", currentUserId,
                "Asset updated by staff: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset updated by staff: {} ({})", saved.getDeviceName(), saved.getAssetNumber());
        return assetMapper.toResponse(saved);
    }

    public void delete(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset not found with id: " + id);
        }
        assetRepository.deleteById(id);
        auditLogService.log("DELETE", "ASSET", id, "ADMIN", null, "Asset deleted: id=" + id);
        log.info("Asset deleted: id={}", id);
    }

    private void updateAssetFields(Asset asset, AssetUpdateRequest request, boolean admin) {
        if (request.getAssetNumber() != null) {
            String assetNumber = request.getAssetNumber().isBlank() ? null : request.getAssetNumber().trim();
            if (assetNumber != null && assetRepository.existsByAssetNumberAndIdNot(assetNumber, asset.getId())) {
                throw new ConflictException("Asset number already exists: " + assetNumber);
            }
            asset.setAssetNumber(assetNumber);
        }
        if (request.getSerialNumber() != null) {
            String serialNumber = request.getSerialNumber().isBlank() ? null : request.getSerialNumber().trim();
            if (serialNumber != null && assetRepository.existsBySerialNumberAndIdNot(serialNumber, asset.getId())) {
                throw new ConflictException("Serial number already exists: " + serialNumber);
            }
            asset.setSerialNumber(serialNumber);
        }
        if (request.getDeviceName() != null) {
            asset.setDeviceName(request.getDeviceName());
        }
        if (request.getDeviceTypeId() != null) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + request.getDeviceTypeId()));
            asset.setDeviceType(deviceType);
        }
        if (request.getDeviceStatus() != null) {
            asset.setDeviceStatus(request.getDeviceStatus());
        }
        if (request.getZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));
            asset.setZone(zone);
        }
        if (request.getOffice() != null) {
            asset.setOffice(normalizeOffice(request.getOffice()));
        }
        if (admin && request.getOwnershipType() != null) {
            asset.setOwnershipType(request.getOwnershipType());
        }
    }

    private String normalizeOffice(String office) {
        if (office == null) {
            return null;
        }
        String trimmed = office.trim();
        if (trimmed.length() > 100) {
            throw new BadRequestException("Office must not exceed 100 characters");
        }
        return trimmed;
    }

    private void requireSetupComplete(User user) {
        if (!user.isSetupCompleted()) {
            throw new BadRequestException(
                    "Please complete your profile setup before registering assets. Add your email, phone number and directorate.");
        }
    }

    private void assertUniqueNumbers(String assetNumber, String serialNumber) {
        if (assetNumber != null && !assetNumber.isBlank()
                && assetRepository.existsByAssetNumber(assetNumber.trim())) {
            throw new ConflictException("Asset number already exists: " + assetNumber);
        }
        if (serialNumber != null && !serialNumber.isBlank()
                && assetRepository.existsBySerialNumber(serialNumber.trim())) {
            throw new ConflictException("Serial number already exists: " + serialNumber);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}