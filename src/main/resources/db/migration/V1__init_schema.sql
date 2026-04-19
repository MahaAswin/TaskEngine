-- TaskEngine multi-tenant schema (PostgreSQL)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    plan VARCHAR(16) NOT NULL DEFAULT 'FREE' CHECK (plan IN ('FREE', 'PRO'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('ADMIN', 'MEMBER')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    avatar_url VARCHAR(2048),
    google_sub VARCHAR(255) UNIQUE
);

CREATE INDEX idx_users_org_id ON users (org_id);

CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users (id),
    assigned_to UUID REFERENCES users (id),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    due_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_org_status ON tasks (org_id, status);
CREATE INDEX idx_tasks_org_assigned_to ON tasks (org_id, assigned_to);

CREATE TABLE task_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES users (id),
    action VARCHAR(32) NOT NULL CHECK (action IN ('CREATED', 'UPDATED', 'STATUS_CHANGED', 'ASSIGNED', 'DELETED')),
    old_value JSONB,
    new_value JSONB,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_task_audit_logs_task_id ON task_audit_logs (task_id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
