package com.att.tdp.issueflow.scheduler;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.TicketPriority;
import com.att.tdp.issueflow.entity.TicketStatus;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscalationScheduler {

    private final TicketRepository ticketRepository;
    private final AuditLogService auditLogService;

    @Scheduled(cron = "${scheduling.escalation.cron}")
    @Transactional
    public void escalateOverdueTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> overdueTickets = ticketRepository
                .findAllByDeletedAtIsNullAndDueDateBeforeAndStatusNot(now, TicketStatus.DONE);

        for (Ticket ticket : overdueTickets) {
            TicketPriority oldPriority = ticket.getPriority();
            if (oldPriority == TicketPriority.CRITICAL) {
                if (!ticket.isOverdue()) {
                    ticket.setOverdue(true);
                    ticketRepository.save(ticket);
                    auditLogService.log("TICKET", ticket.getId(), "MARK_OVERDUE", "SYSTEM", "SYSTEM",
                            "Ticket is overdue and at CRITICAL priority");
                }
            } else {
                TicketPriority newPriority = escalate(oldPriority);
                ticket.setPriority(newPriority);
                if (newPriority == TicketPriority.CRITICAL) {
                    ticket.setOverdue(true);
                }
                ticketRepository.save(ticket);
                auditLogService.log("TICKET", ticket.getId(), "AUTO_ESCALATE", "SYSTEM", "SYSTEM",
                        "Priority escalated from " + oldPriority + " to " + newPriority);
            }
        }
    }

    private TicketPriority escalate(TicketPriority priority) {
        return switch (priority) {
            case LOW -> TicketPriority.MEDIUM;
            case MEDIUM -> TicketPriority.HIGH;
            case HIGH -> TicketPriority.CRITICAL;
            case CRITICAL -> TicketPriority.CRITICAL;
        };
    }
}
