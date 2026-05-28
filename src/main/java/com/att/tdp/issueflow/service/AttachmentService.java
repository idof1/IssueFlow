package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.Attachment;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "application/pdf", "text/plain");
    private static final long MAX_SIZE = 10 * 1024 * 1024L;

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;
    private final AuditLogService auditLogService;

    public Attachment upload(Long ticketId, Long uploadedBy, MultipartFile file) throws IOException {
        ticketService.findById(ticketId);

        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("File exceeds maximum allowed size of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType);
        }

        Attachment attachment = Attachment.builder()
                .ticketId(ticketId)
                .filename(file.getOriginalFilename())
                .contentType(contentType)
                .fileData(file.getBytes())
                .size(file.getSize())
                .uploadedBy(uploadedBy)
                .build();
        attachment = attachmentRepository.save(attachment);
        auditLogService.log("ATTACHMENT", attachment.getId(), "UPLOAD", "user:" + uploadedBy, "USER", null);
        return attachment;
    }

    public List<Attachment> findByTicket(Long ticketId) {
        ticketService.findById(ticketId);
        return attachmentRepository.findAllByTicketId(ticketId);
    }

    public Attachment findById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found: " + id));
    }

    public void delete(Long id) {
        Attachment attachment = findById(id);
        attachmentRepository.delete(attachment);
        auditLogService.log("ATTACHMENT", id, "DELETE", "system", "USER", null);
    }
}
