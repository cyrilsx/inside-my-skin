CREATE TABLE activities (
    id                      UUID PRIMARY KEY,
    connection_id           UUID NOT NULL REFERENCES connections(id),
    provider_record_id      TEXT NOT NULL,
    source                  TEXT NOT NULL,
    type                    TEXT NOT NULL,
    title                   TEXT,
    started_at              TIMESTAMPTZ NOT NULL,
    zone_offset_at_capture  TEXT NOT NULL,
    duration_s              BIGINT NOT NULL,
    distance_m              DOUBLE PRECISION,
    avg_heart_rate_bpm      DOUBLE PRECISION,
    UNIQUE (connection_id, provider_record_id)
);

CREATE TABLE mental_sessions (
    id                      UUID PRIMARY KEY,
    connection_id           UUID NOT NULL REFERENCES connections(id),
    provider_record_id      TEXT NOT NULL,
    source                  TEXT NOT NULL,
    kind                    TEXT NOT NULL,
    started_at              TIMESTAMPTZ NOT NULL,
    zone_offset_at_capture  TEXT NOT NULL,
    duration_s              BIGINT NOT NULL,
    UNIQUE (connection_id, provider_record_id)
);

CREATE TABLE usage_daily (
    id                      UUID PRIMARY KEY,
    connection_id           UUID NOT NULL REFERENCES connections(id),
    provider_record_id      TEXT NOT NULL,
    date                    DATE NOT NULL,
    total_screen_time_s     BIGINT NOT NULL,
    is_estimate             BOOLEAN NOT NULL,
    per_app                 JSONB NOT NULL,
    UNIQUE (connection_id, provider_record_id)
);

CREATE TABLE mood_checkins (
    id                      UUID PRIMARY KEY,
    connection_id           UUID NOT NULL REFERENCES connections(id),
    provider_record_id      TEXT NOT NULL,
    occurred_at             TIMESTAMPTZ NOT NULL,
    zone_offset_at_capture  TEXT NOT NULL,
    valence                 DOUBLE PRECISION NOT NULL,
    labels                  JSONB NOT NULL,
    note                    TEXT,
    UNIQUE (connection_id, provider_record_id)
);

CREATE INDEX idx_activities_started ON activities (started_at);
CREATE INDEX idx_mood_occurred ON mood_checkins (occurred_at);
