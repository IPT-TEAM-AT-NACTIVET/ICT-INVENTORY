package tz.go.nactvet.ict_inventory_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.AuditLogResponse;
import tz.go.nactvet.ict_inventory_management.entity.AuditLog;
import tz.go.nactvet.ict_inventory_management.repository.AuditLogRepository;

@Service
@Transactional
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String entityType, Long entityId, String performedBy, Long performedByUserId, String description) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, performedBy, performedByUserId, description);
        auditLogRepository.save(auditLog);
        log.debug("Audit log: {} {} {} by {}", action, entityType, entityId, performedBy);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEntityTypeAndEntityId(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findRecent(int limit) {
        return auditLogRepository.findRecentLogs(PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setAction(auditLog.getAction());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setPerformedBy(auditLog.getPerformedBy());
        response.setPerformedByUserId(auditLog.getPerformedByUserId());
        response.setDescription(auditLog.getDescription());
        response.setCreatedAt(auditLog.getCreatedAt());
        return response;
    }
}
