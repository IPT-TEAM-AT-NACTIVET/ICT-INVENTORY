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
import tz.go.nactvet.ict_inventory_management.dto.UnitRequest;
import tz.go.nactvet.ict_inventory_management.dto.UnitResponse;
import tz.go.nactvet.ict_inventory_management.service.UnitService;

@RestController
@RequestMapping("/admin/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @PostMapping
    public ResponseEntity<UnitResponse> create(@Valid @RequestBody UnitRequest request) {
        UnitResponse response = unitService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UnitResponse>> findAll() {
        return ResponseEntity.ok(unitService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
