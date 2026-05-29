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

    // Optional: README's create-user contract omits password. When provided it
    // enables login; when absent the user is created without a usable password.
    @Size(min = 6)
    private String password;
}
