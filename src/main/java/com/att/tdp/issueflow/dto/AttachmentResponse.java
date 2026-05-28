package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Attachment;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String filename;
    private String contentType;
    private Long size;
    private Long uploadedBy;
    private LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment a) {
        AttachmentResponse r = new AttachmentResponse();
        r.id = a.getId();
        r.ticketId = a.getTicketId();
        r.filename = a.getFilename();
        r.contentType = a.getContentType();
        r.size = a.getSize();
        r.uploadedBy = a.getUploadedBy();
        r.createdAt = a.getCreatedAt();
        return r;
    }
}
