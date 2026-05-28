package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.TicketPriority;
import com.att.tdp.issueflow.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByProjectIdAndDeletedAtIsNull(Long projectId);
    List<Ticket> findAllByProjectIdAndDeletedAtIsNotNull(Long projectId);
    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

    List<Ticket> findAllByDeletedAtIsNullAndDueDateBeforeAndStatusNot(
            LocalDateTime now, TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.projectId = :projectId AND t.assigneeId = :userId AND t.status <> :done AND t.deletedAt IS NULL")
    long countOpenTickets(@Param("projectId") Long projectId,
                          @Param("userId") Long userId,
                          @Param("done") TicketStatus done);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Ticket t WHERE t.projectId = :projectId AND t.status <> :done AND t.deletedAt IS NULL AND t.assigneeId IS NOT NULL GROUP BY t.assigneeId")
    List<Object[]> countOpenTicketsByAssignee(@Param("projectId") Long projectId,
                                               @Param("done") TicketStatus done);

    List<Ticket> findAllByProjectIdAndDeletedAtIsNullAndPriorityNotAndDueDateBefore(
            Long projectId, TicketPriority priority, LocalDateTime now);
}
