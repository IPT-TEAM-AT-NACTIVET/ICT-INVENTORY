package tz.go.nactvet.ict_inventory_management.dto;

import java.util.Map;

public class DashboardResponse {

    private long totalUsers;
    private long activeStaff;
    private long disabledStaff;
    private long totalAssets;
    private long pendingAssets;
    private long verifiedAssets;
    private long rejectedAssets;
    private long activeAssets;
    private long defectiveAssets;
    private Map<String, Long> assetsByDeviceType;
    private Map<String, Long> assetsByDirectorate;
    private Map<String, Long> assetsBySection;
    private Map<String, Long> assetsByZone;
    private Map<String, Long> assetsByVerificationStatus;
    private Map<String, Long> assetsByDeviceStatus;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveStaff() {
        return activeStaff;
    }

    public void setActiveStaff(long activeStaff) {
        this.activeStaff = activeStaff;
    }

    public long getDisabledStaff() {
        return disabledStaff;
    }

    public void setDisabledStaff(long disabledStaff) {
        this.disabledStaff = disabledStaff;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public long getPendingAssets() {
        return pendingAssets;
    }

    public void setPendingAssets(long pendingAssets) {
        this.pendingAssets = pendingAssets;
    }

    public long getVerifiedAssets() {
        return verifiedAssets;
    }

    public void setVerifiedAssets(long verifiedAssets) {
        this.verifiedAssets = verifiedAssets;
    }

    public long getRejectedAssets() {
        return rejectedAssets;
    }

    public void setRejectedAssets(long rejectedAssets) {
        this.rejectedAssets = rejectedAssets;
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

    public Map<String, Long> getAssetsByDirectorate() {
        return assetsByDirectorate;
    }

    public void setAssetsByDirectorate(Map<String, Long> assetsByDirectorate) {
        this.assetsByDirectorate = assetsByDirectorate;
    }

    public Map<String, Long> getAssetsBySection() {
        return assetsBySection;
    }

    public void setAssetsBySection(Map<String, Long> assetsBySection) {
        this.assetsBySection = assetsBySection;
    }

    public Map<String, Long> getAssetsByZone() {
        return assetsByZone;
    }

    public void setAssetsByZone(Map<String, Long> assetsByZone) {
        this.assetsByZone = assetsByZone;
    }

    public Map<String, Long> getAssetsByVerificationStatus() {
        return assetsByVerificationStatus;
    }

    public void setAssetsByVerificationStatus(Map<String, Long> assetsByVerificationStatus) {
        this.assetsByVerificationStatus = assetsByVerificationStatus;
    }

    public Map<String, Long> getAssetsByDeviceStatus() {
        return assetsByDeviceStatus;
    }

    public void setAssetsByDeviceStatus(Map<String, Long> assetsByDeviceStatus) {
        this.assetsByDeviceStatus = assetsByDeviceStatus;
    }
}
