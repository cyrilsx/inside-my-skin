-- Per-user provider credentials (client id/secret, API keys, vendor account ids).
-- Values are stored as one AES-GCM encrypted JSON blob (see TokenCipher); the master key
-- (TOKEN_ENCRYPTION_KEY) stays in the environment. Never edit this migration once shipped.
CREATE TABLE provider_settings (
    provider        TEXT PRIMARY KEY,
    credentials_enc TEXT NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);
