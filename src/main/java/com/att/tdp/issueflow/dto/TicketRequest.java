package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.TicketPriority;
import com.att.tdp.issueflow.entity.TicketStatus;
import com.att.tdp.issueflow.entity.TicketType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TicketStatus status;

    @NotNull
    private TicketPriority priority;

    @NotNull
    private TicketType type;

    @NotNull
    private Long projectId;

    private Long assigneeId;

    private LocalDateTime dueDate;
}
