package tz.go.nactvet.ict_inventory_management.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.UserManagementCreateRequest;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementResponse;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementUpdateRequest;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.Section;
import tz.go.nactvet.ict_inventory_management.entity.Unit;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.exception.BadRequestException;
import tz.go.nactvet.ict_inventory_management.exception.ConflictException;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
import tz.go.nactvet.ict_inventory_management.repository.SectionRepository;
import tz.go.nactvet.ict_inventory_management.repository.UnitRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
@Transactional
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final UserRepository userRepository;
    private final DirectorateRepository directorateRepository;
    private final SectionRepository sectionRepository;
    private final UnitRepository unitRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final OrganizationalValidator organizationalValidator;
    private final EmployeeIdGenerator employeeIdGenerator;

    public UserManagementService(UserRepository userRepository,
                        DirectorateRepository directorateRepository,
                        SectionRepository sectionRepository,
                        UnitRepository unitRepository,
                        AssetRepository assetRepository,
                        PasswordEncoder passwordEncoder,
                        AuditLogService auditLogService,
                        OrganizationalValidator organizationalValidator,
                        EmployeeIdGenerator employeeIdGenerator) {
        this.userRepository = userRepository;
        this.directorateRepository = directorateRepository;
        this.sectionRepository = sectionRepository;
        this.unitRepository = unitRepository;
        this.assetRepository = assetRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.organizationalValidator = organizationalValidator;
        this.employeeIdGenerator = employeeIdGenerator;
    }

    /**
     * Self-registration: creates a new user account with role ADMIN, PENDING status.
     * The account remains disabled until approved by an active ADMIN.
     */
    public UserManagementResponse registerSelf(UserManagementCreateRequest request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email address is already registered.");
        }

        String username = email;
        String employeeId = employeeIdGenerator.next();

        User user = new User();
        user.setEmployeeId(employeeId);
        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setRole(Role.ADMIN);
        user.setEnabled(false);
        user.setSetupCompleted(false);

        if (request.getDirectorateId() != null) {
            Directorate directorate = directorateRepository.findById(request.getDirectorateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
            user.setDirectorate(directorate);
        }
        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
            user.setSection(section);
        }
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
            user.setUnit(unit);
        }

        User saved = userRepository.save(user);
        auditLogService.log("CREATE", "USER", saved.getId(), "ADMIN", null,
                "User account registered awaiting approval: " + saved.getFullName() + " (" + saved.getEmail() + ")");
        log.info("User account registered awaiting approval: {} ({})", saved.getFullName(), saved.getEmail());
        return toResponse(saved);
    }

    /**
     * Admin creates a new user account (enabled immediately).
     */
    public UserManagementResponse create(UserManagementCreateRequest request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email address is already registered.");
        }

        String username = email;
        String employeeId = employeeIdGenerator.next();

        User user = new User();
        user.setEmployeeId(employeeId);
        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        user.setSetupCompleted(false);

        Directorate directorate = null;
        Section section = null;
        Unit unit = null;

        if (request.getDirectorateId() != null) {
            directorate = directorateRepository.findById(request.getDirectorateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
        }
        if (request.getSectionId() != null) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
        }
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
        }

        if (directorate != null) {
            organizationalValidator.validate(directorate, section);
        }

        user.setDirectorate(directorate);
        user.setSection(section);
        user.setUnit(unit);
        user.setSetupCompleted(isSetupComplete(user));

        User saved = userRepository.save(user);
        auditLogService.log("CREATE", "USER", saved.getId(), "ADMIN", null,
                "User account created: " + saved.getFullName() + " (" + saved.getEmail() + ")");
        log.info("User account created: {} ({})", saved.getFullName(), saved.getEmail());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserManagementResponse> findAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserManagementResponse> findAll(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }
        String term = search.trim();
        List<User> dbResults = userRepository.searchUsers(null, term);
        return dbResults.stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .filter(u -> matchesSearch(u, term))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(User user, String term) {
        String lower = term.toLowerCase();
        if (user.getFullName() != null && user.getFullName().toLowerCase().contains(lower)) {
            return true;
        }
        if (user.getEmail() != null && user.getEmail().toLowerCase().contains(lower)) {
            return true;
        }
        if (user.getEmployeeId() != null && user.getEmployeeId().toLowerCase().contains(lower)) {
            return true;
        }
        if (user.getFullName() != null) {
            String[] parts = user.getFullName().trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    initials.append(Character.toLowerCase(part.charAt(0)));
                }
            }
            if (initials.toString().contains(lower)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public UserManagementResponse findById(Long id) {
        User user = getUserOrThrow(id);
        return toResponse(user);
    }

    public UserManagementResponse update(Long id, UserManagementUpdateRequest request) {
        User user = getUserOrThrow(id);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new ConflictException("Email already exists: " + request.getEmail());
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

        if (user.getDirectorate() != null) {
            organizationalValidator.validate(user.getDirectorate(), user.getSection());
        }
        user.setSetupCompleted(isSetupComplete(user));

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        auditLogService.log("UPDATE", "USER", saved.getId(), "ADMIN", null,
                "User account updated: " + saved.getFullName() + " (" + saved.getUsername() + ")");
        log.info("User account updated: {} ({})", saved.getFullName(), saved.getUsername());
        return toResponse(saved);
    }

    public void activate(Long id, Long actorId) {
        User user = getUserOrThrow(id);
        if (user.isEnabled()) {
            return;
        }
        setUserEnabled(user, true, actorId);
    }

    public void deactivate(Long id, Long actorId) {
        User user = getUserOrThrow(id);
        if (!user.isEnabled()) {
            return;
        }
        setUserEnabled(user, false, actorId);
    }

    public void toggleEnabled(Long id, Long actorId) {
        User user = getUserOrThrow(id);
        if (user.isEnabled()) {
            deactivate(id, actorId);
        } else {
            activate(id, actorId);
        }
    }

    private void setUserEnabled(User user, boolean enabled, Long actorId) {
        if (actorId != null && actorId.equals(user.getId())) {
            throw new BadRequestException("You cannot approve or deactivate your own account.");
        }
        user.setEnabled(enabled);
        if (enabled) {
            User actor = actorId == null ? null : getUserOrThrow(actorId);
            user.setApprovedBy(actor);
            user.setApprovedAt(LocalDateTime.now());
        }
        userRepository.save(user);
        User actor = actorId == null ? null : getUserOrThrow(actorId);
        String actorName = actor != null ? actor.getFullName() : "ADMIN";
        auditLogService.log(enabled ? "ENABLE" : "DISABLE", "USER", user.getId(), "APPROVAL", actorId,
                "User account " + (enabled ? "enabled (approved)" : "disabled") + " by " + actorName
                        + ": " + user.getFullName());
        log.info("User account {} by {}: {} ({})",
                enabled ? "enabled (approved)" : "disabled",
                actorName,
                user.getFullName(), user.getUsername());
    }

    public void delete(Long id, Long actorId) {
        User user = getUserOrThrow(id);
        if (actorId != null && actorId.equals(id)) {
            throw new BadRequestException("You cannot delete your own account.");
        }
        long assetCount = assetRepository.countByCreatedById(id);
        if (assetCount > 0) {
            throw new ConflictException("User cannot be deleted because assets are currently assigned to this user. "
                    + "Please handle the asset assignment first according to the existing inventory workflow.");
        }
        userRepository.deleteById(id);
        auditLogService.log("DELETE", "USER", id, "ADMIN", null,
                "User account deleted: " + user.getFullName() + " (" + user.getUsername() + ")");
        log.info("User account deleted: {} ({})", user.getFullName(), user.getUsername());
    }

    private User getUserOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return user;
    }

    private boolean isSetupComplete(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank()
                && user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()
                && user.getDirectorate() != null;
    }

    private UserManagementResponse toResponse(User user) {
        UserManagementResponse response = new UserManagementResponse();
        response.setId(user.getId());
        response.setEmployeeId(user.getEmployeeId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setSetupCompleted(user.isSetupCompleted());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getApprovedBy() != null) {
            response.setApprovedById(user.getApprovedBy().getId());
            response.setApprovedByName(user.getApprovedBy().getFullName());
        }
        if (user.getApprovedAt() != null) {
            response.setApprovedAt(user.getApprovedAt());
        }

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
