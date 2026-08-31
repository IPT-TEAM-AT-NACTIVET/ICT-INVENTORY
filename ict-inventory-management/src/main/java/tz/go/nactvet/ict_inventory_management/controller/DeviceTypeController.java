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
import tz.go.nactvet.ict_inventory_management.dto.DeviceTypeRequest;
import tz.go.nactvet.ict_inventory_management.dto.DeviceTypeResponse;
import tz.go.nactvet.ict_inventory_management.service.DeviceTypeService;

@RestController
@RequestMapping("/admin/device-types")
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    public DeviceTypeController(DeviceTypeService deviceTypeService) {
        this.deviceTypeService = deviceTypeService;
    }

    @PostMapping
    public ResponseEntity<DeviceTypeResponse> create(@Valid @RequestBody DeviceTypeRequest request) {
        DeviceTypeResponse response = deviceTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DeviceTypeResponse>> findAll() {
        return ResponseEntity.ok(deviceTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceTypeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody DeviceTypeRequest request) {
        return ResponseEntity.ok(deviceTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
