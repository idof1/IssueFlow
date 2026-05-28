package com.att.tdp.issueflow.config;

import com.att.tdp.issueflow.entity.Role;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedAdmin() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@issueflow.com")
                        .fullName("System Admin")
                        .role(Role.ADMIN)
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .build();
                userRepository.save(admin);
                log.info("Default admin user created (username: admin, password: admin123)");
            }
        };
    }
}
