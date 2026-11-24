# ADR-0007 — Per-user provider credentials, stored encrypted at rest

Status: accepted (M10)

## Context

Until M10, provider credentials (OAuth client id/secret, Garmin personal key) existed only as
environment variables, so adding or rotating a provider required a redeploy. The product owner
asked for users to supply their own credentials per provider at runtime ("add a provider
dynamically and set up the connection"), stored securely.

## Decision

- New authenticated endpoints under `/api/v1/providers`:
  - `GET /providers` — known providers with `oauth` and `credentialsConfigured` flags.
  - `PUT /providers/{provider}/credentials` — body `{credentials: {<key>: <value>}}`, generic
    string map (max 16 entries, key pattern `[A-Za-z0-9_.-]{1,64}`, values 1–4096 chars).
  - `GET /providers/{provider}/credentials` — `{configured, keys}`; **never secret values**.
  - `DELETE /providers/{provider}/credentials`.
- Storage: new `provider_settings` table (Flyway V5), one row per provider holding a single
  AES-GCM encrypted JSON blob (existing `TokenCipher`, master key `TOKEN_ENCRYPTION_KEY` from
  env). Per-user scoping: the app is single-user, so `provider` is the key; with multi-user the
  key becomes `(user_id, provider)` — the port signature already isolates this.
- Resolution order in adapters: user-stored credentials first, environment configuration as
  fallback. The static `healthhub.*.enabled` gates on the OAuth/sync adapters were removed so a
  provider becomes usable the moment credentials exist; missing credentials fail with a clear
  422 message naming the endpoint or env var. Vendor base URLs, redirect URIs, scopes and the
  webhook callback secret stay env-only (deployment config, not user credentials).
- `DELETE /api/v1/data` (delete all data) removes connections, tokens and health data but keeps
  `provider_settings` (they are configuration, not data); `DELETE /providers/{p}/credentials`
  removes them explicitly.

## Consequences

- Positive: providers are addable/rotatable at runtime through the UI (Sources page), no redeploy;
  secrets at rest are AES-256-GCM under an env master key; the API never returns secret values;
  CI secret scan still passes (no literals anywhere).
- Negative/accepted: env configuration remains as fallback (two sources of truth, documented);
  `provider_settings` survives `DELETE /data` by design.
- AGENTS.md §3 was amended with this owner-approved exception to "secrets only in env".
