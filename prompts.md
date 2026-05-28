# IssueFlow — AI Interaction Log

## Models Used
- **Claude Code** — for building a work plan and then implementing the project step by step
- **Claude Sonnet 4.6** (claude-sonnet-4-6) via claude.ai — for understanding the code, reviewing the requirements, and asking questions throughout the process

---

## How I Used AI in This Project

I worked in three overlapping phases:

**Phase 1 — Planning (Claude Code)**
I started by asking Claude Code to build a full work plan for the project — breaking down the assignment into clear stages and deciding the implementation order.

**Phase 2 — Step-by-step implementation (Claude Code)**
After the plan was ready, I asked Claude Code to build each part step by step, following the plan. This included entities, repositories, services, controllers, JWT security, the scheduler, and tests.

**Phase 3 — Understanding and verification (claude.ai, in parallel)**
While Claude Code was building each part, I used claude.ai in parallel to ask questions, understand the code I was getting, and verify that everything matched the requirements document. I also used claude.ai at the end to write `run.md` and to generate this `prompts.md` file once the project was fully working.

---

## Claude Code Prompts (Phases 1 & 2 — Plan and Implementation)

### Prompt 1 — Build a work plan
> Read the IssueFlow requirements document. Build me a full work plan to implement this Spring Boot project — break it into stages, decide the order, and list every file and feature I need.

**What it produced:**
A structured plan covering all 5 core features (Users, Auth, Projects, Tickets, Comments) and all 8 extended features (Audit, Dependencies, Attachments, Export/Import, Soft Delete, Mentions, Auto-Escalation, Auto-Assignment) with the recommended implementation order.

### Prompt 2 — Step-by-step build
> Follow the plan and build each part step by step. Start with the foundation (User entity, repository, service, controller), then move to Projects, then Tickets with all the business rules, then Comments, then security, then the extended features.

**What it produced:**
The full project — every entity with the correct fields and enums from the requirements, JPA repositories, service classes with all the business rules (status transitions, can't update DONE, optimistic locking), controllers, JWT security stack, the escalation scheduler, and tests.

---

## claude.ai Prompts (Phase 3 — Understanding, Verification, and Documentation)

While Claude Code was building, I asked claude.ai questions in parallel to make sure I understood everything that was being generated.

### Prompt 3 — Project structure understanding
> Help me understand the structure of the project. I am using Java.

**What I used it for:**
Understanding the layered architecture, the database schema, and the relationships between entities.

### Prompt 4 — API syntax reference
> Help me with the structure and syntax of each API call.

**What I used it for:**
Getting a clear reference of every endpoint, request body, and response shape — to verify that what Claude Code built matches the requirements.

### Prompt 5 — Security and authentication
> What security and authentication do I have to implement?

**What I used it for:**
Understanding the JWT flow, what each security class does (JwtUtil, JwtFilter, SecurityConfig, TokenDenyList, CustomUserDetailsService), and how they fit together.

### Prompt 6 — Scheduler and database integration
> How do I integrate the scheduler with the DB?

**What I used it for:**
Understanding how `@Scheduled` runs the auto-escalation job, how dueDate and isOverdue are tracked, and how audit log entries with actor=SYSTEM are recorded.

### Prompt 7 — Tests
> How to run the tests? Can you build me tests?

**What I used it for:**
Learning the difference between unit tests, controller tests, and scheduler tests — and how to run them using `./mvnw test`.

---

## Prompt 8 — Basic Syntax and Skeleton Questions

While going through the code Claude Code generated, I asked many small questions to make sure I understood the syntax of every annotation, class, and layer.

### Questions about the project skeleton

> - What does the package structure look like in a Spring Boot project?
> - Why is there a `src/main/java` and a `src/test/java` folder?
> - What is `pom.xml` and what goes in it?
> - What does `application.properties` do?
> - What's the difference between `application.properties` and `application-test.properties`?
> - What is the `@SpringBootApplication` annotation on the main class?
> - What does `SpringApplication.run()` actually do?
> - Why do I run the project with `./mvnw spring-boot:run`?
> - What is the `mvnw` wrapper and why use it instead of installing Maven?

### Questions about entities

> - What is `@Entity`?
> - What does `@Table(name = "users")` do?
> - What is `@Id`?
> - What is `@GeneratedValue(strategy = GenerationType.IDENTITY)`?
> - What does `@Column(nullable = false)` mean?
> - Why use `@Column(name = "full_name")` instead of just naming the field full_name?
> - What is `@Enumerated(EnumType.STRING)` and why do we need it?
> - What is the difference between `@ManyToOne` and `@OneToMany`?
> - What does `@JoinColumn(name = "project_id")` do?
> - What is `@Version` and how does optimistic locking work?
> - What is `LocalDateTime` and how does Spring map it to the DB?

### Questions about repositories

> - What is `JpaRepository` and why do we extend it?
> - So after creating the User entity, we wrap it with an interface that inherits from one that already knows how to update the table?
> - What does the `<User, Long>` part mean in `JpaRepository<User, Long>`?
> - Why is it an interface and not a class?
> - How does Spring know how to implement the interface methods?
> - What is `Optional<User>` and why does `findById` return it instead of `User`?
> - How do method names like `findByUsername` work — does Spring really translate them to SQL?
> - What is `@Query` and when do I need to write my own query?

### Questions about services

> - What is `@Service`?
> - Why is it different from `@Component`?
> - What does `@RequiredArgsConstructor` do?
> - What is dependency injection and why does Spring inject things automatically?
> - Why is everything declared `private final`?
> - What is `@Transactional` and when do I need it?

### Questions about controllers

> - What is `@RestController`?
> - What's the difference between `@Controller` and `@RestController`?
> - What does `@RequestMapping("/users")` do?
> - What's the difference between `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@DeleteMapping`?
> - What is `@PathVariable`?
> - What is `@RequestBody`?
> - What is `@RequestParam`?
> - What is `ResponseEntity` and why use it instead of returning the object directly?
> - What does `ResponseEntity.status(201).body(...)` mean?
> - What is `@Valid` and how does it trigger validation?

### Questions about DTOs

> - Why don't we return the `User` object directly — why do we need a DTO?
> - What's the difference between a DTO, a request object, and an entity?
> - Why do we have a separate `CreateUserRequest` and `UpdateUserRequest`?
> - How do we map between an entity and a DTO?

### Questions about exception handling

> - What is `@RestControllerAdvice`?
> - What does `@ExceptionHandler` do?
> - How does the global exception handler turn an exception into a proper HTTP response?
> - Why is a custom `ResourceNotFoundException` better than throwing a generic exception?

### Questions about security

> - What is a filter in Spring Security?
> - What does `OncePerRequestFilter` mean?
> - What is `SecurityContextHolder` and why do we set the authenticated user into it?
> - What is `UserDetailsService` and why do we implement it?
> - What is `BCryptPasswordEncoder` and why don't we store passwords as plain text?
> - What does `SessionCreationPolicy.STATELESS` mean?
> - Why do we disable CSRF for a REST API?
> - What is a JWT and what's inside it?
> - Why do we need a deny list for logout if JWTs are stateless?

### Questions about the requirements document

> - Where in the requirements are the Ticket fields defined?
> - Does each class in sections 2.1–2.5 need all 4 layers? Is this the standard pattern?
> - Which features are mandatory vs extended (section 3)?
> - What does "soft delete" mean in section 3.5?

### Questions about Docker and database

> - What is Docker and why do I need it for this project?
> - What does `docker compose up -d` do?
> - How does Spring Boot connect to the PostgreSQL inside the container?
> - What does `spring.jpa.hibernate.ddl-auto=update` do?
> - What's the difference between `update`, `create`, and `create-drop`?

### Questions about tests

> - What is `@Mock` and what is `@InjectMocks`?
> - What's the difference between `@WebMvcTest` and `@SpringBootTest`?
> - What is `MockMvc`?
> - What is `@MockBean` and how is it different from `@Mock`?
> - Why do we use H2 for tests instead of PostgreSQL?
> - What is `assertThatThrownBy` from AssertJ?

**What I used these for:**
Building a deep understanding of every annotation, pattern, and design decision in the codebase. Going through each one until I could explain it in my own words — so I could fully own and defend the code Claude Code generated.

---

## Final claude.ai Prompts (Documentation)

### Prompt 9 — run.md
> Help me write the run.md documentation file for the project.

**What I used it for:**
Writing the `run.md` documentation file with full setup, build, run, and test instructions.

### Prompt 10 — prompts.md
> Can you write for me the prompts.md file I used?

**What I used it for:**
Generating this `prompts.md` file, once the project was fully working locally, to document the full AI interaction log.

---

## Notes on AI Usage

- The work was driven by a plan I had Claude Code build first, then implement step by step.
- I used claude.ai in parallel throughout the process to understand every piece of code Claude Code produced and to verify it against the requirements.
- Every annotation, business rule, and pattern in the project was reviewed and understood before submission.
- I am fully accountable for the code and can explain every part of it.
