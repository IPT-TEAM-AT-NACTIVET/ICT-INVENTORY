package tz.go.nactvet.ict_inventory_management.dto;

import java.time.LocalDateTime;

public class RecentAssetDto {

    private String assetNumber;
    private String deviceName;
    private String deviceType;
    private String userOfAsset;
    private String zone;
    private String office;
    private String registeredBy;
    private LocalDateTime registeredAt;

    public RecentAssetDto() {
    }

    public RecentAssetDto(String assetNumber, String deviceName, String deviceType, String userOfAsset,
                          String zone, String office, String registeredBy, LocalDateTime registeredAt) {
        this.assetNumber = assetNumber;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.userOfAsset = userOfAsset;
        this.zone = zone;
        this.office = office;
        this.registeredBy = registeredBy;
        this.registeredAt = registeredAt;
    }

    public String getAssetNumber() {
        return assetNumber;
    }

    public void setAssetNumber(String assetNumber) {
        this.assetNumber = assetNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getUserOfAsset() {
        return userOfAsset;
    }

    public void setUserOfAsset(String userOfAsset) {
        this.userOfAsset = userOfAsset;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(String registeredBy) {
        this.registeredBy = registeredBy;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
