package com.att.tdp.issueflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_dependencies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ticket_id", "blocked_by_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(TicketDependencyId.class)
public class TicketDependency {

    @Id
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Id
    @Column(name = "blocked_by_id", nullable = false)
    private Long blockedById;
}
