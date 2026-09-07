package tz.go.nactvet.ict_inventory_management.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportFilterOptionsResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportSummaryResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public ReportService(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    /**
     * Single entry point for the Reports module. Fetches the full filtered asset
     * list once, then derives the KPI summary, grouped report items and the
     * detailed asset list from that exact same dataset so they can never diverge.
     */
    public ReportResponse getReportData(String search, Long deviceTypeId, String status,
            String ownershipType, Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to, String groupBy) {

        List<AssetResponse> assets = getFilteredAssets(search, deviceTypeId, status,
                ownershipType, zoneId, office, userOfAsset, registeredBy, from, to);

        ReportSummaryResponse summary = buildSummary(assets);

        List<ReportResponse.ReportItem> items;
        if (isGrouped(groupBy)) {
            items = buildGroupedItems(assets, groupBy);
        } else {
            items = List.of();
        }

        ReportResponse response = new ReportResponse();
        response.setReportType(groupBy == null ? "overview" : groupBy);
        response.setTotalAssets(assets.size());
        response.setSummary(summary);
        response.setAssets(assets);
        response.setItems(items);
        return response;
    }

    public ReportFilterOptionsResponse getFilterOptions() {
        List<String> offices = assetRepository.findDistinctOffices();

        ReportFilterOptionsResponse response = new ReportFilterOptionsResponse();
        response.setOffices(offices);
        response.setUsersOfAsset(assetRepository.findDistinctUsersOfAsset());

        List<ReportFilterOptionsResponse.RegistrarOption> registrars = assetRepository.findDistinctRegistrars()
                .stream()
                .map(r -> {
                    ReportFilterOptionsResponse.RegistrarOption o = new ReportFilterOptionsResponse.RegistrarOption();
                    if (r[0] instanceof Long) {
                        o.setId((Long) r[0]);
                    }
                    o.setName(r[1] != null ? r[1].toString() : "Unknown");
                    return o;
                })
                .collect(Collectors.toList());
        response.setRegisteredBy(registrars);
        return response;
    }

    public String exportCsv(String search, Long deviceTypeId, String status,
            String ownershipType, Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to) {

        List<AssetResponse> assets = getFilteredAssets(search, deviceTypeId, status,
                ownershipType, zoneId, office, userOfAsset, registeredBy, from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("Asset Number,Serial Number,Device Type,Device Model,User of Asset,")
           .append("Zone,Office,Ownership,Device Status,Registered By,Registered At\n");
        for (AssetResponse a : assets) {
            csv.append(csv(a.getAssetNumber())).append(',')
               .append(csv(a.getSerialNumber())).append(',')
               .append(csv(a.getDeviceTypeName())).append(',')
               .append(csv(a.getDeviceModel())).append(',')
               .append(csv(a.getUserOfAsset())).append(',')
               .append(csv(a.getZoneName())).append(',')
               .append(csv(a.getOffice())).append(',')
               .append(csv(a.getOwnershipType() != null ? a.getOwnershipType().name() : null)).append(',')
               .append(csv(a.getDeviceStatus() != null ? a.getDeviceStatus().name() : null)).append(',')
               .append(csv(a.getCreatedByName())).append(',')
               .append(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
               .append('\n');
        }
        return csv.toString();
    }

    // ---------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------

    private List<AssetResponse> getFilteredAssets(String search, Long deviceTypeId, String status,
            String ownershipType, Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to) {

        AssetService.SearchParams sp = AssetService.normalizeSearch(search);
        String termLike = sp.term() != null ? sp.term() : "%%";
        DeviceStatus statusFilter = parseStatus(status);
        OwnershipType ownershipFilter = parseOwnership(ownershipType);
        String officeLike = likeOrMatchAll(office);
        String userOfAssetLike = likeOrMatchAll(userOfAsset);
        String registeredByLike = likeOrMatchAll(registeredBy);
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDateTime.of(1, 1, 1, 0, 0);
        LocalDateTime toDt = to != null ? to.plusDays(1).atStartOfDay() : LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        return assetRepository.findForReport(
                        termLike, sp.statusOn(), sp.statuses(),
                        deviceTypeId, ownershipFilter, statusFilter,
                        zoneId, officeLike, userOfAssetLike, registeredByLike, fromDt, toDt)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    private ReportSummaryResponse buildSummary(List<AssetResponse> assets) {
        ReportSummaryResponse s = new ReportSummaryResponse();
        s.setTotalAssets(assets.size());
        s.setActiveAssets(assets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.WORKING).count());
        s.setDefectiveAssets(assets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.NOT_WORKING).count());
        s.setOfficeAssets(assets.stream().filter(a -> a.getOwnershipType() == OwnershipType.OFFICE).count());
        s.setPersonalAssets(assets.stream().filter(a -> a.getOwnershipType() == OwnershipType.PERSONAL).count());
        return s;
    }

    private boolean isGrouped(String groupBy) {
        return groupBy != null && !groupBy.isBlank()
                && !groupBy.equals("overview") && !groupBy.equals("registration");
    }

    private List<ReportResponse.ReportItem> buildGroupedItems(List<AssetResponse> assets, String groupBy) {
        Map<String, List<AssetResponse>> grouped = assets.stream()
                .collect(Collectors.groupingBy(a -> groupKey(a, groupBy)));

        return grouped.entrySet().stream()
                .map(entry -> {
                    ReportResponse.ReportItem item = new ReportResponse.ReportItem();
                    item.setName(entry.getKey());
                    List<AssetResponse> groupAssets = entry.getValue();
                    item.setCount(groupAssets.size());
                    item.setWorking(groupAssets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.WORKING).count());
                    item.setNotWorking(groupAssets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.NOT_WORKING).count());
                    item.setOfficeCount(groupAssets.stream().filter(a -> a.getOwnershipType() == OwnershipType.OFFICE).count());
                    item.setPersonalCount(groupAssets.stream().filter(a -> a.getOwnershipType() == OwnershipType.PERSONAL).count());
                    groupAssets.stream().findFirst().ifPresent(first -> {
                        switch (groupBy) {
                            case "by-zone" -> item.setId(first.getZoneId());
                            case "by-device-type" -> item.setId(first.getDeviceTypeId());
                            default -> item.setId(null);
                        }
                    });
                    item.setAssets(new ArrayList<>(groupAssets));
                    return item;
                })
                .sorted(Comparator.comparing(ReportResponse.ReportItem::getCount).reversed()
                        .thenComparing(ReportResponse.ReportItem::getName))
                .collect(Collectors.toList());
    }

    private String groupKey(AssetResponse a, String groupBy) {
        return switch (groupBy) {
            case "by-zone" -> a.getZoneName() != null ? a.getZoneName() : "Unknown";
            case "by-office" -> a.getOffice() != null ? a.getOffice() : "Unknown";
            case "by-device-type" -> a.getDeviceTypeName() != null ? a.getDeviceTypeName() : "Unknown";
            case "by-status" -> a.getDeviceStatus() != null ? a.getDeviceStatus().name() : "Unknown";
            case "by-ownership" -> a.getOwnershipType() != null ? a.getOwnershipType().name() : "Unknown";
            case "by-user" -> a.getUserOfAsset() != null ? a.getUserOfAsset() : "Unknown";
            default -> "Unknown";
        };
    }

    private DeviceStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return DeviceStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private OwnershipType parseOwnership(String ownershipType) {
        if (ownershipType == null || ownershipType.isBlank()) {
            return null;
        }
        try {
            return OwnershipType.valueOf(ownershipType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String blankToLike(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    /**
     * LIKE pattern that, when unfiltered, becomes {@code %%} (matches everything)
     * instead of {@code null}. This avoids PostgreSQL's "could not determine data
     * type of parameter" error, which occurs when a LIKE bound-parameter is null.
     */
    private String likeOrMatchAll(String value) {
        if (value == null || value.isBlank()) {
            return "%%";
        }
        return "%" + value.trim().toLowerCase() + "%";
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
