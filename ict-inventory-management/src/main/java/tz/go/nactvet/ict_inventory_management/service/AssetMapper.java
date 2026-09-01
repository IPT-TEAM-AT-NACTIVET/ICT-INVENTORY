package tz.go.nactvet.ict_inventory_management.service;

import org.springframework.stereotype.Component;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.entity.Asset;

@Component
public class AssetMapper {

    public AssetResponse toResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        response.setId(asset.getId());
        response.setAssetNumber(asset.getAssetNumber());
        response.setSerialNumber(asset.getSerialNumber());
        response.setDeviceName(asset.getDeviceName());
        response.setOwnershipType(asset.getOwnershipType());
        response.setDeviceStatus(asset.getDeviceStatus());
        response.setVerificationStatus(asset.getVerificationStatus());
        response.setRejectionReason(asset.getRejectionReason());
        response.setCreatedAt(asset.getCreatedAt());
        response.setUpdatedAt(asset.getUpdatedAt());

        if (asset.getDeviceType() != null) {
            response.setDeviceTypeId(asset.getDeviceType().getId());
            response.setDeviceTypeName(asset.getDeviceType().getName());
        }
        if (asset.getUser() != null) {
            response.setUserId(asset.getUser().getId());
            response.setUserFullName(asset.getUser().getFullName());
            response.setUserEmployeeId(asset.getUser().getEmployeeId());
            response.setUserEmail(asset.getUser().getEmail());
            response.setUserPhoneNumber(asset.getUser().getPhoneNumber());

            if (asset.getUser().getDirectorate() != null) {
                response.setDirectorateId(asset.getUser().getDirectorate().getId());
                response.setDirectorateName(asset.getUser().getDirectorate().getName());
            }
            if (asset.getUser().getSection() != null) {
                response.setSectionId(asset.getUser().getSection().getId());
                response.setSectionName(asset.getUser().getSection().getName());
            }
            if (asset.getUser().getUnit() != null) {
                response.setUnitId(asset.getUser().getUnit().getId());
                response.setUnitName(asset.getUser().getUnit().getName());
            }
        }

        if (asset.getZone() != null) {
            response.setZoneId(asset.getZone().getId());
            response.setZoneName(asset.getZone().getName());
        }
        response.setOffice(asset.getOffice());

        return response;
    }
}