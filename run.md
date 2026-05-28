# IssueFlow – Setup & Run Guide

## Prerequisites
- Java 17+ (JDK)
- Docker & Docker Compose
- Maven (included via `mvnw` wrapper)

## 1. Start the Database

```bash
docker compose up -d
```

This starts a PostgreSQL 14 instance on port 5432 with:
- Database: `issueflow`
- Username: `issueflow`
- Password: `issueflow`

## 2. Build the Project

```bash
./mvnw clean package -DskipTests
```

On Windows:
```cmd
mvnw.cmd clean package -DskipTests
```

## 3. Run the Application

```bash
./mvnw spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/issueflow-0.0.1-SNAPSHOT.jar
```

The server starts on **http://localhost:8080**

## 4. Run Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database – no running PostgreSQL required.

## 5. First Login

A default admin user is seeded at startup:
- **Username:** `admin`
- **Password:** `admin123`

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Use the returned `token` in subsequent requests:
```bash
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer <token>"
```

## 6. API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | /auth/login | Login, returns JWT |
| POST | /auth/logout | Logout (invalidates token) |
| GET | /auth/me | Current user profile |
| POST | /users | Register user |
| GET | /users | List all users |
| GET | /users/{id} | Get user |
| POST | /users/update/{id} | Update user |
| DELETE | /users/{id} | Delete user |
| GET | /users/{id}/mentions | Comments mentioning user (paginated) |
| POST | /projects | Create project |
| GET | /projects | List projects |
| GET | /projects/{id} | Get project |
| PATCH | /projects/{id} | Update project |
| DELETE | /projects/{id} | Soft delete project |
| GET | /projects/deleted | List deleted projects (ADMIN) |
| POST | /projects/{id}/restore | Restore project (ADMIN) |
| GET | /projects/{id}/workload | Developer workload |
| POST | /tickets | Create ticket |
| GET | /tickets?projectId= | List tickets for project |
| GET | /tickets/{id} | Get ticket |
| PATCH | /tickets/{id} | Update ticket |
| DELETE | /tickets/{id} | Soft delete ticket |
| GET | /tickets/deleted?projectId= | Deleted tickets (ADMIN) |
| POST | /tickets/{id}/restore | Restore ticket (ADMIN) |
| POST | /tickets/{id}/dependencies | Add blocker dependency |
| GET | /tickets/{id}/dependencies | List blockers |
| DELETE | /tickets/{id}/dependencies/{blockerId} | Remove dependency |
| GET | /tickets/export?projectId= | Export tickets as CSV |
| POST | /tickets/import?projectId= | Import tickets from CSV |
| POST | /tickets/{id}/attachments | Upload file attachment |
| GET | /tickets/{id}/attachments | List attachments |
| DELETE | /tickets/{id}/attachments/{attachId} | Delete attachment |
| POST | /tickets/{id}/comments | Add comment |
| GET | /tickets/{id}/comments | List comments |
| PATCH | /tickets/{id}/comments/{commentId} | Update comment |
| DELETE | /tickets/{id}/comments/{commentId} | Delete comment |
| GET | /audit-logs | Audit logs (filterable) |
