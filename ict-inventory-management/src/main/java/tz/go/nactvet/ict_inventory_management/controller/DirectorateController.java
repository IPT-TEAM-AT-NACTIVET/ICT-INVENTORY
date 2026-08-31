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
import tz.go.nactvet.ict_inventory_management.dto.DirectorateRequest;
import tz.go.nactvet.ict_inventory_management.dto.DirectorateResponse;
import tz.go.nactvet.ict_inventory_management.service.DirectorateService;

@RestController
@RequestMapping("/admin/directorates")
public class DirectorateController {

    private final DirectorateService directorateService;

    public DirectorateController(DirectorateService directorateService) {
        this.directorateService = directorateService;
    }

    @PostMapping
    public ResponseEntity<DirectorateResponse> create(@Valid @RequestBody DirectorateRequest request) {
        DirectorateResponse response = directorateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DirectorateResponse>> findAll() {
        return ResponseEntity.ok(directorateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorateResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(directorateService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectorateResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody DirectorateRequest request) {
        return ResponseEntity.ok(directorateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        directorateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}