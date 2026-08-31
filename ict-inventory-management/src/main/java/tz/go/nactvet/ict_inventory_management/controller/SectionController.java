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
import tz.go.nactvet.ict_inventory_management.dto.SectionRequest;
import tz.go.nactvet.ict_inventory_management.dto.SectionResponse;
import tz.go.nactvet.ict_inventory_management.service.SectionService;

@RestController
@RequestMapping("/admin/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        SectionResponse response = sectionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SectionResponse>> findAll() {
        return ResponseEntity.ok(sectionService.findAll());
    }

    @GetMapping("/directorate/{directorateId}")
    public ResponseEntity<List<SectionResponse>> findByDirectorateId(@PathVariable Long directorateId) {
        return ResponseEntity.ok(sectionService.findByDirectorateId(directorateId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}