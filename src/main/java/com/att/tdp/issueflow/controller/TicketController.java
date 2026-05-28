package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.AttachmentService;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketRequest req) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.create(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findByProject(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.findByProject(projectId).stream()
                .map(TicketResponse::from).collect(Collectors.toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody TicketUpdateRequest req) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> findDeleted(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.findDeletedByProject(projectId).stream()
                .map(TicketResponse::from).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> restore(@PathVariable Long id) {
        return ResponseEntity.ok(TicketResponse.from(ticketService.restore(id)));
    }

    @PostMapping("/{ticketId}/dependencies")
    public ResponseEntity<Void> addDependency(@PathVariable Long ticketId,
                                               @Valid @RequestBody DependencyRequest req) {
        ticketService.addDependency(ticketId, req.getBlockedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ticketId}/dependencies")
    public ResponseEntity<List<TicketResponse>> getDependencies(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getDependencies(ticketId).stream()
                .map(TicketResponse::from).collect(Collectors.toList()));
    }

    @DeleteMapping("/{ticketId}/dependencies/{blockerId}")
    public ResponseEntity<Void> removeDependency(@PathVariable Long ticketId, @PathVariable Long blockerId) {
        ticketService.removeDependency(ticketId, blockerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestParam Long projectId) throws IOException {
        byte[] csv = ticketService.exportCsv(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importCsv(@RequestParam Long projectId,
                                                   @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ticketService.importCsv(projectId, file));
    }

    @PostMapping("/{ticketId}/attachments")
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable Long ticketId,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam Long uploadedBy) throws IOException {
        return ResponseEntity.ok(AttachmentResponse.from(attachmentService.upload(ticketId, uploadedBy, file)));
    }

    @GetMapping("/{ticketId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> listAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(attachmentService.findByTicket(ticketId).stream()
                .map(AttachmentResponse::from).collect(Collectors.toList()));
    }

    @DeleteMapping("/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long ticketId,
                                                  @PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
        return ResponseEntity.ok().build();
    }
}
