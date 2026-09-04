package tz.go.nactvet.ict_inventory_management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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
import tz.go.nactvet.ict_inventory_management.dto.UserManagementCreateRequest;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementResponse;
import tz.go.nactvet.ict_inventory_management.dto.UserResponse;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.security.JwtService;
import tz.go.nactvet.ict_inventory_management.service.UserManagementService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserManagementService userManagementService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            UserManagementService userManagementService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserManagementResponse> register(@Valid @RequestBody UserManagementCreateRequest request) {
        UserManagementResponse response = userManagementService.registerSelf(request);
        log.info("New registration submitted for approval: {}", request.getEmail());
        return ResponseEntity.status(201).body(response);
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

        log.info("User '{}' logged in successfully", user.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }
}
