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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.web.multipart.MultipartFile;
import tz.go.nactvet.ict_inventory_management.dto.AssetRequest;
import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.AssetUpdateRequest;
import tz.go.nactvet.ict_inventory_management.dto.CsvImportResult;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService;
import tz.go.nactvet.ict_inventory_management.service.AssetService;

@RestController
@RequestMapping("/admin/assets")
public class AdminAssetController {

    private final AssetService assetService;

    public AdminAssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request,
                                                Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        AssetResponse response = assetService.createByAdmin(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/import")
    public ResponseEntity<CsvImportResult> importCsv(@RequestPart("file") MultipartFile file,
                                                     Authentication authentication) throws IOException {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(assetService.importCsv(csvContent, principal.getId()));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AssetResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(assetService.findSearch(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody AssetUpdateRequest request,
                                                Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(assetService.updateByAdmin(id, request, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}