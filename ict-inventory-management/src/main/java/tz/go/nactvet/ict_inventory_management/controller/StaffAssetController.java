package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.StaffAssetRequest;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService;
import tz.go.nactvet.ict_inventory_management.service.AssetService;

@RestController
@RequestMapping("/assets")
public class StaffAssetController {

    private final AssetService assetService;

    public StaffAssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody StaffAssetRequest request,
                                                Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        AssetResponse response = assetService.createByStaff(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AssetResponse>> getMyAssets(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(assetService.findByUserId(principal.getId()));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<AssetResponse> getMyAssetById(@PathVariable Long id, Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        AssetResponse asset = assetService.findById(id);
        if (!principal.getId().equals(asset.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(asset);
    }

    @PutMapping("/my/{id}")
    public ResponseEntity<AssetResponse> updateMyAsset(@PathVariable Long id,
                                                      @Valid @RequestBody AssetUpdateRequest request,
                                                      Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(assetService.updateByStaff(id, request, principal.getId()));
    }
}
