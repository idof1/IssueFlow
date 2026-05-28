package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.entity.*;
import com.att.tdp.issueflow.exception.*;
import com.att.tdp.issueflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketDependencyRepository dependencyRepository;
    private final AuditLogService auditLogService;

    private static final List<TicketStatus> STATUS_ORDER =
            List.of(TicketStatus.TODO, TicketStatus.IN_PROGRESS, TicketStatus.IN_REVIEW, TicketStatus.DONE);

    @Transactional
    public Ticket create(TicketRequest req) {
        projectRepository.findByIdAndDeletedAtIsNull(req.getProjectId())
                .orElseThrow(() -> new NotFoundException("Project not found: " + req.getProjectId()));

        Long assigneeId = req.getAssigneeId();
        if (assigneeId == null) {
            assigneeId = autoAssign(req.getProjectId());
        } else {
            validateAssignee(assigneeId);
        }

        Ticket ticket = Ticket.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus())
                .priority(req.getPriority())
                .type(req.getType())
                .projectId(req.getProjectId())
                .assigneeId(assigneeId)
                .dueDate(req.getDueDate())
                .build();
        ticket = ticketRepository.save(ticket);

        if (req.getAssigneeId() == null && assigneeId != null) {
            User assignee = userRepository.findById(assigneeId).orElse(null);
            String actorName = assignee != null ? assignee.getUsername() : String.valueOf(assigneeId);
            auditLogService.log("TICKET", ticket.getId(), "AUTO_ASSIGN", "SYSTEM", "SYSTEM",
                    "Auto-assigned to " + actorName);
        }
        auditLogService.log("TICKET", ticket.getId(), "CREATE", "system", "USER", null);
        return ticket;
    }

    public Ticket findById(Long id) {
        return ticketRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    public List<Ticket> findByProject(Long projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNull(projectId);
    }

    public List<Ticket> findDeletedByProject(Long projectId) {
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNotNull(projectId);
    }

    @Transactional
    public Ticket update(Long id, TicketUpdateRequest req) {
        Ticket ticket = findById(id);
        if (ticket.getStatus() == TicketStatus.DONE) {
            throw new BadRequestException("Cannot update a ticket that is DONE");
        }

        if (req.getStatus() != null) {
            validateStatusTransition(ticket.getStatus(), req.getStatus());
            ticket.setStatus(req.getStatus());
        }
        if (req.getTitle() != null) ticket.setTitle(req.getTitle());
        if (req.getDescription() != null) ticket.setDescription(req.getDescription());
        if (req.getAssigneeId() != null) {
            validateAssignee(req.getAssigneeId());
            ticket.setAssigneeId(req.getAssigneeId());
            ticket.setOverdue(false);
        }
        if (req.getPriority() != null) {
            ticket.setPriority(req.getPriority());
            ticket.setOverdue(false);
        }
        if (req.getDueDate() != null) ticket.setDueDate(req.getDueDate());

        if (req.getStatus() == TicketStatus.DONE) {
            checkNoDoneBlockers(id);
        }

        ticket = ticketRepository.save(ticket);
        auditLogService.log("TICKET", ticket.getId(), "UPDATE", "system", "USER", null);
        return ticket;
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findById(id);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        auditLogService.log("TICKET", id, "SOFT_DELETE", "system", "USER", null);
    }

    @Transactional
    public Ticket restore(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
        if (ticket.getDeletedAt() == null) {
            throw new BadRequestException("Ticket is not deleted");
        }
        ticket.setDeletedAt(null);
        ticket = ticketRepository.save(ticket);
        auditLogService.log("TICKET", id, "RESTORE", "system", "USER", null);
        return ticket;
    }

    public void addDependency(Long ticketId, Long blockedById) {
        Ticket ticket = findById(ticketId);
        Ticket blocker = findById(blockedById);
        if (!ticket.getProjectId().equals(blocker.getProjectId())) {
            throw new BadRequestException("Both tickets must belong to the same project");
        }
        if (ticketId.equals(blockedById)) {
            throw new BadRequestException("A ticket cannot block itself");
        }
        if (dependencyRepository.existsByTicketIdAndBlockedById(ticketId, blockedById)) {
            throw new ConflictException("Dependency already exists");
        }
        dependencyRepository.save(TicketDependency.builder()
                .ticketId(ticketId)
                .blockedById(blockedById)
                .build());
    }

    public List<Ticket> getDependencies(Long ticketId) {
        findById(ticketId);
        return dependencyRepository.findAllByTicketId(ticketId).stream()
                .map(d -> ticketRepository.findByIdAndDeletedAtIsNull(d.getBlockedById()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeDependency(Long ticketId, Long blockerId) {
        TicketDependencyId depId = new TicketDependencyId(ticketId, blockerId);
        if (!dependencyRepository.existsById(depId)) {
            throw new NotFoundException("Dependency not found");
        }
        dependencyRepository.deleteById(depId);
    }

    public List<WorkloadResponse> getWorkload(Long projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        List<Object[]> rows = ticketRepository.countOpenTicketsByAssignee(projectId, TicketStatus.DONE);
        Map<Long, Long> countByUser = new LinkedHashMap<>();
        for (Object[] row : rows) {
            countByUser.put((Long) row[0], (Long) row[1]);
        }

        List<User> developers = userRepository.findByRole(Role.DEVELOPER);
        return developers.stream()
                .map(u -> new WorkloadResponse(u.getId(), u.getUsername(),
                        countByUser.getOrDefault(u.getId(), 0L)))
                .sorted(Comparator.comparingLong(WorkloadResponse::getOpenTicketCount))
                .collect(Collectors.toList());
    }

    public byte[] exportCsv(Long projectId) throws IOException {
        List<Ticket> tickets = findByProject(projectId);
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(
                "id", "title", "description", "status", "priority", "type", "assigneeId"))) {
            for (Ticket t : tickets) {
                printer.printRecord(t.getId(), t.getTitle(), t.getDescription(),
                        t.getStatus(), t.getPriority(), t.getType(), t.getAssigneeId());
            }
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public ImportResult importCsv(Long projectId, MultipartFile file) throws IOException {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        ImportResult result = new ImportResult();
        result.setErrors(new ArrayList<>());
        int created = 0, failed = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            int row = 1;
            for (CSVRecord record : parser) {
                row++;
                try {
                    String title = record.get("title");
                    String description = getOptional(record, "description");
                    TicketStatus status = TicketStatus.valueOf(record.get("status").toUpperCase());
                    TicketPriority priority = TicketPriority.valueOf(record.get("priority").toUpperCase());
                    TicketType type = TicketType.valueOf(record.get("type").toUpperCase());
                    String assigneeStr = getOptional(record, "assigneeId");
                    Long assigneeId = assigneeStr != null && !assigneeStr.isBlank()
                            ? Long.parseLong(assigneeStr) : null;

                    Ticket ticket = Ticket.builder()
                            .title(title)
                            .description(description)
                            .status(status)
                            .priority(priority)
                            .type(type)
                            .projectId(projectId)
                            .assigneeId(assigneeId)
                            .build();
                    ticketRepository.save(ticket);
                    created++;
                } catch (Exception e) {
                    failed++;
                    result.getErrors().add("Row " + row + ": " + e.getMessage());
                }
            }
        }
        result.setCreated(created);
        result.setFailed(failed);
        return result;
    }

    private void validateStatusTransition(TicketStatus current, TicketStatus next) {
        int currentIdx = STATUS_ORDER.indexOf(current);
        int nextIdx = STATUS_ORDER.indexOf(next);
        if (nextIdx < currentIdx) {
            throw new BadRequestException("Status can only move forward: " + current + " -> " + next + " is not allowed");
        }
    }

    private void checkNoDoneBlockers(Long ticketId) {
        List<TicketDependency> deps = dependencyRepository.findAllByTicketId(ticketId);
        for (TicketDependency dep : deps) {
            ticketRepository.findByIdAndDeletedAtIsNull(dep.getBlockedById()).ifPresent(blocker -> {
                if (blocker.getStatus() != TicketStatus.DONE) {
                    throw new BadRequestException("Cannot mark as DONE: blocker ticket " + blocker.getId() + " is not DONE");
                }
            });
        }
    }

    private Long autoAssign(Long projectId) {
        List<User> developers = userRepository.findByRole(Role.DEVELOPER);
        if (developers.isEmpty()) return null;

        return developers.stream()
                .min(Comparator
                        .comparingLong((User u) ->
                                ticketRepository.countOpenTickets(projectId, u.getId(), TicketStatus.DONE))
                        .thenComparingLong(User::getId))
                .map(User::getId)
                .orElse(null);
    }

    private void validateAssignee(Long assigneeId) {
        if (!userRepository.existsById(assigneeId)) {
            throw new NotFoundException("Assignee user not found: " + assigneeId);
        }
    }

    private String getOptional(CSVRecord record, String column) {
        try {
            return record.get(column);
        } catch (Exception e) {
            return null;
        }
    }
}
