package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;

public class AssetRequest {

    private String assetNumber;

    private String serialNumber;

    private Long zoneId;

    private Long directorateId;

    @Size(max = 100, message = "office: must not exceed 100 characters")
    private String office;

    @Size(max = 255, message = "userOfAsset: must not exceed 255 characters")
    private String userOfAsset;

    @NotNull(message = "deviceTypeId: must not be null")
    private Long deviceTypeId;

    @NotBlank(message = "deviceModel: must not be blank")
    private String deviceModel;

    @NotNull(message = "deviceStatus: must not be null")
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
