package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByCreatedAtDesc();
    List<AuditLog> findAllByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}
