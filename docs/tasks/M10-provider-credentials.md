# M10 — Per-user provider credentials (stored encrypted) + dynamic provider setup

Status: done

## Goal

Users can supply their own credentials for each provider through the authenticated API and set up
a provider connection dynamically — no env redeploy. Credentials are stored AES-GCM encrypted at
rest under the env-held master key (`TOKEN_ENCRYPTION_KEY`); the API never returns secret values.
Env configuration remains as fallback. See ADR-0007.

## Acceptance criteria

- [x] `ProviderSettingsPort` (ingestion) + `provider_settings` table (Flyway V5) + `ProviderSettingsJpa`
      (AES-GCM JSON blob via `TokenCipher`).
- [x] `GET /providers`, `GET/PUT/DELETE /providers/{provider}/credentials` in `ProvidersController`
      (api module), wired into `docs/openapi.yaml` (schemas `ProviderInfo`, `ProviderList`,
      `CredentialsUpdate`, `CredentialsStatus`); validation → RFC 9457 422; JWT required (401 otherwise).
- [x] `ConnectionService`: `listProviders`, `saveCredentials` (key/value validation), `credentialsStatus`
      (keys only), `deleteCredentials`; unknown provider → 422.
- [x] Adapters resolve user-stored credentials first, env second: Withings OAuth (`clientId`/`clientSecret`,
      incl. authorize URL and token exchange), Garmin (`personalKey`/`userId`). Static `enabled` gates
      removed from OAuth/sync adapters; missing creds → clear 422. Webhook handler stays env-gated
      (callback secret is deployment config).
- [x] Frontend: Sources page credentials panel (provider picker, per-provider fields, password inputs,
      save/delete, status keys), store methods + specs, i18n keys in en/fr/ar/zh/ru; generated client
      refreshed from the contract.
- [x] Tests: `ConnectionServiceTest` (save/status/delete/validation/listProviders), adapter tests
      (user creds override env: authorize URL client_id, garmin header key), `ProvidersControllerTest`
      (401/204/422, keys-only status). `./mvnw test` green; smoke vs real PostgreSQL: PUT 204, GET keys
      only (no leak), authorize uses user client_id, DB blob encrypted (no plaintext), Flyway V5 applied,
      DELETE 204. Frontend `npm run test` (22) + `npm run build` green; secret scan green.
- [x] Docs: ADR-0007, this file, PLAN.md row, AGENTS.md §3 amended with the owner-approved exception.

## Dependencies

M0–M9 merged. Contract extended (additive only) — frontend/Android clients regenerate from openapi.yaml.

## Lane

Backend + api + frontend. Verification: `./mvnw test`, boot smoke, `cd frontend && npm run test && npm run build`, secret scan, CI.

## Notes

- `DELETE /data` intentionally keeps `provider_settings` (configuration, not health data); use
  `DELETE /providers/{p}/credentials` to remove them.
- Pre-existing observation (not fixed here, out of scope): `GET /connections` returns a bare array while
  the contract's `ConnectionList` is `{items: [...]}` — worth a follow-up contract/impl alignment.
