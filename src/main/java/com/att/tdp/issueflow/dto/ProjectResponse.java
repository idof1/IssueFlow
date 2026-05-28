package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Project;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.ownerId = p.getOwnerId();
        r.createdAt = p.getCreatedAt();
        r.deletedAt = p.getDeletedAt();
        return r;
    }
}
