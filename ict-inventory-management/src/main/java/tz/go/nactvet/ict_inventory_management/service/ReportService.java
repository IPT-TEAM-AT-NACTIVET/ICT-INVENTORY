package tz.go.nactvet.ict_inventory_management.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportSummaryResponse;
import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public ReportService(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    /**
     * Inventory report with server-side search and pagination. Each asset retains
     * its audit information (registered by / registered at).
     */
    public PagedResponse<AssetResponse> getInventoryReport(String search, int page, int size) {
        String term = like(search);
        Page<Asset> assetPage = assetRepository.findBySearch(term,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AssetResponse> content = assetPage.getContent().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, assetPage.getNumber(), assetPage.getSize(),
                assetPage.getTotalElements(), assetPage.getTotalPages());
    }

    /**
     * Summary cards: total assets and counts broken down by status and ownership.
     * Runs the global search before aggregating.
     */
    public ReportSummaryResponse getSummary(String search) {
        String term = like(search);
        ReportSummaryResponse response = new ReportSummaryResponse();
        response.setTotalAssets(assetRepository.countWithSearch(term));

        long active = 0;
        long defective = 0;
        for (Object[] row : assetRepository.countByDeviceStatusGroupedWithSearch(term)) {
            if (row[0] == null) {
                continue;
            }
            DeviceStatus status = (DeviceStatus) row[0];
            long count = toLong(row[1]);
            if (status == DeviceStatus.ACTIVE) {
                active = count;
            } else if (status == DeviceStatus.DEFECTIVE) {
                defective = count;
            }
        }
        response.setActiveAssets(active);
        response.setDefectiveAssets(defective);

        long office = 0;
        long personal = 0;
        for (Object[] row : assetRepository.countByOwnershipGroupedWithSearch(term)) {
            if (row[0] == null) {
                continue;
            }
            OwnershipType type = (OwnershipType) row[0];
            long count = toLong(row[1]);
            if (type == OwnershipType.OFFICE) {
                office = count;
            } else if (type == OwnershipType.PERSONAL) {
                personal = count;
            }
        }
        response.setOfficeAssets(office);
        response.setPersonalAssets(personal);
        return response;
    }

    public ReportResponse getReportByZone(String search) {
        return buildGroupedReport("by-zone", assetRepository.countByZoneGroupedWithSearch(like(search)));
    }

    public ReportResponse getReportByOffice(String search) {
        return buildTwoColumnReport("by-office", assetRepository.countByOfficeGroupedWithSearch(like(search)));
    }

    public ReportResponse getReportByDeviceType(String search) {
        return buildGroupedReport("by-device-type", assetRepository.countByDeviceTypeGroupedWithSearch(like(search)));
    }

    public ReportResponse getReportByStatus(String search) {
        return buildTwoColumnReport("by-status", assetRepository.countByDeviceStatusGroupedWithSearch(like(search)));
    }

    public ReportResponse getReportByOwnership(String search) {
        return buildTwoColumnReport("by-ownership", assetRepository.countByOwnershipGroupedWithSearch(like(search)));
    }

    public List<AssetResponse> getFilteredAssets(String search) {
        return assetRepository.findBySearch(like(search), PageRequest.of(0, 10000))
                .getContent()
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    public String exportInventoryCsv(String search) {
        List<AssetResponse> assets = getFilteredAssets(search);
        StringBuilder csv = new StringBuilder();
        csv.append("Asset Number,Serial Number,Device Type,Device Name,User of Asset,Zone,Office,Ownership,Device Status\n");
        for (AssetResponse asset : assets) {
            csv.append(csv(asset.getAssetNumber())).append(',')
               .append(csv(asset.getSerialNumber())).append(',')
               .append(csv(asset.getDeviceTypeName())).append(',')
               .append(csv(asset.getDeviceName())).append(',')
               .append(csv(asset.getUserOfAsset())).append(',')
               .append(csv(asset.getZoneName())).append(',')
               .append(csv(asset.getOffice())).append(',')
               .append(csv(asset.getOwnershipType() != null ? asset.getOwnershipType().name() : null)).append(',')
               .append(csv(asset.getDeviceStatus() != null ? asset.getDeviceStatus().name() : null))
               .append('\n');
        }
        return csv.toString();
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

    private ReportResponse buildReportResponse(String reportType, List<ReportResponse.ReportItem> items, long totalCount) {
        ReportResponse response = new ReportResponse();
        response.setReportType(reportType);
        response.setItems(items);
        response.setTotalAssets(totalCount);
        return response;
    }

    private long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return ((Number) value).longValue();
    }

    private String like(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
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
