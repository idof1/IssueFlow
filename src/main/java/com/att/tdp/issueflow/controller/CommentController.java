package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable Long ticketId,
                                                   @Valid @RequestBody CommentRequest req) {
        return ResponseEntity.ok(commentService.create(ticketId, req));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findAll(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.findByTicket(ticketId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long ticketId,
                                                   @PathVariable Long commentId,
                                                   @Valid @RequestBody CommentUpdateRequest req) {
        return ResponseEntity.ok(commentService.update(commentId, req));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long ticketId, @PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.ok().build();
    }
}
