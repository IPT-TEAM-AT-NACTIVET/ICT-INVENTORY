package tz.go.nactvet.ict_inventory_management.dto;

public class ReportSummaryResponse {

    private long totalAssets;
    private long activeAssets;
    private long defectiveAssets;
    private long officeAssets;
    private long personalAssets;

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

    public long getOfficeAssets() {
        return officeAssets;
    }

    public void setOfficeAssets(long officeAssets) {
        this.officeAssets = officeAssets;
    }

    public long getPersonalAssets() {
        return personalAssets;
    }

    public void setPersonalAssets(long personalAssets) {
        this.personalAssets = personalAssets;
    }
}
