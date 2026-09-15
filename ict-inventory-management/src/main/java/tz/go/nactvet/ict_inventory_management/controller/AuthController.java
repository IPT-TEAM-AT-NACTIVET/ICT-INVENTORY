package tz.go.nactvet.ict_inventory_management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tz.go.nactvet.ict_inventory_management.dto.LoginRequest;
import tz.go.nactvet.ict_inventory_management.dto.LoginResponse;
import tz.go.nactvet.ict_inventory_management.dto.UserResponse;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService.UserPrincipal;
import tz.go.nactvet.ict_inventory_management.security.JwtService;
import tz.go.nactvet.ict_inventory_management.service.AuditLogService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(userDetails, user.getId(), user.getRole().name());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isSetupCompleted(),
                user.getRole());

        auditLogService.log("LOGIN", "USER", user.getId(), user.getUsername(), user.getId(),
                "User logged in: " + user.getEmail());
        log.info("User '{}' logged in successfully", user.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            Long userId = principal.getId();
            String username = principal.getUsername();
            auditLogService.log("LOGOUT", "USER", userId, username, userId,
                    "User logged out: " + username);
            log.info("User '{}' logged out", username);
        }
        return ResponseEntity.ok().build();
    }
}
