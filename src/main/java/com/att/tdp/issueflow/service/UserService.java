package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.UserRequest;
import com.att.tdp.issueflow.dto.UserUpdateRequest;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.exception.ConflictException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public User create(UserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username already taken: " + req.getUsername());
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already taken: " + req.getEmail());
        }
        // Password is optional per the README contract. If none is supplied,
        // generate a random one so the account exists but cannot be logged into.
        String rawPassword = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword()
                : UUID.randomUUID().toString();
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .fullName(req.getFullName())
                .role(req.getRole())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        user = userRepository.save(user);
        auditLogService.log("USER", user.getId(), "CREATE", user.getUsername(), "USER", null);
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User update(Long id, UserUpdateRequest req) {
        User user = findById(id);
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getRole() != null) user.setRole(req.getRole());
        user = userRepository.save(user);
        auditLogService.log("USER", user.getId(), "UPDATE", user.getUsername(), "USER", null);
        return user;
    }

    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        auditLogService.log("USER", id, "DELETE", user.getUsername(), "USER", null);
    }
}
