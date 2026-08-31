package tz.go.nactvet.ict_inventory_management.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    public static final String CSV_MEDIA_TYPE = "text/csv";

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public ReportService(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    public ReportResponse getInventoryReport(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        List<AssetResponse> assets = findFiltered(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus);

        ReportResponse response = new ReportResponse();
        response.setReportType("inventory");
        response.setTotalAssets(assets.size());
        ReportResponse.ReportItem item = new ReportResponse.ReportItem();
        item.setName("All Assets");
        item.setCount(assets.size());
        item.setAssets(assets);
        response.setItems(List.of(item));
        return response;
    }

    public ReportResponse getReportByDirectorate(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-directorate",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getDirectorateId() != null ? a.getDirectorateId() : -1L,
                    a -> a.getDirectorateId() != null ? a.getDirectorateName() : "Unknown");
        }
        return buildGroupedReport("by-directorate", assetRepository.countByDirectorateGrouped());
    }

    public ReportResponse getReportBySection(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-section",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getSectionId() != null ? a.getSectionId() : -1L,
                    a -> a.getSectionId() != null ? a.getSectionName() : "Unknown");
        }
        return buildGroupedReport("by-section", assetRepository.countBySectionGrouped());
    }

    public ReportResponse getReportByZone(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-zone",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getZoneId() != null ? a.getZoneId() : -1L,
                    a -> a.getZoneId() != null ? a.getZoneName() : "Unknown");
        }
        return buildGroupedReport("by-zone", assetRepository.countByZoneGrouped());
    }

    public ReportResponse getReportByUnit(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-unit",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getUnitId() != null ? a.getUnitId() : -1L,
                    a -> a.getUnitId() != null ? a.getUnitName() : "Unknown");
        }
        return buildGroupedReport("by-unit", assetRepository.countByUnitGrouped());
    }

    public ReportResponse getReportByOffice(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-office",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getOfficeId() != null ? a.getOfficeId() : -1L,
                    a -> a.getOfficeCode() != null ? a.getOfficeCode() : "Unknown");
        }
        return buildGroupedReport("by-office", assetRepository.countByOfficeGrouped());
    }

    public ReportResponse getReportByDeviceType(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-device-type",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getDeviceTypeId() != null ? a.getDeviceTypeId() : -1L,
                    a -> a.getDeviceTypeId() != null ? a.getDeviceTypeName() : "Unknown");
        }
        return buildGroupedReport("by-device-type", assetRepository.countByDeviceTypeGrouped());
    }

    public ReportResponse getReportByStatus(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        if (hasAnyFilter(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId, ownershipType, deviceStatus, verificationStatus)) {
            return buildGroupedReport("by-status",
                    findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                            userId, directorateId, sectionId, unitId, zoneId, officeId,
                            ownershipType, deviceStatus, verificationStatus),
                    a -> a.getVerificationStatus() != null ? a.getVerificationStatus().ordinal() : -1L,
                    a -> a.getVerificationStatus() != null ? a.getVerificationStatus().name() : "Unknown");
        }
        return buildTwoColumnReport("by-status", assetRepository.countByVerificationStatusGrouped());
    }

    public List<AssetResponse> getFilteredAssets(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, Long userId, Long directorateId, Long sectionId, Long unitId,
            Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {
        return findFiltered(assetNumber, serialNumber, deviceName, deviceTypeId, null, null,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus);
    }

    public String exportInventoryCsv(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        List<AssetResponse> assets = findFiltered(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus);

        StringBuilder csv = new StringBuilder();
        csv.append("Asset Number,Serial Number,Device Type,Device Name,Staff Name,Employee ID,Directorate,Section,Unit,Zone,Office,Ownership,Device Status,Verification Status\n");
        for (AssetResponse asset : assets) {
            csv.append(csv(asset.getAssetNumber())).append(',')
               .append(csv(asset.getSerialNumber())).append(',')
               .append(csv(asset.getDeviceTypeName())).append(',')
               .append(csv(asset.getDeviceName())).append(',')
               .append(csv(asset.getUserFullName())).append(',')
               .append(csv(asset.getUserEmployeeId())).append(',')
               .append(csv(asset.getDirectorateName())).append(',')
               .append(csv(asset.getSectionName())).append(',')
               .append(csv(asset.getUnitName())).append(',')
               .append(csv(asset.getZoneName())).append(',')
               .append(csv(asset.getOfficeCode())).append(',')
               .append(csv(asset.getOwnershipType() != null ? asset.getOwnershipType().name() : null)).append(',')
               .append(csv(asset.getDeviceStatus() != null ? asset.getDeviceStatus().name() : null)).append(',')
               .append(csv(asset.getVerificationStatus() != null ? asset.getVerificationStatus().name() : null))
               .append('\n');
        }
        return csv.toString();
    }

    private List<AssetResponse> findFiltered(
            String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {

        return assetRepository.findByFilters(
                blankToNull(assetNumber), blankToNull(serialNumber), blankToNull(deviceName),
                deviceTypeId, blankToNull(employeeId), blankToNull(userName), userId,
                directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus,
                PageRequest.of(0, 10000))
                .getContent()
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean hasAnyFilter(String assetNumber, String serialNumber, String deviceName,
            Long deviceTypeId, String employeeId, String userName, Long userId,
            Long directorateId, Long sectionId, Long unitId, Long zoneId, Long officeId,
            OwnershipType ownershipType, DeviceStatus deviceStatus, VerificationStatus verificationStatus) {
        return notBlank(assetNumber) || notBlank(serialNumber) || notBlank(deviceName)
                || deviceTypeId != null || notBlank(employeeId) || notBlank(userName)
                || userId != null || directorateId != null || sectionId != null || unitId != null || zoneId != null || officeId != null
                || ownershipType != null || deviceStatus != null || verificationStatus != null;
    }

    private ReportResponse buildGroupedReport(String reportType,
            List<AssetResponse> assets,
            java.util.function.Function<AssetResponse, Long> keyFn,
            java.util.function.Function<AssetResponse, String> nameFn) {
        Map<Long, ReportResponse.ReportItem> itemsByKey = new LinkedHashMap<>();
        for (AssetResponse asset : assets) {
            Long key = keyFn.apply(asset);
            ReportResponse.ReportItem item = itemsByKey.computeIfAbsent(key, k -> {
                ReportResponse.ReportItem newItem = new ReportResponse.ReportItem();
                newItem.setId(key < 0 ? null : key);
                newItem.setName(nameFn.apply(asset));
                newItem.setAssets(new ArrayList<>());
                return newItem;
            });
            item.getAssets().add(asset);
        }
        itemsByKey.values().forEach(item -> item.setCount(item.getAssets().size()));
        return buildReportResponse(reportType, new ArrayList<>(itemsByKey.values()));
    }

    private ReportResponse buildGroupedReport(String reportType, List<Object[]> grouped) {
        List<ReportResponse.ReportItem> items = new ArrayList<>();
        long totalCount = 0;
        for (Object[] row : grouped) {
            ReportResponse.ReportItem item = new ReportResponse.ReportItem();
            if (row[0] instanceof Long) {
                item.setId((Long) row[0]);
            }
            item.setName(row[1] != null ? row[1].toString() : "Unknown");
            long count = toLong(row[2]);
            item.setCount(count);
            totalCount += count;
            items.add(item);
        }
        return buildReportResponse(reportType, items, totalCount);
    }

    private ReportResponse buildTwoColumnReport(String reportType, List<Object[]> grouped) {
        List<ReportResponse.ReportItem> items = new ArrayList<>();
        long totalCount = 0;
        for (Object[] row : grouped) {
            ReportResponse.ReportItem item = new ReportResponse.ReportItem();
            item.setName(row[0] != null ? row[0].toString() : "Unknown");
            long count = toLong(row[1]);
            item.setCount(count);
            totalCount += count;
            items.add(item);
        }
        return buildReportResponse(reportType, items, totalCount);
    }

    private ReportResponse buildReportResponse(String reportType, List<ReportResponse.ReportItem> items) {
        long totalCount = items.stream().mapToLong(ReportResponse.ReportItem::getCount).sum();
        return buildReportResponse(reportType, items, totalCount);
    }

    private ReportResponse buildReportResponse(String reportType, List<ReportResponse.ReportItem> items, long totalCount) {
        ReportResponse response = new ReportResponse();
        response.setReportType(reportType);
        response.setItems(items);
        response.setTotalAssets(totalCount);
        return response;
    }

    private long toLong(Object value) {
        return value instanceof Long ? (Long) value : ((Number) value).longValue();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}