package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.TicketDependency;
import com.att.tdp.issueflow.entity.TicketDependencyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketDependencyRepository extends JpaRepository<TicketDependency, TicketDependencyId> {
    List<TicketDependency> findAllByTicketId(Long ticketId);
    boolean existsByTicketIdAndBlockedById(Long ticketId, Long blockedById);
}
