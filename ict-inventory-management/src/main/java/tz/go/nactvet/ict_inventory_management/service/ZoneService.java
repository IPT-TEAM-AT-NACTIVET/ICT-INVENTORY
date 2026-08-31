package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.ZoneRequest;
import tz.go.nactvet.ict_inventory_management.dto.ZoneResponse;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;

@Service
@Transactional
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ZoneService(ZoneRepository zoneRepository,
                       UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.zoneRepository = zoneRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public ZoneResponse create(ZoneRequest request) {
        if (zoneRepository.existsByName(request.getName())) {
            throw new ConflictException("Zone name already exists: " + request.getName());
        }
        if (request.getCode() != null && zoneRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Zone code already exists: " + request.getCode());
        }

        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setCode(request.getCode());
        zone.setDescription(request.getDescription());
        zone.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        Zone saved = zoneRepository.save(zone);
        auditLogService.log("CREATE", "ZONE", saved.getId(), "ADMIN", null,
                "Zone created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> findAll() {
        return zoneRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ZoneResponse findById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        return toResponse(zone);
    }

    public ZoneResponse update(Long id, ZoneRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        if (zoneRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Zone name already exists: " + request.getName());
        }
        if (request.getCode() != null && zoneRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException("Zone code already exists: " + request.getCode());
        }

        zone.setName(request.getName());
        zone.setCode(request.getCode());
        zone.setDescription(request.getDescription());
        zone.setStatus(request.getStatus() != null ? request.getStatus() : zone.getStatus());

        Zone saved = zoneRepository.save(zone);
        auditLogService.log("UPDATE", "ZONE", saved.getId(), "ADMIN", null,
                "Zone updated: " + saved.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        long userCount = userRepository.countByRoleAndZoneId(Role.STAFF, id);
        if (userCount > 0) {
            throw new ConflictException("Cannot delete zone: " + userCount + " staff members are assigned to it");
        }

        long officeCount = zone.getOffices().size();
        if (officeCount > 0) {
            throw new ConflictException("Cannot delete zone: " + officeCount + " offices are assigned to it");
        }

        zoneRepository.deleteById(id);
        auditLogService.log("DELETE", "ZONE", id, "ADMIN", null,
                "Zone deleted: " + zone.getName());
    }

    private ZoneResponse toResponse(Zone zone) {
        ZoneResponse response = new ZoneResponse();
        response.setId(zone.getId());
        response.setName(zone.getName());
        response.setCode(zone.getCode());
        response.setDescription(zone.getDescription());
        response.setStatus(zone.getStatus());
        response.setOfficeCount((long) zone.getOffices().size());
        response.setCreatedAt(zone.getCreatedAt());
        response.setUpdatedAt(zone.getUpdatedAt());
        return response;
    }
}