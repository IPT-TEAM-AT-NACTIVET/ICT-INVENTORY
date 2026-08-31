package tz.go.nactvet.ict_inventory_management.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.DashboardResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffDashboardResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public DashboardService(AssetRepository assetRepository, UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getAdminDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalUsers(userRepository.count());
        response.setActiveStaff(userRepository.countByRoleAndEnabled(Role.STAFF, true));
        response.setDisabledStaff(userRepository.countByRoleAndEnabled(Role.STAFF, false));
        response.setTotalAssets(assetRepository.count());
        response.setPendingAssets(assetRepository.countByVerificationStatus(VerificationStatus.PENDING));
        response.setVerifiedAssets(assetRepository.countByVerificationStatus(VerificationStatus.VERIFIED));
        response.setRejectedAssets(assetRepository.countByVerificationStatus(VerificationStatus.REJECTED));
        response.setActiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.ACTIVE));
        response.setDefectiveAssets(assetRepository.countByDeviceStatus(DeviceStatus.DEFECTIVE));

        response.setAssetsByDeviceType(toMap(assetRepository.countByDeviceTypeGrouped()));
        response.setAssetsByDirectorate(toMap(assetRepository.countByDirectorateGrouped()));
        response.setAssetsBySection(toMap(assetRepository.countBySectionGrouped()));
        response.setAssetsByZone(toMap(assetRepository.countByZoneGrouped()));
        response.setAssetsByVerificationStatus(toEnumMap(assetRepository.countByVerificationStatusGrouped()));
        response.setAssetsByDeviceStatus(toEnumMap(assetRepository.countByDeviceStatusGrouped()));

        return response;
    }

    public StaffDashboardResponse getStaffDashboard(Long userId) {
        StaffDashboardResponse response = new StaffDashboardResponse();

        response.setTotalAssets(assetRepository.countByUserId(userId));
        response.setPendingAssets(assetRepository.countByUserIdAndVerificationStatus(userId, VerificationStatus.PENDING));
        response.setVerifiedAssets(assetRepository.countByUserIdAndVerificationStatus(userId, VerificationStatus.VERIFIED));
        response.setRejectedAssets(assetRepository.countByUserIdAndVerificationStatus(userId, VerificationStatus.REJECTED));
        response.setActiveAssets(assetRepository.countByUserIdAndDeviceStatus(userId, DeviceStatus.ACTIVE));
        response.setDefectiveAssets(assetRepository.countByUserIdAndDeviceStatus(userId, DeviceStatus.DEFECTIVE));

        return response;
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

    private Map<String, Long> toEnumMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : results) {
            String key = row[0] != null ? row[0].toString() : "Unknown";
            Long count = row[1] instanceof Long ? (Long) row[1] : ((Number) row[1]).longValue();
            map.put(key, count);
        }
        return map;
    }
}
