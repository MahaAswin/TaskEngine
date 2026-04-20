-- Team model and task scope visibility support

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'task_scope') THEN
        CREATE TYPE task_scope AS ENUM ('GLOBAL', 'TEAM', 'PRIVATE');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'team_member_role') THEN
        CREATE TYPE team_member_role AS ENUM ('TEAM_LEADER', 'MEMBER');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (org_id, name)
);

CREATE TABLE IF NOT EXISTS team_members (
    team_id UUID NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role team_member_role NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (team_id, user_id)
);

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS scope task_scope NOT NULL DEFAULT 'PRIVATE',
    ADD COLUMN IF NOT EXISTS team_id UUID REFERENCES teams (id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_tasks_scope ON tasks (org_id, scope);
CREATE INDEX IF NOT EXISTS idx_tasks_team ON tasks (team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user ON team_members (user_id);
