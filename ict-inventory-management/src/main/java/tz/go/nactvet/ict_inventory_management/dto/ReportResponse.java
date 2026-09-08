package tz.go.nactvet.ict_inventory_management.dto;

import java.util.List;

public class ReportResponse {

    private List<ReportItem> items;
    private String reportType;
    private long totalAssets;
    private ReportSummaryResponse summary;
    private List<AssetResponse> assets;

    public List<ReportItem> getItems() {
        return items;
    }

    public void setItems(List<ReportItem> items) {
        this.items = items;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public ReportSummaryResponse getSummary() {
        return summary;
    }

    public void setSummary(ReportSummaryResponse summary) {
        this.summary = summary;
    }

    public List<AssetResponse> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetResponse> assets) {
        this.assets = assets;
    }

    public static class ReportItem {

        private Long id;
        private String name;
        private long count;
        private long working;
        private long notWorking;
        private List<AssetResponse> assets;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getWorking() {
            return working;
        }

        public void setWorking(long working) {
            this.working = working;
        }

        public long getNotWorking() {
            return notWorking;
        }

        public void setNotWorking(long notWorking) {
            this.notWorking = notWorking;
        }

        public List<AssetResponse> getAssets() {
            return assets;
        }

        public void setAssets(List<AssetResponse> assets) {
            this.assets = assets;
        }
    }
}
