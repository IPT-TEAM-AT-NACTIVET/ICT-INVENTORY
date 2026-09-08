package tz.go.nactvet.ict_inventory_management.dto;

import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;

public class AssetUpdateRequest {

    private String assetNumber;
    private String serialNumber;
    private Long zoneId;
    private Long directorateId;
    private String office;
    private String userOfAsset;
    private Long deviceTypeId;
    private String deviceModel;
    private DeviceStatus deviceStatus;

    public String getAssetNumber() {
        return assetNumber;
    }

    public void setAssetNumber(String assetNumber) {
        this.assetNumber = assetNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getDirectorateId() {
        return directorateId;
    }

    public void setDirectorateId(Long directorateId) {
        this.directorateId = directorateId;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getUserOfAsset() {
        return userOfAsset;
    }

    public void setUserOfAsset(String userOfAsset) {
        this.userOfAsset = userOfAsset;
    }

    public Long getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}
