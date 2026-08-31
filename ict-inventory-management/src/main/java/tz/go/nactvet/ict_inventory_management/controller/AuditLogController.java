package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tz.go.nactvet.ict_inventory_management.dto.AuditLogResponse;
import tz.go.nactvet.ict_inventory_management.service.AuditLogService;

@RestController
@RequestMapping("/admin/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> findAll() {
        return ResponseEntity.ok(auditLogService.findAll());
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponse>> findByEntity(@PathVariable String entityType,
                                                               @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.findByEntityTypeAndEntityId(entityType, entityId));
    }

    @GetMapping("/recent/{limit}")
    public ResponseEntity<List<AuditLogResponse>> findRecent(@PathVariable int limit) {
        return ResponseEntity.ok(auditLogService.findRecent(limit));
    }
}
