package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ProjectRequest;
import com.att.tdp.issueflow.dto.ProjectUpdateRequest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public Project create(ProjectRequest req) {
        if (!userRepository.existsById(req.getOwnerId())) {
            throw new NotFoundException("Owner user not found: " + req.getOwnerId());
        }
        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .ownerId(req.getOwnerId())
                .build();
        project = projectRepository.save(project);
        auditLogService.log("PROJECT", project.getId(), "CREATE", "user:" + req.getOwnerId(), "USER", null);
        return project;
    }

    public Project findById(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }

    public List<Project> findAll() {
        return projectRepository.findAllByDeletedAtIsNull();
    }

    public List<Project> findDeleted() {
        return projectRepository.findAllByDeletedAtIsNotNull();
    }

    public Project update(Long id, ProjectUpdateRequest req) {
        Project project = findById(id);
        if (req.getName() != null) project.setName(req.getName());
        if (req.getDescription() != null) project.setDescription(req.getDescription());
        project = projectRepository.save(project);
        auditLogService.log("PROJECT", project.getId(), "UPDATE", "system", "USER", null);
        return project;
    }

    public void delete(Long id) {
        Project project = findById(id);
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
        auditLogService.log("PROJECT", id, "SOFT_DELETE", "system", "USER", null);
    }

    public Project restore(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found: " + id));
        if (project.getDeletedAt() == null) {
            throw new BadRequestException("Project is not deleted");
        }
        project.setDeletedAt(null);
        project = projectRepository.save(project);
        auditLogService.log("PROJECT", id, "RESTORE", "system", "USER", null);
        return project;
    }
}
