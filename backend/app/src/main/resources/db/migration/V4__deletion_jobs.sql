CREATE TABLE deletion_jobs (
    id            UUID PRIMARY KEY,
    status        TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    completed_at  TIMESTAMPTZ
);
