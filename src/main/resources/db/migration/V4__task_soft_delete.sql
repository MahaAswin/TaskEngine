-- Soft delete for tasks (audit history + undo)
ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_tasks_org_deleted ON tasks (org_id, deleted);
