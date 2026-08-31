package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.DirectorateRequest;
import tz.go.nactvet.ict_inventory_management.dto.DirectorateResponse;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
import tz.go.nactvet.ict_inventory_management.repository.SectionRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional
public class DirectorateService {

    private final DirectorateRepository directorateRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DirectorateService(DirectorateRepository directorateRepository,
                              SectionRepository sectionRepository,
                              UserRepository userRepository,
                              AuditLogService auditLogService) {
        this.directorateRepository = directorateRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public DirectorateResponse create(DirectorateRequest request) {
        if (directorateRepository.existsByName(request.getName())) {
            throw new ConflictException("Directorate name already exists: " + request.getName());
        }
        if (request.getCode() != null && directorateRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Directorate code already exists: " + request.getCode());
        }

        Directorate directorate = new Directorate();
        directorate.setName(request.getName());
        directorate.setCode(request.getCode());
        directorate.setDescription(request.getDescription());

        Directorate saved = directorateRepository.save(directorate);
        auditLogService.log("CREATE", "DIRECTORATE", saved.getId(), "ADMIN", null,
                "Directorate created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DirectorateResponse> findAll() {
        return directorateRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DirectorateResponse findById(Long id) {
        Directorate directorate = directorateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + id));
        return toResponse(directorate);
    }

    public DirectorateResponse update(Long id, DirectorateRequest request) {
        Directorate directorate = directorateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + id));

        if (directorateRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Directorate name already exists: " + request.getName());
        }
        if (request.getCode() != null && directorateRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException("Directorate code already exists: " + request.getCode());
        }

        directorate.setName(request.getName());
        directorate.setCode(request.getCode());
        directorate.setDescription(request.getDescription());

        Directorate saved = directorateRepository.save(directorate);
        auditLogService.log("UPDATE", "DIRECTORATE", saved.getId(), "ADMIN", null,
                "Directorate updated: " + saved.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Directorate directorate = directorateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + id));

        long userCount = userRepository.countByRoleAndDirectorateId(Role.STAFF, id);
        if (userCount > 0) {
            throw new ConflictException("Cannot delete directorate: " + userCount + " staff members are assigned to it");
        }

        if (!sectionRepository.findByDirectorateIdOrderByDirectorateIdAscNameAsc(id).isEmpty()) {
            throw new ConflictException("Cannot delete directorate: it has associated sections");
        }

        directorateRepository.deleteById(id);
        auditLogService.log("DELETE", "DIRECTORATE", id, "ADMIN", null,
                "Directorate deleted: " + directorate.getName());
    }

    private DirectorateResponse toResponse(Directorate directorate) {
        DirectorateResponse response = new DirectorateResponse();
        response.setId(directorate.getId());
        response.setName(directorate.getName());
        response.setCode(directorate.getCode());
        response.setDescription(directorate.getDescription());
        response.setCreatedAt(directorate.getCreatedAt());
        response.setUpdatedAt(directorate.getUpdatedAt());
        return response;
    }
}