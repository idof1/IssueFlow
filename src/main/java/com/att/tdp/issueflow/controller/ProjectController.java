package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.ProjectService;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest req) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.create(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> findAll() {
        return ResponseEntity.ok(projectService.findAll().stream()
                .map(ProjectResponse::from).collect(Collectors.toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @RequestBody ProjectUpdateRequest req) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponse>> findDeleted() {
        return ResponseEntity.ok(projectService.findDeleted().stream()
                .map(ProjectResponse::from).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.restore(id)));
    }

    @GetMapping("/{projectId}/workload")
    public ResponseEntity<List<WorkloadResponse>> getWorkload(@PathVariable Long projectId) {
        return ResponseEntity.ok(ticketService.getWorkload(projectId));
    }
}
