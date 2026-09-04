package tz.go.nactvet.ict_inventory_management.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private long totalAssets;
    private long activeAssets;
    private long defectiveAssets;
    private Map<String, Long> assetsByDeviceType;
    private Map<String, Long> assetsByZone;
    private Map<String, Long> assetsByDeviceStatus;
    private Map<String, Long> assetsByOwnership;
    private List<RecentAssetDto> recentAssets = new ArrayList<>();

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public long getActiveAssets() {
        return activeAssets;
    }

    public void setActiveAssets(long activeAssets) {
        this.activeAssets = activeAssets;
    }

    public long getDefectiveAssets() {
        return defectiveAssets;
    }

    public void setDefectiveAssets(long defectiveAssets) {
        this.defectiveAssets = defectiveAssets;
    }

    public Map<String, Long> getAssetsByDeviceType() {
        return assetsByDeviceType;
    }

    public void setAssetsByDeviceType(Map<String, Long> assetsByDeviceType) {
        this.assetsByDeviceType = assetsByDeviceType;
    }

    public Map<String, Long> getAssetsByZone() {
        return assetsByZone;
    }

    public void setAssetsByZone(Map<String, Long> assetsByZone) {
        this.assetsByZone = assetsByZone;
    }

    public Map<String, Long> getAssetsByDeviceStatus() {
        return assetsByDeviceStatus;
    }

    public void setAssetsByDeviceStatus(Map<String, Long> assetsByDeviceStatus) {
        this.assetsByDeviceStatus = assetsByDeviceStatus;
    }

    public Map<String, Long> getAssetsByOwnership() {
        return assetsByOwnership;
    }

    public void setAssetsByOwnership(Map<String, Long> assetsByOwnership) {
        this.assetsByOwnership = assetsByOwnership;
    }

    public List<RecentAssetDto> getRecentAssets() {
        return recentAssets;
    }

    public void setRecentAssets(List<RecentAssetDto> recentAssets) {
        this.recentAssets = recentAssets;
    }
}
