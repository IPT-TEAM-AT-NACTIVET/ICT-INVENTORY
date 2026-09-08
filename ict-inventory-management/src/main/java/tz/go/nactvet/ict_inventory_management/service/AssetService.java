package tz.go.nactvet.ict_inventory_management.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tz.go.nactvet.ict_inventory_management.dto.AssetRequest;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.CsvImportResult;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.exception.BadRequestException;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
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
    private final DirectorateRepository directorateRepository;
    private final AuditLogService auditLogService;
    private final AssetMapper assetMapper;

    public AssetService(AssetRepository assetRepository,
                        DeviceTypeRepository deviceTypeRepository,
                        UserRepository userRepository,
                        ZoneRepository zoneRepository,
                        DirectorateRepository directorateRepository,
                        AuditLogService auditLogService,
                        AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
        this.directorateRepository = directorateRepository;
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

        Directorate directorate = null;
        if (request.getDirectorateId() != null) {
            directorate = directorateRepository.findById(request.getDirectorateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
        }

        Asset asset = new Asset();
        asset.setAssetNumber(request.getAssetNumber());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setZone(zone);
        asset.setDirectorate(directorate);
        asset.setOffice(normalizeOffice(request.getOffice()));
        asset.setUserOfAsset(normalizeUserOfAsset(request.getUserOfAsset()));
        asset.setDeviceType(deviceType);
        asset.setDeviceModel(request.getDeviceModel());
        asset.setCreatedBy(createdBy);
        asset.setUpdatedBy(createdBy);
        asset.setDeviceStatus(request.getDeviceStatus());

        Asset saved = assetRepository.save(asset);
        String actor = createdBy != null ? createdBy.getUsername() : "system";
        auditLogService.log("CREATE", "ASSET", saved.getId(), actor, createdBy != null ? createdBy.getId() : null,
                "Asset registered: " + saved.getDeviceModel() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset created: {} ({})", saved.getDeviceModel(), saved.getAssetNumber());
        return assetMapper.toResponse(saved);
    }

    public CsvImportResult importCsv(String csvContent, Long currentUserId) {
        if (csvContent == null || csvContent.isBlank()) {
            return new CsvImportResult();
        }
        return processRows(parseCsv(csvContent), currentUserId);
    }

    public CsvImportResult importFile(MultipartFile file, Long currentUserId) throws IOException {
        if (file == null || file.isEmpty()) {
            return new CsvImportResult();
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        boolean excel = name.endsWith(".xlsx") || name.endsWith(".xls")
                || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(file.getContentType())
                || "application/vnd.ms-excel".equalsIgnoreCase(file.getContentType());
        if (excel) {
            try (InputStream in = file.getInputStream()) {
                return processRows(parseExcel(in), currentUserId);
            } catch (EncryptedDocumentException | IOException e) {
                throw new BadRequestException("Could not read the spreadsheet file. Please provide a valid .xlsx or .xls file.");
            }
        }
        return importCsv(new String(file.getBytes(), StandardCharsets.UTF_8), currentUserId);
    }

    private CsvImportResult processRows(List<List<String>> rows, Long currentUserId) {
        CsvImportResult result = new CsvImportResult();
        if (rows.isEmpty()) {
            return result;
        }

        Map<String, Integer> header = indexHeaders(rows.get(0));
        if (header.isEmpty()) {
            result.addError(1, "File is missing the required header row");
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
        log.info("File import: {} assets by user id {}", saved.size(), currentUserId);
        for (Asset asset : saved) {
            String actor = createdBy != null ? createdBy.getUsername() : "system";
            auditLogService.log("CREATE", "ASSET", asset.getId(), actor,
                    createdBy != null ? createdBy.getId() : null,
                    "Asset imported from file: " + asset.getDeviceModel() + " (" + asset.getAssetNumber() + ")");
        }
        return result;
    }

    private Asset buildFromRow(List<String> row, Map<String, Integer> header, int rowNumber,
                               CsvImportResult result, List<String> fileAssetNumbers, List<String> fileSerialNumbers,
                               User createdBy) {
        String assetNumber = value(row, header, "assetNumber");
        String serialNumber = value(row, header, "serialNumber");
        String deviceModel = value(row, header, "deviceModel");
        String deviceTypeName = value(row, header, "deviceType");
        String userOfAsset = value(row, header, "userOfAsset");
        String zoneName = value(row, header, "zone");
        String directorateName = value(row, header, "directorate");
        String office = value(row, header, "office");
        String statusRaw = value(row, header, "deviceStatus");

        if (deviceModel == null || deviceModel.isBlank()) {
            result.addError(rowNumber, "Device Model is required");
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

        Directorate directorate = null;
        if (directorateName != null && !directorateName.isBlank()) {
            directorate = directorateRepository.findByName(directorateName.trim()).orElse(null);
            if (directorate == null) {
                result.addError(rowNumber, "Unknown Directorate: " + directorateName);
                return null;
            }
        }

        DeviceStatus status = parseStatus(statusRaw);
        if (status == null) {
            result.addError(rowNumber, "Invalid Device Status: '" + statusRaw + "'. Expected WORKING or NOT_WORKING");
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
        asset.setZone(zone);
        asset.setDirectorate(directorate);
        asset.setOffice(normOffice);
        asset.setUserOfAsset(normUser);
        asset.setDeviceType(deviceType);
        asset.setDeviceModel(deviceModel.trim());
        asset.setCreatedBy(createdBy);
        asset.setUpdatedBy(createdBy);
        asset.setDeviceStatus(status);
        return asset;
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
        if (!index.containsKey("devicename") && index.containsKey("devicemodel")) {
            index.put("devicename", index.get("devicemodel"));
        }
        if (!index.containsKey("devicemodel") && index.containsKey("devicename")) {
            index.put("devicemodel", index.get("devicename"));
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

    private List<List<String>> parseExcel(InputStream inputStream) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (Row row : sheet) {
                List<String> cells = new ArrayList<>();
                if (row.getLastCellNum() > 0) {
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        cells.add(cell == null ? "" : formatter.formatCellValue(cell));
                    }
                }
                if (!cells.isEmpty() && !(cells.size() == 1 && cells.get(0).isBlank())) {
                    rows.add(cells);
                }
            }
        }
        return rows;
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
            String deviceModel, Long deviceTypeId, String userOfAsset, Long zoneId, String office,
            DeviceStatus deviceStatus) {
        Page<Asset> assetPage = assetRepository.findByFilters(
                blankToNull(assetNumber), blankToNull(serialNumber), blankToNull(deviceModel),
                deviceTypeId, blankToNull(userOfAsset),
                zoneId, blankToNull(office), deviceStatus,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> findSearch(int page, int size, String search) {
        if (search == null || search.isBlank()) {
            return findAllPaged(page, size);
        }
        String term = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        Page<Asset> assetPage = assetRepository.findBySearch(term,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    /**
     * Un-paginated inventory search. The display value "not working" is mapped to
     * the stored enum value NOT_WORKING (otherwise the space/underscore difference
     * would prevent the phrase from ever matching), and "working" is mapped to
     * WORKING so the search does not also catch NOT_WORKING rows. Any other term
     * is matched case-insensitively across every asset field, including the
     * registering user's name.
     */
    /**
     * Result of normalizing a user-supplied search term. The term is wrapped in
     * LIKE wildcards; when the phrase refers to a device status ("working" /
     * "not working") a dedicated enum set is produced so the space/underscore
     * difference between the display value and the stored enum never prevents a match.
     */
    public record SearchParams(String term, boolean statusOn, List<DeviceStatus> statuses) {
    }

    public static SearchParams normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return new SearchParams(null, false, List.of());
        }
        String normalized = search.trim().toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        String term = "%" + normalized + "%";
        boolean statusOn = false;
        List<DeviceStatus> statuses = List.of();
        if (normalized.contains("not working")) {
            statuses = List.of(DeviceStatus.NOT_WORKING);
            statusOn = true;
        } else if (normalized.contains("working")) {
            statuses = List.of(DeviceStatus.WORKING);
            statusOn = true;
        }
        return new SearchParams(term, statusOn, statuses);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> searchAll(String search) {
        SearchParams sp = normalizeSearch(search);
        return assetRepository.searchAll(sp.term(), sp.statusOn(), sp.statuses())
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
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
                "Asset updated: " + saved.getDeviceModel() + " (" + saved.getAssetNumber() + ")");
        log.info("Asset updated: {} ({})", saved.getDeviceModel(), saved.getAssetNumber());
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
        if (request.getZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));
            asset.setZone(zone);
        }
        if (request.getDirectorateId() != null) {
            Directorate directorate = directorateRepository.findById(request.getDirectorateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
            asset.setDirectorate(directorate);
        }
        if (request.getOffice() != null) {
            asset.setOffice(normalizeOffice(request.getOffice()));
        }
        if (request.getUserOfAsset() != null) {
            asset.setUserOfAsset(normalizeUserOfAsset(request.getUserOfAsset()));
        }
        if (request.getDeviceTypeId() != null) {
            DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + request.getDeviceTypeId()));
            asset.setDeviceType(deviceType);
        }
        if (request.getDeviceModel() != null) {
            asset.setDeviceModel(request.getDeviceModel());
        }
        if (request.getDeviceStatus() != null) {
            asset.setDeviceStatus(request.getDeviceStatus());
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