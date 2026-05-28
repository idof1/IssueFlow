DROP TABLE IF EXISTS mentions;
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS ticket_dependencies;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS revoked_tokens;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS task;

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','DEVELOPER')),
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id    BIGINT       NOT NULL REFERENCES users(id),
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tickets (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('TODO','IN_PROGRESS','IN_REVIEW','DONE')),
    priority    VARCHAR(20)  NOT NULL CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    type        VARCHAR(20)  NOT NULL CHECK (type IN ('BUG','FEATURE','TECHNICAL')),
    project_id  BIGINT       NOT NULL REFERENCES projects(id),
    assignee_id BIGINT       REFERENCES users(id),
    due_date    TIMESTAMP,
    is_overdue  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    ticket_id  BIGINT    NOT NULL REFERENCES tickets(id),
    author_id  BIGINT    NOT NULL REFERENCES users(id),
    content    TEXT      NOT NULL,
    version    BIGINT    NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    action      VARCHAR(100) NOT NULL,
    actor       VARCHAR(100) NOT NULL,
    actor_type  VARCHAR(20),
    details     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ticket_dependencies (
    ticket_id     BIGINT NOT NULL REFERENCES tickets(id),
    blocked_by_id BIGINT NOT NULL REFERENCES tickets(id),
    PRIMARY KEY (ticket_id, blocked_by_id)
);

CREATE TABLE IF NOT EXISTS attachments (
    id           BIGSERIAL PRIMARY KEY,
    ticket_id    BIGINT       NOT NULL REFERENCES tickets(id),
    filename     VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_data    BYTEA        NOT NULL,
    size         BIGINT       NOT NULL,
    uploaded_by  BIGINT       NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS mentions (
    id         BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    UNIQUE (comment_id, user_id)
);

CREATE TABLE IF NOT EXISTS revoked_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      TEXT      NOT NULL UNIQUE,
    revoked_at TIMESTAMP NOT NULL DEFAULT NOW()
);
