ALTER TABLE teams
    ADD COLUMN IF NOT EXISTS invite_code VARCHAR(16);

UPDATE teams
SET invite_code = UPPER(SUBSTRING(REPLACE(id::TEXT, '-', '') FROM 1 FOR 8))
WHERE invite_code IS NULL;

ALTER TABLE teams
    ALTER COLUMN invite_code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_teams_invite_code ON teams (invite_code);

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    receiver_id UUID REFERENCES users (id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_org_created_at ON messages (org_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_direct ON messages (org_id, sender_id, receiver_id, created_at DESC);
