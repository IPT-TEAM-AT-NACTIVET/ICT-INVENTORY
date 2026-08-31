package tz.go.nactvet.ict_inventory_management.dto;

public class StaffDashboardResponse {

    private long totalAssets;
    private long pendingAssets;
    private long verifiedAssets;
    private long rejectedAssets;
    private long activeAssets;
    private long defectiveAssets;

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
}
