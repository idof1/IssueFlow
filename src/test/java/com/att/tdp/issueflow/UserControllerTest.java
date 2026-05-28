package com.att.tdp.issueflow;

import com.att.tdp.issueflow.dto.UserRequest;
import com.att.tdp.issueflow.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturn200() throws Exception {
        UserRequest req = new UserRequest();
        req.setUsername("testuser");
        req.setEmail("test@example.com");
        req.setFullName("Test User");
        req.setRole(Role.DEVELOPER);
        req.setPassword("password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    void createUser_duplicateUsername_shouldReturn409() throws Exception {
        UserRequest req = new UserRequest();
        req.setUsername("dupuser");
        req.setEmail("dup@example.com");
        req.setFullName("Dup User");
        req.setRole(Role.DEVELOPER);
        req.setPassword("password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        UserRequest req2 = new UserRequest();
        req2.setUsername("dupuser");
        req2.setEmail("dup2@example.com");
        req2.setFullName("Dup User 2");
        req2.setRole(Role.DEVELOPER);
        req2.setPassword("password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void getAllUsers_shouldReturn200() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void getUser_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_invalidEmail_shouldReturn400() throws Exception {
        UserRequest req = new UserRequest();
        req.setUsername("baduser");
        req.setEmail("not-an-email");
        req.setFullName("Bad User");
        req.setRole(Role.DEVELOPER);
        req.setPassword("password123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
