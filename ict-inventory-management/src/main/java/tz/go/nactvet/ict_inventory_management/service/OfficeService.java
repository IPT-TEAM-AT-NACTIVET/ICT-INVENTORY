package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.OfficeRequest;
import tz.go.nactvet.ict_inventory_management.dto.OfficeResponse;
import tz.go.nactvet.ict_inventory_management.entity.Office;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.OfficeRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;

@Service
@Transactional
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final ZoneRepository zoneRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    public OfficeService(OfficeRepository officeRepository,
                         ZoneRepository zoneRepository,
                         AssetRepository assetRepository,
                         AuditLogService auditLogService) {
        this.officeRepository = officeRepository;
        this.zoneRepository = zoneRepository;
        this.assetRepository = assetRepository;
        this.auditLogService = auditLogService;
    }

    public OfficeResponse create(OfficeRequest request) {
        Zone zone = loadZone(request.getZoneId());
        String officeCode = normalizeCode(request.getOfficeCode());

        if (officeRepository.existsByZoneIdAndOfficeCode(zone.getId(), officeCode)) {
            throw new ConflictException("Office code '" + officeCode + "' already exists in zone '" + zone.getName() + "'");
        }

        Office office = new Office();
        office.setZone(zone);
        office.setOfficeCode(officeCode);
        office.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        Office saved = officeRepository.save(office);
        auditLogService.log("CREATE", "OFFICE", saved.getId(), "ADMIN", null,
                "Office created: code " + saved.getOfficeCode() + " in zone " + zone.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OfficeResponse> findAll() {
        return officeRepository.findAllByOrderByZoneNameAscOfficeCodeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfficeResponse> findByZoneId(Long zoneId) {
        return officeRepository.findByZoneIdOrderByOfficeCodeAsc(zoneId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfficeResponse findById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));
        return toResponse(office);
    }

    public OfficeResponse update(Long id, OfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));
        Zone zone = loadZone(request.getZoneId());
        String officeCode = normalizeCode(request.getOfficeCode());

        if (officeRepository.existsByZoneIdAndOfficeCodeAndIdNot(zone.getId(), officeCode, id)) {
            throw new ConflictException("Office code '" + officeCode + "' already exists in zone '" + zone.getName() + "'");
        }

        office.setZone(zone);
        office.setOfficeCode(officeCode);
        office.setStatus(request.getStatus() != null ? request.getStatus() : office.getStatus());

        Office saved = officeRepository.save(office);
        auditLogService.log("UPDATE", "OFFICE", saved.getId(), "ADMIN", null,
                "Office updated: code " + saved.getOfficeCode() + " in zone " + zone.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));

        long assetCount = assetRepository.countByOfficeId(id);
        if (assetCount > 0) {
            throw new ConflictException("Cannot delete office: " + assetCount + " assets are located in it");
        }

        officeRepository.deleteById(id);
        auditLogService.log("DELETE", "OFFICE", id, "ADMIN", null,
                "Office deleted: code " + office.getOfficeCode() + " in zone " + office.getZone().getName());
    }

    private Zone loadZone(Long zoneId) {
        if (zoneId == null) {
            throw new ConflictException("Zone is required");
        }
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));
    }

    private String normalizeCode(String officeCode) {
        if (officeCode == null || officeCode.trim().isEmpty()) {
            throw new ConflictException("Office code is required");
        }
        return officeCode.trim();
    }

    private OfficeResponse toResponse(Office office) {
        OfficeResponse response = new OfficeResponse();
        response.setId(office.getId());
        if (office.getZone() != null) {
            response.setZoneId(office.getZone().getId());
            response.setZoneName(office.getZone().getName());
        }
        response.setOfficeCode(office.getOfficeCode());
        response.setStatus(office.getStatus());
        response.setCreatedAt(office.getCreatedAt());
        response.setUpdatedAt(office.getUpdatedAt());
        return response;
    }
}