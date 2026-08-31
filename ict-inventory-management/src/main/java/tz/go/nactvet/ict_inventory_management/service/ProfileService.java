package tz.go.nactvet.ict_inventory_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.ProfileResponse;
import tz.go.nactvet.ict_inventory_management.dto.ProfileUpdateRequest;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.Section;
import tz.go.nactvet.ict_inventory_management.entity.Unit;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
import tz.go.nactvet.ict_inventory_management.repository.SectionRepository;
import tz.go.nactvet.ict_inventory_management.repository.UnitRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final DirectorateRepository directorateRepository;
    private final SectionRepository sectionRepository;
    private final UnitRepository unitRepository;
    private final OrganizationalValidator organizationalValidator;

    public ProfileService(UserRepository userRepository,
                          DirectorateRepository directorateRepository,
                          SectionRepository sectionRepository,
                          UnitRepository unitRepository,
                          OrganizationalValidator organizationalValidator) {
        this.userRepository = userRepository;
        this.directorateRepository = directorateRepository;
        this.sectionRepository = sectionRepository;
        this.unitRepository = unitRepository;
        this.organizationalValidator = organizationalValidator;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return toResponse(user);
    }

    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new ConflictException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        Directorate directorate = user.getDirectorate();
        Section section = user.getSection();
        Unit unit = user.getUnit();

        if (request.getDirectorateId() != null) {
            directorate = directorateRepository.findById(request.getDirectorateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
            user.setDirectorate(directorate);
        }
        if (request.getSectionId() != null) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
            user.setSection(section);
        }
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
            user.setUnit(unit);
        }

        organizationalValidator.validate(directorate, section);
        user.setSetupCompleted(isSetupComplete(user));

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", saved.getUsername());
        return toResponse(saved);
    }

    private boolean isSetupComplete(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank()
                && user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()
                && user.getDirectorate() != null;
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setEmployeeId(user.getEmployeeId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setSetupCompleted(user.isSetupCompleted());
        response.setRole(user.getRole().name());
        response.setEnabled(user.isEnabled());

        if (user.getDirectorate() != null) {
            response.setDirectorateId(user.getDirectorate().getId());
            response.setDirectorateName(user.getDirectorate().getName());
        }
        if (user.getSection() != null) {
            response.setSectionId(user.getSection().getId());
            response.setSectionName(user.getSection().getName());
        }
        if (user.getUnit() != null) {
            response.setUnitId(user.getUnit().getId());
            response.setUnitName(user.getUnit().getName());
        }

        return response;
    }
}