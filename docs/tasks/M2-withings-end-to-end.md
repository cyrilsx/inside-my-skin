# M2 — Withings end to end (OAuth, webhooks, read API)

Status: done

## Goal

Connect a Withings account through OAuth 2.0 (authorize → callback → token exchange/refresh, tokens
encrypted at rest with AES-GCM), receive vendor webhooks and answer them with delta fetches, and
normalize the Withings measure API into the canonical idempotent ingestion pipeline — with every
sync recorded in `sync_audit`. All vendor interaction in tests runs against fixtures in
`docs/fixtures/withings/` (synthetic until real recordings exist); real vendor endpoints and scopes
live only behind env-backed configuration.

## Acceptance criteria

- [x] `docs/openapi.yaml` extended with connection management (`/connections`, authorize, callback,
      sync, revoke) and the vendor webhook path; frontend client regenerated, `npm run test` +
      `npm run build` green.
- [x] Withings adapter in `backend:providers:withings` behind a port defined outside providers;
      maps vendor measure types to canonical `MetricType`s with canonical units (kg, Pa, bpm);
      no vendor field names/enums leak past the adapter.
- [x] OAuth flow: authorization URL, code exchange, refresh; tokens stored AES-GCM encrypted
      (`TOKEN_ENCRYPTION_KEY` env); state validated on callback.
- [x] Webhook endpoint verifies our configured webhook secret, then triggers a delta sync;
      `/api/v1/webhooks/**` is permitAll at the security layer (secret checked in the controller),
      everything else under `/api/**` stays JWT-protected; errors are RFC 9457.
- [x] Idempotent ingestion: webhook replay / repeated delta fetch produces no duplicates
      (`(connection_id, provider_record_id)` uniqueness), proven by tests.
- [x] `sync_audit` records every sync (status, counts, error) — silent failures are loud.
- [x] Flyway V2 adds token columns + `sync_audit`; never modifies V1.
- [x] Tests: adapter against WireMock + `docs/fixtures/withings/*-synthetic.json`, webhook secret
      tests, encryption roundtrip, Testcontainers IT for storage/idempotency; `gradle test` green
      in CI; secret scan stays green.

## Dependencies

M1 (contract, core domain, idempotent ingestion, security) — merged.

## Lane

Backend (primary), contract touch regenerates the frontend client. Android consumes from M4.
