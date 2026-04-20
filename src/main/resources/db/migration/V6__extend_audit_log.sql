-- Extend task audit action domain and add team audit log stream

ALTER TABLE task_audit_logs
    DROP CONSTRAINT IF EXISTS task_audit_logs_action_check;

ALTER TABLE task_audit_logs
    ADD CONSTRAINT task_audit_logs_action_check
    CHECK (
        action IN (
            'CREATED',
            'UPDATED',
            'STATUS_CHANGED',
            'ASSIGNED',
            'DELETED',
            'SCOPE_CHANGED',
            'TEAM_CHANGED'
        )
    );

CREATE TABLE IF NOT EXISTS team_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES users (id),
    action VARCHAR(32) NOT NULL CHECK (
        action IN ('TEAM_CREATED', 'TEAM_MEMBER_ADDED', 'TEAM_MEMBER_REMOVED', 'TEAM_MEMBER_ROLE_CHANGED')
    ),
    affected_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_team_audit_logs_team_id ON team_audit_logs (team_id, timestamp DESC);
