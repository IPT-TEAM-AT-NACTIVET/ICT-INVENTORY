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
        response.setDeviceStatus(asset.getDeviceStatus());
        response.setUserOfAsset(asset.getUserOfAsset());
        response.setCreatedAt(asset.getCreatedAt());
        response.setUpdatedAt(asset.getUpdatedAt());

        if (asset.getDeviceType() != null) {
            response.setDeviceTypeId(asset.getDeviceType().getId());
            response.setDeviceTypeName(asset.getDeviceType().getName());
        }
        response.setDeviceModel(asset.getDeviceModel());

        if (asset.getZone() != null) {
            response.setZoneId(asset.getZone().getId());
            response.setZoneName(asset.getZone().getName());
        }

        if (asset.getDirectorate() != null) {
            response.setDirectorateId(asset.getDirectorate().getId());
            response.setDirectorateName(asset.getDirectorate().getName());
        }
        response.setOffice(asset.getOffice());

        if (asset.getCreatedBy() != null) {
            response.setCreatedById(asset.getCreatedBy().getId());
            response.setCreatedByName(asset.getCreatedBy().getFullName());
        }
        if (asset.getUpdatedBy() != null) {
            response.setUpdatedById(asset.getUpdatedBy().getId());
            response.setUpdatedByName(asset.getUpdatedBy().getFullName());
        }

        return response;
    }
}