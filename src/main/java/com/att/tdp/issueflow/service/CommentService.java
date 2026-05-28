package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.entity.Comment;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final MentionService mentionService;
    private final AuditLogService auditLogService;

    @Transactional
    public CommentResponse create(Long ticketId, CommentRequest req) {
        ticketService.findById(ticketId);
        if (!userRepository.existsById(req.getAuthorId())) {
            throw new NotFoundException("Author not found: " + req.getAuthorId());
        }

        Comment comment = Comment.builder()
                .ticketId(ticketId)
                .authorId(req.getAuthorId())
                .content(req.getContent())
                .build();
        comment = commentRepository.save(comment);

        List<User> mentioned = mentionService.syncMentions(comment.getId(), req.getContent());
        auditLogService.log("COMMENT", comment.getId(), "CREATE", "user:" + req.getAuthorId(), "USER", null);

        return buildResponse(comment, mentioned);
    }

    public List<CommentResponse> findByTicket(Long ticketId) {
        ticketService.findById(ticketId);
        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(c -> buildResponse(c, mentionService.getMentionedUsers(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse update(Long commentId, CommentUpdateRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
        comment.setContent(req.getContent());
        comment = commentRepository.save(comment);

        List<User> mentioned = mentionService.syncMentions(comment.getId(), req.getContent());
        auditLogService.log("COMMENT", comment.getId(), "UPDATE", "system", "USER", null);

        return buildResponse(comment, mentioned);
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
        commentRepository.delete(comment);
        auditLogService.log("COMMENT", commentId, "DELETE", "system", "USER", null);
    }

    public List<CommentResponse> getMentionsForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        return mentionService.getCommentIdsByUserId(userId).stream()
                .map(cid -> commentRepository.findById(cid).orElse(null))
                .filter(c -> c != null)
                .map(c -> buildResponse(c, mentionService.getMentionedUsers(c.getId())))
                .collect(Collectors.toList());
    }

    private CommentResponse buildResponse(Comment comment, List<User> mentioned) {
        CommentResponse r = CommentResponse.from(comment);
        r.setMentionedUsers(mentioned.stream().map(u -> {
            CommentResponse.MentionInfo mi = new CommentResponse.MentionInfo();
            mi.setId(u.getId());
            mi.setUsername(u.getUsername());
            mi.setFullName(u.getFullName());
            return mi;
        }).collect(Collectors.toList()));
        return r;
    }
}
