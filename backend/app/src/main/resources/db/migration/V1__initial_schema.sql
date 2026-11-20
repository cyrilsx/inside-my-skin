CREATE TABLE connections (
    id          UUID PRIMARY KEY,
    provider    TEXT NOT NULL,
    label       TEXT,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE raw_events (
    id                  BIGSERIAL PRIMARY KEY,
    connection_id       UUID NOT NULL REFERENCES connections(id),
    provider_record_id  TEXT NOT NULL,
    payload             JSONB NOT NULL,
    received_at         TIMESTAMPTZ NOT NULL,
    normalized          BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (connection_id, provider_record_id)
);

CREATE INDEX idx_raw_events_connection ON raw_events (connection_id);

CREATE TABLE measurements (
    id                      UUID PRIMARY KEY,
    connection_id           UUID NOT NULL REFERENCES connections(id),
    provider_record_id      TEXT NOT NULL,
    source                  TEXT NOT NULL,
    metric_type             TEXT NOT NULL,
    value                   DOUBLE PRECISION NOT NULL,
    occurred_at             TIMESTAMPTZ NOT NULL,
    zone_offset_at_capture  TEXT NOT NULL,
    received_at             TIMESTAMPTZ NOT NULL,
    UNIQUE (connection_id, provider_record_id, metric_type)
);

CREATE INDEX idx_measurements_metric_time ON measurements (metric_type, occurred_at);
