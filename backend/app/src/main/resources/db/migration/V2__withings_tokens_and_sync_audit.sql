ALTER TABLE connections
    ADD COLUMN encrypted_access_token TEXT,
    ADD COLUMN encrypted_refresh_token TEXT,
    ADD COLUMN token_expires_at TIMESTAMPTZ,
    ADD COLUMN scope TEXT,
    ADD COLUMN external_user_id TEXT,
    ADD COLUMN last_sync_at TIMESTAMPTZ,
    ADD COLUMN last_sync_status TEXT;

CREATE UNIQUE INDEX idx_connections_external_user ON connections (provider, external_user_id)
    WHERE external_user_id IS NOT NULL;

CREATE TABLE sync_audit (
    id            BIGSERIAL PRIMARY KEY,
    connection_id UUID NOT NULL REFERENCES connections(id),
    provider      TEXT NOT NULL,
    started_at    TIMESTAMPTZ NOT NULL,
    finished_at   TIMESTAMPTZ NOT NULL,
    status        TEXT NOT NULL,
    fetched       INTEGER NOT NULL,
    ingested      INTEGER NOT NULL,
    duplicates    INTEGER NOT NULL,
    error         TEXT
);

CREATE INDEX idx_sync_audit_connection ON sync_audit (connection_id, started_at DESC);
