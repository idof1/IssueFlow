package com.att.tdp.issueflow.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class TicketDependencyId implements Serializable {
    private Long ticketId;
    private Long blockedById;
}
