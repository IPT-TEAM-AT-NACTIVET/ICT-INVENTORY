package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.DeviceTypeRequest;
import tz.go.nactvet.ict_inventory_management.dto.DeviceTypeResponse;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;

@Service
@Transactional
public class DeviceTypeService {

    private static final Logger log = LoggerFactory.getLogger(DeviceTypeService.class);

    private final DeviceTypeRepository deviceTypeRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    public DeviceTypeService(DeviceTypeRepository deviceTypeRepository,
                             AssetRepository assetRepository,
                             AuditLogService auditLogService) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.assetRepository = assetRepository;
        this.auditLogService = auditLogService;
    }

    public DeviceTypeResponse create(DeviceTypeRequest request) {
        if (deviceTypeRepository.existsByName(request.getName())) {
            throw new ConflictException("Device type name already exists: " + request.getName());
        }

        DeviceType deviceType = new DeviceType();
        deviceType.setName(request.getName());
        deviceType.setDescription(request.getDescription());

        DeviceType saved = deviceTypeRepository.save(deviceType);
        auditLogService.log("CREATE", "DEVICE_TYPE", saved.getId(), "ADMIN", null,
                "Device type created: " + saved.getName());
        log.info("Device type created: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DeviceTypeResponse> findAll() {
        return deviceTypeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceTypeResponse findById(Long id) {
        DeviceType deviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + id));
        return toResponse(deviceType);
    }

    public DeviceTypeResponse update(Long id, DeviceTypeRequest request) {
        DeviceType deviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + id));

        if (deviceTypeRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Device type name already exists: " + request.getName());
        }

        deviceType.setName(request.getName());
        deviceType.setDescription(request.getDescription());

        DeviceType saved = deviceTypeRepository.save(deviceType);
        auditLogService.log("UPDATE", "DEVICE_TYPE", saved.getId(), "ADMIN", null,
                "Device type updated: " + saved.getName());
        log.info("Device type updated: {}", saved.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        DeviceType deviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device type not found with id: " + id));

        long assetCount = assetRepository.countByDeviceTypeId(id);
        if (assetCount > 0) {
            throw new ConflictException("Cannot delete device type: " + assetCount + " assets reference it");
        }

        deviceTypeRepository.deleteById(id);
        auditLogService.log("DELETE", "DEVICE_TYPE", id, "ADMIN", null,
                "Device type deleted: " + deviceType.getName());
        log.info("Device type deleted: id={}", id);
    }

    private DeviceTypeResponse toResponse(DeviceType deviceType) {
        DeviceTypeResponse response = new DeviceTypeResponse();
        response.setId(deviceType.getId());
        response.setName(deviceType.getName());
        response.setDescription(deviceType.getDescription());
        response.setCreatedAt(deviceType.getCreatedAt());
        response.setUpdatedAt(deviceType.getUpdatedAt());
        return response;
    }
}
