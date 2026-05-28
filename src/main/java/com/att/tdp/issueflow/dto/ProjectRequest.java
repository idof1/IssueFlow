package com.att.tdp.issueflow.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long ownerId;
}
