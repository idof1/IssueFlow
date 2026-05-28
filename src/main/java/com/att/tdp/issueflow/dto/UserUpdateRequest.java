package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.entity.Role;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullName;
    private Role role;
}
