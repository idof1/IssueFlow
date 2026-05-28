package com.att.tdp.issueflow.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull
    private Long authorId;

    @NotBlank
    private String content;
}
