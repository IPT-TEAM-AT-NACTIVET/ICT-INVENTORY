package tz.go.nactvet.ict_inventory_management.dto;

import java.util.List;

public class ReportResponse {

    private List<ReportItem> items;
    private String reportType;
    private long totalAssets;

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

    public static class ReportItem {

        private Long id;
        private String name;
        private long count;
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

        public List<AssetResponse> getAssets() {
            return assets;
        }

        public void setAssets(List<AssetResponse> assets) {
            this.assets = assets;
        }
    }
}
