package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MentionInfo> mentionedUsers;

    @Data
    public static class MentionInfo {
        private Long id;
        private String username;
        private String fullName;
    }

    public static CommentResponse from(Comment c) {
        CommentResponse r = new CommentResponse();
        r.id = c.getId();
        r.ticketId = c.getTicketId();
        r.authorId = c.getAuthorId();
        r.content = c.getContent();
        r.version = c.getVersion();
        r.createdAt = c.getCreatedAt();
        r.updatedAt = c.getUpdatedAt();
        return r;
    }
}
