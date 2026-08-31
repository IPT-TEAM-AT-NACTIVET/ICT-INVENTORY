package tz.go.nactvet.ict_inventory_management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import tz.go.nactvet.ict_inventory_management.dto.RegisterRequest;
import tz.go.nactvet.ict_inventory_management.dto.RegisterResponse;
import tz.go.nactvet.ict_inventory_management.dto.UserResponse;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.exception.ResourceNotFoundException;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.security.JwtService;
import tz.go.nactvet.ict_inventory_management.service.StaffService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final StaffService staffService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            StaffService staffService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.staffService = staffService;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse created = staffService.register(request);
        log.info("New staff registered: {}", created.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
