package tz.go.nactvet.ict_inventory_management.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.DashboardResponse;
import tz.go.nactvet.ict_inventory_management.dto.RecentAssetDto;
import tz.go.nactvet.ict_inventory_management.dto.UserDashboardResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int RECENT_ASSETS_LIMIT = 8;

    private final AssetRepository assetRepository;

    public DashboardService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public DashboardResponse getAdminDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalAssets(assetRepository.count());
        response.setActiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.ACTIVE));
        response.setDefectiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.DEFECTIVE));

        response.setAssetsByDeviceType(toMap(assetRepository.countByDeviceTypeGrouped()));
        response.setAssetsByZone(toMap(assetRepository.countByZoneGrouped()));
        response.setAssetsByDeviceStatus(toSimpleMap(assetRepository.countByDeviceStatusGrouped()));
        response.setAssetsByOwnership(toSimpleMap(assetRepository.countByOwnershipGrouped()));

        response.setRecentAssets(toRecentAssets(assetRepository.findRecentRegistrations(
                PageRequest.of(0, RECENT_ASSETS_LIMIT))));

        return response;
    }

    public UserDashboardResponse getUserDashboard(Long userId) {
        UserDashboardResponse response = new UserDashboardResponse();

        response.setTotalAssets(assetRepository.count());
        response.setActiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.ACTIVE));
        response.setDefectiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.DEFECTIVE));

        response.setAssetsByDeviceType(toMap(assetRepository.countByDeviceTypeGrouped()));
        response.setAssetsByZone(toMap(assetRepository.countByZoneGrouped()));
        response.setAssetsByDeviceStatus(toSimpleMap(assetRepository.countByDeviceStatusGrouped()));
        response.setAssetsByOwnership(toSimpleMap(assetRepository.countByOwnershipGrouped()));

        response.setRecentAssets(toRecentAssets(assetRepository.findRecentRegistrations(
                PageRequest.of(0, RECENT_ASSETS_LIMIT))));

        return response;
    }

    private List<RecentAssetDto> toRecentAssets(List<Object[]> results) {
        List<RecentAssetDto> list = new ArrayList<>();
        for (Object[] row : results) {
            RecentAssetDto dto = new RecentAssetDto(
                    str(row[0]),
                    str(row[1]),
                    str(row[2]),
                    str(row[3]),
                    str(row[4]),
                    str(row[5]),
                    str(row[6]),
                    (java.time.LocalDateTime) row[7]);
            list.add(dto);
        }
        return list;
    }

    private String str(Object value) {
        return value != null ? value.toString() : null;
    }

    private Map<String, Long> toMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            String key = row[1] != null ? row[1].toString() : "Unknown";
            Long count = row[2] instanceof Long ? (Long) row[2] : ((Number) row[2]).longValue();
            map.put(key, count);
        }
        return map;
    }

    private Map<String, Long> toSimpleMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            String key = row[0] != null ? row[0].toString() : "Unknown";
            Long count = row[1] instanceof Long ? (Long) row[1] : ((Number) row[1]).longValue();
            map.put(key, count);
        }
        return map;
    }
}
