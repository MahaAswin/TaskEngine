CREATE TABLE IF NOT EXISTS org_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('ADMIN', 'MEMBER')),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_org_invites_org_id ON org_invites (org_id);
CREATE INDEX IF NOT EXISTS idx_org_invites_token_hash ON org_invites (token_hash);
