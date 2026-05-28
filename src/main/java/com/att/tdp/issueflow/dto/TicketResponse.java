package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.TicketPriority;
import com.att.tdp.issueflow.entity.TicketStatus;
import com.att.tdp.issueflow.entity.TicketType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketType type;
    private Long projectId;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private boolean isOverdue;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static TicketResponse from(Ticket t) {
        TicketResponse r = new TicketResponse();
        r.id = t.getId();
        r.title = t.getTitle();
        r.description = t.getDescription();
        r.status = t.getStatus();
        r.priority = t.getPriority();
        r.type = t.getType();
        r.projectId = t.getProjectId();
        r.assigneeId = t.getAssigneeId();
        r.dueDate = t.getDueDate();
        r.isOverdue = t.isOverdue();
        r.version = t.getVersion();
        r.createdAt = t.getCreatedAt();
        r.deletedAt = t.getDeletedAt();
        return r;
    }
}
