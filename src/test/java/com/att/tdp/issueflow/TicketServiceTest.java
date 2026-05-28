package com.att.tdp.issueflow;

import com.att.tdp.issueflow.dto.TicketRequest;
import com.att.tdp.issueflow.dto.TicketUpdateRequest;
import com.att.tdp.issueflow.entity.*;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.repository.*;
import com.att.tdp.issueflow.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Long projectId;
    private Long userId;

    @BeforeEach
    void setup() {
        User user = userRepository.save(User.builder()
                .username("dev1")
                .email("dev1@test.com")
                .fullName("Dev One")
                .role(Role.DEVELOPER)
                .passwordHash("hash")
                .build());
        userId = user.getId();

        Project project = projectRepository.save(Project.builder()
                .name("Test Project")
                .ownerId(userId)
                .build());
        projectId = project.getId();
    }

    @Test
    void createTicket_shouldAssignToUser() {
        TicketRequest req = new TicketRequest();
        req.setTitle("Test Ticket");
        req.setStatus(TicketStatus.TODO);
        req.setPriority(TicketPriority.MEDIUM);
        req.setType(TicketType.BUG);
        req.setProjectId(projectId);
        req.setAssigneeId(userId);

        Ticket ticket = ticketService.create(req);
        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getAssigneeId()).isEqualTo(userId);
    }

    @Test
    void updateTicket_statusBackward_shouldThrow() {
        TicketRequest req = new TicketRequest();
        req.setTitle("Backward Test");
        req.setStatus(TicketStatus.IN_PROGRESS);
        req.setPriority(TicketPriority.LOW);
        req.setType(TicketType.FEATURE);
        req.setProjectId(projectId);
        req.setAssigneeId(userId);

        Ticket ticket = ticketService.create(req);

        TicketUpdateRequest update = new TicketUpdateRequest();
        update.setStatus(TicketStatus.TODO);

        assertThatThrownBy(() -> ticketService.update(ticket.getId(), update))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("forward");
    }

    @Test
    void updateDoneTicket_shouldThrow() {
        TicketRequest req = new TicketRequest();
        req.setTitle("Done Ticket");
        req.setStatus(TicketStatus.DONE);
        req.setPriority(TicketPriority.HIGH);
        req.setType(TicketType.TECHNICAL);
        req.setProjectId(projectId);
        req.setAssigneeId(userId);

        Ticket ticket = ticketService.create(req);

        TicketUpdateRequest update = new TicketUpdateRequest();
        update.setTitle("Updated Title");

        assertThatThrownBy(() -> ticketService.update(ticket.getId(), update))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DONE");
    }

    @Test
    void autoAssign_shouldPickLeastLoadedDeveloper() {
        TicketRequest req = new TicketRequest();
        req.setTitle("Auto Assign Test");
        req.setStatus(TicketStatus.TODO);
        req.setPriority(TicketPriority.LOW);
        req.setType(TicketType.BUG);
        req.setProjectId(projectId);

        Ticket ticket = ticketService.create(req);
        assertThat(ticket.getAssigneeId()).isEqualTo(userId);
    }
}
