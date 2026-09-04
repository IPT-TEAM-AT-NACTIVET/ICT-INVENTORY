package tz.go.nactvet.ict_inventory_management.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetRequest;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.CsvImportResult;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
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

    public AssetResponse createByAdmin(AssetRequest request, Long currentUserId) {
        assertUniqueNumbers(request.getAssetNumber(), request.getSerialNumber());

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + request.getDeviceTypeId()));

        User createdBy = currentUserId != null
                ? userRepository.findById(currentUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId))
                : null;

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        Asset asset = new Asset();
        asset.setAssetNumber(request.getAssetNumber());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setDeviceName(request.getDeviceName());
        asset.setDeviceType(deviceType);
        asset.setUserOfAsset(normalizeUserOfAsset(request.getUserOfAsset()));
        asset.setCreatedBy(createdBy);
        asset.setUpdatedBy(createdBy);
        asset.setZone(zone);
        asset.setOffice(normalizeOffice(request.getOffice()));
        asset.setOwnershipType(request.getOwnershipType());
        asset.setDeviceStatus(request.getDeviceStatus());

        Asset saved = assetRepository.save(asset);
        String actor = createdBy != null ? createdBy.getUsername() : "system";
        auditLogService.log("CREATE", "ASSET", saved.getId(), actor, createdBy != null ? createdBy.getId() : null,
                "Asset registered: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset created: {} ({})", saved.getDeviceName(), saved.getAssetNumber());
        return assetMapper.toResponse(saved);
    }

    public CsvImportResult importCsv(String csvContent, Long currentUserId) {
        CsvImportResult result = new CsvImportResult();
        if (csvContent == null || csvContent.isBlank()) {
            result.setImported(0);
            return result;
        }

        List<List<String>> rows = parseCsv(csvContent);
        if (rows.isEmpty()) {
            return result;
        }

        Map<String, Integer> header = indexHeaders(rows.get(0));
        if (header.isEmpty()) {
            result.addError(1, "CSV is missing the required header row");
            return result;
        }

        User createdBy = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        List<Asset> toSave = new ArrayList<>();
        List<String> fileAssetNumbers = new ArrayList<>();
        List<String> fileSerialNumbers = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            int rowNumber = i + 1;
            Asset asset = buildFromRow(row, header, rowNumber, result, fileAssetNumbers, fileSerialNumbers, createdBy);
            if (asset != null) {
                toSave.add(asset);
            }
        }

        List<Asset> saved = assetRepository.saveAll(toSave);
        result.setImported(saved.size());
        log.info("CSV import: {} assets by user id {}", saved.size(), currentUserId);
        for (Asset asset : saved) {
            String actor = createdBy != null ? createdBy.getUsername() : "system";
            auditLogService.log("CREATE", "ASSET", asset.getId(), actor,
                    createdBy != null ? createdBy.getId() : null,
                    "Asset imported from CSV: " + asset.getDeviceName() + " (" + asset.getAssetNumber() + ")");
        }
        return result;
    }

    private Asset buildFromRow(List<String> row, Map<String, Integer> header, int rowNumber,
                               CsvImportResult result, List<String> fileAssetNumbers, List<String> fileSerialNumbers,
                               User createdBy) {
        String assetNumber = value(row, header, "assetNumber");
        String serialNumber = value(row, header, "serialNumber");
        String deviceName = value(row, header, "deviceName");
        String deviceTypeName = value(row, header, "deviceType");
        String userOfAsset = value(row, header, "userOfAsset");
        String zoneName = value(row, header, "zone");
        String office = value(row, header, "office");
        String ownershipRaw = value(row, header, "ownership");
        String statusRaw = value(row, header, "deviceStatus");

        if (deviceName == null || deviceName.isBlank()) {
            result.addError(rowNumber, "Device Name is required");
            return null;
        }
        if (deviceTypeName == null || deviceTypeName.isBlank()) {
            result.addError(rowNumber, "Device Type is required");
            return null;
        }
        if (zoneName == null || zoneName.isBlank()) {
            result.addError(rowNumber, "Zone is required");
            return null;
        }

        DeviceType deviceType = deviceTypeRepository.findByName(deviceTypeName.trim())
                .orElse(null);
        if (deviceType == null) {
            result.addError(rowNumber, "Unknown Device Type: " + deviceTypeName);
            return null;
        }

        Zone zone = zoneRepository.findByName(zoneName.trim()).orElse(null);
        if (zone == null) {
            result.addError(rowNumber, "Unknown Zone: " + zoneName);
            return null;
        }

        OwnershipType ownership = parseOwnership(ownershipRaw);
        if (ownership == null) {
            result.addError(rowNumber, "Invalid Ownership: '" + ownershipRaw + "'. Expected OFFICE or PERSONAL");
            return null;
        }

        DeviceStatus status = parseStatus(statusRaw);
        if (status == null) {
            result.addError(rowNumber, "Invalid Device Status: '" + statusRaw + "'. Expected ACTIVE or DEFECTIVE");
            return null;
        }

        String normAssetNumber = blank(assetNumber);
        String normSerialNumber = blank(serialNumber);
        String normUser = userOfAsset.trim();
        String normOffice = office.trim();
        if (normUser.length() > 255) {
            result.addError(rowNumber, "User of Asset must not exceed 255 characters");
            return null;
        }
        if (normOffice.length() > 100) {
            result.addError(rowNumber, "Office must not exceed 100 characters");
            return null;
        }

        if (normAssetNumber != null) {
            if (assetRepository.existsByAssetNumber(normAssetNumber) || fileAssetNumbers.contains(normAssetNumber)) {
                result.addError(rowNumber, "Asset number already exists: " + normAssetNumber);
                return null;
            }
            fileAssetNumbers.add(normAssetNumber);
        }
        if (normSerialNumber != null) {
            if (assetRepository.existsBySerialNumber(normSerialNumber) || fileSerialNumbers.contains(normSerialNumber)) {
                result.addError(rowNumber, "Serial number already exists: " + normSerialNumber);
                return null;
            }
            fileSerialNumbers.add(normSerialNumber);
        }

        Asset asset = new Asset();
        asset.setAssetNumber(normAssetNumber);
        asset.setSerialNumber(normSerialNumber);
        asset.setDeviceName(deviceName.trim());
        asset.setDeviceType(deviceType);
        asset.setUserOfAsset(normUser);
        asset.setCreatedBy(createdBy);
        asset.setUpdatedBy(createdBy);
        asset.setZone(zone);
        asset.setOffice(normOffice);
        asset.setOwnershipType(ownership);
        asset.setDeviceStatus(status);
        return asset;
    }

    private OwnershipType parseOwnership(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return OwnershipType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private DeviceStatus parseStatus(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return DeviceStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Map<String, Integer> indexHeaders(List<String> headerRow) {
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            String col = headerRow.get(i).trim().toLowerCase(Locale.ROOT).replace(" ", "");
            index.putIfAbsent(col, i);
        }
        Set<String> required = Set.of("devicename", "devicetype", "zone");
        if (!index.keySet().containsAll(required)) {
            return Map.of();
        }
        return index;
    }

    private String value(List<String> row, Map<String, Integer> header, String key) {
        Integer idx = header.get(key);
        if (idx == null || idx >= row.size()) {
            return null;
        }
        String v = row.get(idx);
        return v == null || v.isBlank() ? null : v.trim();
    }

    private List<List<String>> parseCsv(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
                i++;
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                } else if (c == ',') {
                    current.add(field.toString());
                    field.setLength(0);
                    i++;
                } else if (c == '\n') {
                    current.add(field.toString());
                    field.setLength(0);
                    if (!current.isEmpty() && !(current.size() == 1 && current.get(0).isEmpty())) {
                        rows.add(current);
                    }
                    current = new ArrayList<>();
                    i++;
                } else if (c == '\r') {
                    i++;
                } else {
                    field.append(c);
                    i++;
                }
            }
        }
        current.add(field.toString());
        if (!current.isEmpty() && !(current.size() == 1 && current.get(0).isEmpty())) {
            rows.add(current);
        }
        return rows;
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
            String deviceName, Long deviceTypeId, String userOfAsset, Long zoneId, String office, OwnershipType ownershipType,
            DeviceStatus deviceStatus) {
        Page<Asset> assetPage = assetRepository.findByFilters(
                blankToNull(assetNumber), blankToNull(serialNumber), blankToNull(deviceName),
                deviceTypeId, blankToNull(userOfAsset),
                zoneId, blankToNull(office), ownershipType, deviceStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> findSearch(int page, int size, String search) {
        String term = search == null || search.isBlank()
                ? "%"
                : "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        Page<Asset> assetPage = assetRepository.findBySearch(term,
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
    public List<AssetResponse> findByDeviceTypeId(Long deviceTypeId) {
        return assetRepository.findByDeviceTypeId(deviceTypeId)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AssetResponse updateByAdmin(Long id, AssetUpdateRequest request, Long currentUserId) {
        Asset asset = assetRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        User updatedBy = currentUserId != null
                ? userRepository.findById(currentUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId))
                : null;
        asset.setUpdatedBy(updatedBy);

        updateAssetFields(asset, request);

        Asset saved = assetRepository.save(asset);
        auditLogService.log("UPDATE", "ASSET", saved.getId(),
                updatedBy != null ? updatedBy.getUsername() : "admin", updatedBy != null ? updatedBy.getId() : null,
                "Asset updated: " + saved.getDeviceName() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset updated: {} ({})", saved.getDeviceName(), saved.getAssetNumber());
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

    private void updateAssetFields(Asset asset, AssetUpdateRequest request) {
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
        if (request.getOwnershipType() != null) {
            asset.setOwnershipType(request.getOwnershipType());
        }
        if (request.getUserOfAsset() != null) {
            asset.setUserOfAsset(normalizeUserOfAsset(request.getUserOfAsset()));
        }
    }

    private String normalizeUserOfAsset(String userOfAsset) {
        if (userOfAsset == null) {
            return null;
        }
        String trimmed = userOfAsset.trim();
        if (trimmed.length() > 255) {
            throw new BadRequestException("User of asset must not exceed 255 characters");
        }
        return trimmed;
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