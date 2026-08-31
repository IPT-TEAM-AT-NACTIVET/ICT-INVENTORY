package tz.go.nactvet.ict_inventory_management.controller;

import org.springframework.http.HttpStatus;
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
import tz.go.nactvet.ict_inventory_management.dto.AssetRequest;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.dto.RejectRequest;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService;
import tz.go.nactvet.ict_inventory_management.service.AssetService;
import tz.go.nactvet.ict_inventory_management.service.AssetVerificationService;

@RestController
@RequestMapping("/admin/assets")
public class AdminAssetController {

    private final AssetService assetService;
    private final AssetVerificationService assetVerificationService;

    public AdminAssetController(AssetService assetService, AssetVerificationService assetVerificationService) {
        this.assetService = assetService;
        this.assetVerificationService = assetVerificationService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        AssetResponse response = assetService.createByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AssetResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(assetService.findFiltered(
                page, size, assetNumber, serialNumber, deviceName, deviceTypeId,
                employeeId, userName, userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody AssetUpdateRequest request) {
        return ResponseEntity.ok(assetService.updateByAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<PagedResponse<AssetResponse>> findPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return findAll(page, size, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, VerificationStatus.PENDING);
    }

    @GetMapping("/verified")
    public ResponseEntity<PagedResponse<AssetResponse>> findVerified(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return findAll(page, size, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, VerificationStatus.VERIFIED);
    }

    @GetMapping("/rejected")
    public ResponseEntity<PagedResponse<AssetResponse>> findRejected(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return findAll(page, size, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, VerificationStatus.REJECTED);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<AssetResponse> verify(@PathVariable Long id, Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        AssetResponse response = assetVerificationService.verify(id, principal.getUsername(), principal.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<AssetResponse> reject(@PathVariable Long id,
                                                @Valid @RequestBody RejectRequest request,
                                                Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        AssetResponse response = assetVerificationService.reject(id, request, principal.getUsername(), principal.getId());
        return ResponseEntity.ok(response);
    }
}