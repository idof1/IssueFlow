package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotNull
    private Role role;

    @NotBlank @Size(min = 6)
    private String password;
}
