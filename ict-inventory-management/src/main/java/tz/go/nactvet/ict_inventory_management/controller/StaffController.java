package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import tz.go.nactvet.ict_inventory_management.dto.StaffCreateRequest;
import tz.go.nactvet.ict_inventory_management.dto.StaffResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffUpdateRequest;
import tz.go.nactvet.ict_inventory_management.service.StaffService;

@RestController
@RequestMapping("/admin/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffCreateRequest request) {
        StaffResponse response = staffService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> findAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(staffService.findAll(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody StaffUpdateRequest request) {
        return ResponseEntity.ok(staffService.update(id, request));
    }

    @PatchMapping("/{id}/toggle-enabled")
    public ResponseEntity<Void> toggleEnabled(@PathVariable Long id) {
        staffService.toggleEnabled(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        staffService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        staffService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<StaffResponse> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.resetPassword(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
