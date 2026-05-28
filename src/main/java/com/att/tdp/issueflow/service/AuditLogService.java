package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String entityType, Long entityId, String action, String actor, String actorType, String details) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(actor)
                .actorType(actorType)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll(String entityType, Long entityId, String action, String actor) {
        List<AuditLog> results;
        if (entityType != null && entityId != null) {
            results = auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        } else if (entityType != null) {
            results = auditLogRepository.findAllByEntityTypeOrderByCreatedAtDesc(entityType);
        } else {
            results = auditLogRepository.findAllByOrderByCreatedAtDesc();
        }
        if (action != null) results = results.stream().filter(l -> action.equals(l.getAction())).toList();
        if (actor != null)  results = results.stream().filter(l -> actor.equals(l.getActor())).toList();
        return results;
    }
}
