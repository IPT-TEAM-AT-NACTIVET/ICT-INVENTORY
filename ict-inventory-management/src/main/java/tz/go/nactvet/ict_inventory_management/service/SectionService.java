package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.SectionRequest;
import tz.go.nactvet.ict_inventory_management.dto.SectionResponse;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.Section;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
import tz.go.nactvet.ict_inventory_management.repository.SectionRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final DirectorateRepository directorateRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public SectionService(SectionRepository sectionRepository,
                          DirectorateRepository directorateRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.sectionRepository = sectionRepository;
        this.directorateRepository = directorateRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public SectionResponse create(SectionRequest request) {
        if (sectionRepository.existsByName(request.getName())) {
            throw new ConflictException("Section name already exists: " + request.getName());
        }
        if (request.getCode() != null && sectionRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Section code already exists: " + request.getCode());
        }

        Directorate directorate = directorateRepository.findById(request.getDirectorateId())
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));

        Section section = new Section();
        section.setName(request.getName());
        section.setCode(request.getCode());
        section.setDescription(request.getDescription());
        section.setDirectorate(directorate);

        Section saved = sectionRepository.save(section);
        auditLogService.log("CREATE", "SECTION", saved.getId(), "ADMIN", null,
                "Section created: " + saved.getName() + " in directorate: " + directorate.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findAll() {
        return sectionRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findByDirectorateId(Long directorateId) {
        return sectionRepository.findByDirectorateIdOrderByDirectorateIdAscNameAsc(directorateId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));
        return toResponse(section);
    }

    public SectionResponse update(Long id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        if (sectionRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Section name already exists: " + request.getName());
        }
        if (request.getCode() != null && sectionRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ConflictException("Section code already exists: " + request.getCode());
        }

        Directorate directorate = directorateRepository.findById(request.getDirectorateId())
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));

        section.setName(request.getName());
        section.setCode(request.getCode());
        section.setDescription(request.getDescription());
        section.setDirectorate(directorate);

        Section saved = sectionRepository.save(section);
        auditLogService.log("UPDATE", "SECTION", saved.getId(), "ADMIN", null,
                "Section updated: " + saved.getName());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        long userCount = userRepository.countByRoleAndSectionId(Role.STAFF, id);
        if (userCount > 0) {
            throw new ConflictException("Cannot delete section: " + userCount + " staff members are assigned to it");
        }

        sectionRepository.deleteById(id);
        auditLogService.log("DELETE", "SECTION", id, "ADMIN", null,
                "Section deleted: " + section.getName());
    }

    private SectionResponse toResponse(Section section) {
        SectionResponse response = new SectionResponse();
        response.setId(section.getId());
        response.setName(section.getName());
        response.setCode(section.getCode());
        response.setDescription(section.getDescription());
        if (section.getDirectorate() != null) {
            response.setDirectorateId(section.getDirectorate().getId());
            response.setDirectorateName(section.getDirectorate().getName());
        }
        response.setCreatedAt(section.getCreatedAt());
        response.setUpdatedAt(section.getUpdatedAt());
        return response;
    }
}