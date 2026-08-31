package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tz.go.nactvet.ict_inventory_management.dto.OfficeResponse;
import tz.go.nactvet.ict_inventory_management.dto.ZoneRequest;
import tz.go.nactvet.ict_inventory_management.dto.ZoneResponse;
import tz.go.nactvet.ict_inventory_management.service.OfficeService;
import tz.go.nactvet.ict_inventory_management.service.ZoneService;

@RestController
@RequestMapping("/admin/zones")
public class ZoneController {

    private final ZoneService zoneService;
    private final OfficeService officeService;

    public ZoneController(ZoneService zoneService, OfficeService officeService) {
        this.zoneService = zoneService;
        this.officeService = officeService;
    }

    @PostMapping
    public ResponseEntity<ZoneResponse> create(@Valid @RequestBody ZoneRequest request) {
        ZoneResponse response = zoneService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ZoneResponse>> findAll() {
        return ResponseEntity.ok(zoneService.findAll());
    }

    @GetMapping("/{zoneId}/offices")
    public ResponseEntity<List<OfficeResponse>> findOfficesByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(officeService.findByZoneId(zoneId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ZoneResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody ZoneRequest request) {
        return ResponseEntity.ok(zoneService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
