package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.UnitRequest;
import tz.go.nactvet.ict_inventory_management.dto.UnitResponse;
import tz.go.nactvet.ict_inventory_management.entity.Unit;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.UnitRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional
public class UnitService {

    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public UnitService(UnitRepository unitRepository,
                       UserRepository userRepository,
                       AuditLogService auditLogService) {
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public UnitResponse create(UnitRequest request) {
        if (unitRepository.existsByName(request.getName())) {
            throw new ConflictException("Unit name already exists: " + request.getName());
        }
        if (request.getCode() != null && unitRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Unit code already exists: " + request.getCode());
        }

        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setCode(request.getCode());
        unit.setDescription(request.getDescription());

        Unit saved = unitRepository.save(unit);
        auditLogService.log("CREATE", "UNIT", saved.getId(), "ADMIN", null,
                "Unit created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> findAll() {
        return unitRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnitResponse findById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));
        return toResponse(unit);
    }

    public UnitResponse update(Long id, UnitRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));

        if (unitRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Unit name already exists: " + request.getName());
        }
        if (request.getCode() != null && unitRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException("Unit code already exists: " + request.getCode());
        }

        unit.setName(request.getName());
        unit.setCode(request.getCode());
        unit.setDescription(request.getDescription());

        Unit saved = unitRepository.save(unit);
        auditLogService.log("UPDATE", "UNIT", saved.getId(), "ADMIN", null,
                "Unit updated: " + saved.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + id));

        long userCount = userRepository.countByRoleAndUnitId(Role.ADMIN, id);
        if (userCount > 0) {
            throw new ConflictException("Cannot delete unit: " + userCount + " user accounts are assigned to it");
        }

        unitRepository.deleteById(id);
        auditLogService.log("DELETE", "UNIT", id, "ADMIN", null,
                "Unit deleted: " + unit.getName());
    }

    private UnitResponse toResponse(Unit unit) {
        UnitResponse response = new UnitResponse();
        response.setId(unit.getId());
        response.setName(unit.getName());
        response.setCode(unit.getCode());
        response.setDescription(unit.getDescription());
        response.setCreatedAt(unit.getCreatedAt());
        response.setUpdatedAt(unit.getUpdatedAt());
        return response;
    }
}