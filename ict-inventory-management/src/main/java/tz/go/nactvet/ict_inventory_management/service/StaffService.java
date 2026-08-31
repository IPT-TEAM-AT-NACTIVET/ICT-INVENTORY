package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.StaffCreateRequest;
import tz.go.nactvet.ict_inventory_management.dto.RegisterRequest;
import tz.go.nactvet.ict_inventory_management.dto.RegisterResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffUpdateRequest;
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
public class StaffService {

    private static final Logger log = LoggerFactory.getLogger(StaffService.class);

    private final UserRepository userRepository;
    private final DirectorateRepository directorateRepository;
    private final SectionRepository sectionRepository;
    private final UnitRepository unitRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final OrganizationalValidator organizationalValidator;
    private final EmployeeIdGenerator employeeIdGenerator;

    public StaffService(UserRepository userRepository,
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

    public StaffResponse create(StaffCreateRequest request) {
        String username = generateUsername(request.getFullName());
        String employeeId = employeeIdGenerator.next();
        String initialPassword = generateInitialPassword(request.getFullName());

        User user = new User();
        user.setEmployeeId(employeeId);
        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(initialPassword));
        user.setRole(Role.STAFF);
        user.setEnabled(true);
        user.setSetupCompleted(false);

        User saved = userRepository.save(user);
        auditLogService.log("CREATE", "STAFF", saved.getId(), "ADMIN", null,
                "Staff account created: " + saved.getFullName() + " (" + saved.getUsername() + ")");
        log.info("Staff account created: {} ({})", saved.getFullName(), saved.getUsername());

        StaffResponse response = toResponse(saved);
        response.setInitialPassword(initialPassword);
        return response;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (request.getConfirmPassword() == null
                || !request.getConfirmPassword().equals(request.getPassword())) {
            throw new BadRequestException("Password and confirm password do not match");
        }

        String email = request.getEmail().trim();
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
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setRole(Role.STAFF);
        user.setEnabled(true);
        user.setSetupCompleted(false);

        Directorate directorate = directorateRepository.findById(request.getDirectorateId())
                .orElseThrow(() -> new ResourceNotFoundException("Directorate not found with id: " + request.getDirectorateId()));
        Section section = null;
        Unit unit = null;

        if (request.getSectionId() != null) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
        }
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));
        }

        organizationalValidator.validate(directorate, section);

        user.setDirectorate(directorate);
        user.setSection(section);
        user.setUnit(unit);
        user.setSetupCompleted(isSetupComplete(user));

        User saved = userRepository.save(user);
        auditLogService.log("REGISTER", "STAFF", saved.getId(), null, null,
                "Staff self-registered: " + saved.getFullName() + " (" + saved.getEmail() + ")");
        log.info("Staff self-registered: {} ({})", saved.getFullName(), saved.getEmail());

        RegisterResponse response = new RegisterResponse();
        response.setMessage("Registration successful. Your account has been created.");
        response.setEmployeeId(saved.getEmployeeId());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());
        response.setStatus(saved.isEnabled() ? "ACTIVE" : "INACTIVE");
        return response;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> findAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STAFF)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffResponse findById(Long id) {
        User user = getStaffOrThrow(id);
        return toResponse(user);
    }

    public StaffResponse update(Long id, StaffUpdateRequest request) {
        User user = getStaffOrThrow(id);

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

        organizationalValidator.validate(directorate, section);
        user.setSetupCompleted(isSetupComplete(user));

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        auditLogService.log("UPDATE", "STAFF", saved.getId(), "ADMIN", null,
                "Staff account updated: " + saved.getFullName() + " (" + saved.getUsername() + ")");
        log.info("Staff account updated: {} ({})", saved.getFullName(), saved.getUsername());
        return toResponse(saved);
    }

    public StaffResponse resetPassword(Long id) {
        User user = getStaffOrThrow(id);
        String newPassword = generateInitialPassword(user.getFullName());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log("RESET_PASSWORD", "STAFF", user.getId(), "ADMIN", null,
                "Initial password regenerated for: " + user.getFullName());
        log.info("Password reset for staff: {} ({})", user.getFullName(), user.getUsername());
        StaffResponse response = toResponse(user);
        response.setInitialPassword(newPassword);
        return response;
    }

    public void toggleEnabled(Long id) {
        User user = getStaffOrThrow(id);
        setStaffEnabled(user, !user.isEnabled());
    }

    public void activate(Long id) {
        User user = getStaffOrThrow(id);
        if (user.isEnabled()) {
            return;
        }
        setStaffEnabled(user, true);
    }

    public void deactivate(Long id) {
        User user = getStaffOrThrow(id);
        if (!user.isEnabled()) {
            return;
        }
        setStaffEnabled(user, false);
    }

    private void setStaffEnabled(User user, boolean enabled) {
        user.setEnabled(enabled);
        userRepository.save(user);
        auditLogService.log(enabled ? "ENABLE" : "DISABLE", "STAFF", user.getId(), "ADMIN", null,
                "Staff account " + (enabled ? "enabled" : "disabled") + ": " + user.getFullName());
        log.info("Staff account {} by admin: {} ({})",
                enabled ? "enabled" : "disabled",
                user.getFullName(), user.getUsername());
    }

    public void delete(Long id) {
        User user = getStaffOrThrow(id);
        long assetCount = assetRepository.countByUserId(id);
        if (assetCount > 0) {
            throw new ConflictException("Cannot delete staff: " + assetCount + " asset(s) are registered to " + user.getFullName() + ". Deactivate the account instead.");
        }
        userRepository.deleteById(id);
        auditLogService.log("DELETE", "STAFF", id, "ADMIN", null,
                "Staff account deleted: " + user.getFullName() + " (" + user.getUsername() + ")");
        log.info("Staff account deleted: {} ({})", user.getFullName(), user.getUsername());
    }

    private User getStaffOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
        if (user.getRole() != Role.STAFF) {
            throw new ResourceNotFoundException("User with id " + id + " is not a staff member");
        }
        return user;
    }

    private boolean isSetupComplete(User user) {
        return user.getEmail() != null && !user.getEmail().isBlank()
                && user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()
                && user.getDirectorate() != null;
    }

    private String generateUsername(String fullName) {
        String base = normalizeUsername(fullName);
        String candidate = base;
        int counter = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + (++counter);
        }
        return candidate;
    }

    private String normalizeUsername(String fullName) {
        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        String name;
        if (parts.length >= 2) {
            name = parts[0] + "." + parts[parts.length - 1];
        } else {
            name = parts.length > 0 ? parts[0] : "";
        }
        name = name.replaceAll("[^a-z0-9.]", "").replaceAll("\\.+", ".");
        return name.isEmpty() ? "user" : name;
    }

    private String generateInitialPassword(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase());
        }
        String namePart = sb.length() == 0 ? "Staff" : sb.toString();
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return namePart + random + "@";
    }

    private StaffResponse toResponse(User user) {
        StaffResponse response = new StaffResponse();
        response.setId(user.getId());
        response.setEmployeeId(user.getEmployeeId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setSetupCompleted(user.isSetupCompleted());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setCreatedAt(user.getCreatedAt());

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