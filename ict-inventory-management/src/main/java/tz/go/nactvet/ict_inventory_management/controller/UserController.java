package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementCreateRequest;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementResponse;
import tz.go.nactvet.ict_inventory_management.dto.UserManagementUpdateRequest;
import tz.go.nactvet.ict_inventory_management.service.UserManagementService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public ResponseEntity<UserManagementResponse> create(
            @Valid @RequestBody UserManagementCreateRequest request) {
        return ResponseEntity.ok(userManagementService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<UserManagementResponse>> findAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(userManagementService.findAll(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserManagementResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserManagementResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UserManagementUpdateRequest request) {
        return ResponseEntity.ok(userManagementService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-enabled")
    public ResponseEntity<Void> toggleEnabled(@PathVariable Long id, Authentication authentication) {
        userManagementService.toggleEnabled(id, actorId(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id, Authentication authentication) {
        userManagementService.activate(id, actorId(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, Authentication authentication) {
        userManagementService.deactivate(id, actorId(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<UserManagementResponse> approve(@PathVariable Long id, Authentication authentication) {
        userManagementService.activate(id, actorId(authentication));
        return ResponseEntity.ok(userManagementService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        userManagementService.delete(id, actorId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long actorId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService.UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
