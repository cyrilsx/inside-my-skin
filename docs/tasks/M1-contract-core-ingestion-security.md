# M1 — OpenAPI contract, core domain, idempotent ingestion, security

Status: done

## Goal

Write the canonical API contract in `docs/openapi.yaml` before any implementation, and build the
backend foundation it describes: the canonical core-domain model, the idempotent ingestion pipeline
with raw JSONB retention, and JWT resource-server security with RFC 9457 errors. This task gates
every F-lane and companion upload task.

## Acceptance criteria

- [x] `docs/openapi.yaml` is valid OpenAPI 3.1 and covers the canonical endpoints from PLAN.md
      (`/dailies`, `/series/{metricType}`, `/activities`, `/mental/sessions`, `/mental/usage/daily`,
      `/mood/checkins`, `/events/stream`, `/export`, `DELETE /data`) plus companion batch ingest,
      with RFC 9457 Problem Details as the error shape and a bearer-JWT security scheme.
- [x] `docs/adr/` records the locked decisions this milestone rests on (ADR-0001..0006).
- [x] `backend:core-domain` holds the canonical model (records + enums) with canonical units only
      (meters, kilograms, seconds, plus dimensionless counts/scores/rates) and no vendor types.
- [x] `backend:ingestion` implements replay-safe ingestion under the `(connection_id,
      provider_record_id)` uniqueness rule, storing every raw payload before normalization.
- [x] Flyway V1 creates the schema; timestamps stored UTC with original zone offset.
- [x] Security: resource-server JWT from `JWT_SIGNING_KEY` env, `/api/**` authenticated,
      actuator health public, errors as RFC 9457 Problem Details.
- [x] Swagger UI at `/swagger-ui` serves `docs/openapi.yaml` (smoke-tested locally).
- [x] Frontend generates its API client from `docs/openapi.yaml`; `npm run test` + `npm run build` pass.
- [x] `./gradlew test` green in CI (Testcontainers integration tests included); unit + security slice tests also run locally on JDK 25.

## Dependencies

M0 (monorepo skeleton, green CI).

## Lane

Backend (primary), frontend (generated client). Android consumes the contract from M4.
