# M6 — Insights, export, deletion, hexagonal boundary verification

Status: done

## Goal

Close out the contract: serve the remaining `docs/openapi.yaml` endpoints (`GET /events/stream` SSE,
`GET /export`, `DELETE /data` async deletion with a completion event), add the weekly training-volume
insight, and lock the hexagonal layout with architecture tests so no vendor code can leak past the
adapter modules again.

## Acceptance criteria

- [x] `GET /api/v1/events/stream` streams `text/event-stream` per the `Event` schema (JWT required);
      ACCEPTED ingests publish MEASUREMENT/ACTIVITY/MENTAL_SESSION/USAGE_DAILY/MOOD_CHECKIN events,
      syncs publish SYNC, deletion publishes DELETION; duplicates publish nothing.
- [x] `GET /api/v1/export?format=json|csv` returns all canonical data (measurements, activities,
      mental sessions, usage daily, mood check-ins, connection metadata) — canonical records only,
      no raw vendor payloads and no tokens; unknown format → 422 problem+json.
- [x] `DELETE /api/v1/data` returns 202 with a `DeletionJob` (PENDING), then asynchronously deletes
      canonical tables, raw events, sync audit and connections (encrypted tokens go with them),
      transitions the job to COMPLETED/FAILED (persisted via Flyway V4 `deletion_jobs`) and publishes
      a DELETION event.
- [x] Insights: `WeeklyRollupService` in `backend/insights` aggregates activity duration/distance per
      ISO week, bucketed by the captured zone offset, with DST-transition-day tests (2026-03-29
      spring forward, 2026-10-25 fall back).
- [x] Hexagonal boundary verification: new `backend:architecture` module (ArchUnit) asserts
      core-domain has no non-JDK dependencies, nothing outside `..providers..` references provider
      packages or vendor-named classes, and api/companion-api/insights never reference `..app..`.
- [x] Frontend: Sources view offers export (json/csv download) and delete-all-data (confirm → 202)
      via the generated client only; store spec covers both actions.
- [x] `gradle test` green in CI (incl. new architecture module and Testcontainers ITs), frontend
      `npm run test` + `npm run build` green, android lane untouched and green, secret scan green,
      compose still validates. Swagger UI reflects `docs/openapi.yaml` unchanged.
- [x] Smoke-tested end to end against real PostgreSQL: SSE receives ingest + DELETION events,
      export returns json and csv, delete empties all tables and completes the job.

## Dependencies

M1–M5 merged (contract, ingestion, providers, companion app, dashboard + read endpoints).

## Lane

Backend (ingestion, api, app, insights, new architecture module) + frontend (Sources view).
Android unchanged.

## Notes

- Export serves the authenticated user's own canonical data (data portability). It is not a dataset,
  embedding, or fine-tuning path, and it carries no vendor terms: raw vendor payloads and tokens are
  excluded (ADR-0006, PLAN.md privacy section).
- CSV format: one section per collection (header row, then rows, blank line between sections).
