# M3 — Garmin Personal API adapter and sync orchestration

Status: done

## Goal

Add the Garmin adapter behind the existing sync port — pulling daily steps, resting heart rate,
sleep duration, and HRV from the Garmin Connect Personal API using the self-serve key — plus
scheduled sync orchestration for all connections, and a key-based "connect" endpoint for
providers without OAuth (Garmin). All vendor interaction in tests runs against synthetic-marked
fixtures in `docs/fixtures/garmin/`; real endpoints, the key header, and the user id live only
behind env-backed configuration filled by the human. Garmin data is never used for AI training
(ADR-0006).

## Acceptance criteria

- [x] `backend:providers:garmin` implements `ProviderSyncPort` (provider `garmin`), mapping vendor
      payloads to canonical `MetricType`s (STEPS count, RESTING_HEART_RATE_BPM, SLEEP_SECONDS,
      HRV_RMSSD_S with ms→s conversion in the adapter); vendor fields stay inside the adapter.
- [x] Contract: `POST /api/v1/connections/{provider}/connect` for key-based providers; frontend
      client regenerated, `npm run test` + `npm run build` green.
- [x] Scheduled sync (`@Scheduled`, interval via `healthhub.sync.interval-ms`) syncs every
      connection and records each run in `sync_audit`; manual `POST /connections/{id}/sync` works.
- [x] Replay-safe: repeated syncs produce duplicates, not new rows (covered by tests).
- [x] Tests: adapter vs WireMock + `docs/fixtures/garmin/*-synthetic.json`; unit + Testcontainers
      ITs; `gradle test` green in CI; secret scan green.
- [x] PLAN.md roadmap updated.

## Dependencies

M1 (ingestion + security), M2 (ports, orchestrator, connection management) — merged.

## Lane

Backend (primary), contract touch regenerates the frontend client.
