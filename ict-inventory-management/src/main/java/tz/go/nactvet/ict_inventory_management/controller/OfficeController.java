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
import tz.go.nactvet.ict_inventory_management.dto.OfficeRequest;
import tz.go.nactvet.ict_inventory_management.dto.OfficeResponse;
import tz.go.nactvet.ict_inventory_management.service.OfficeService;

@RestController
@RequestMapping("/admin/offices")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @PostMapping
    public ResponseEntity<OfficeResponse> create(@Valid @RequestBody OfficeRequest request) {
        OfficeResponse response = officeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OfficeResponse>> findAll() {
        return ResponseEntity.ok(officeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(officeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficeResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody OfficeRequest request) {
        return ResponseEntity.ok(officeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        officeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
