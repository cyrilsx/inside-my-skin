# M12 — README/docs review and fixes

Status: done

## Goal

Review `README.md` (and the duplicated getting-started in `PLAN.md` / `AGENTS.md`) against the
actual repo state and fix the mistakes: wrong docker compose invocation, broken Maven run
sequence, stale roadmap, overclaiming privacy/logging statements, outdated env-var section.

## Fixes

- [x] `docker compose up -d postgres` → `POSTGRES_PASSWORD=... docker compose -f infra/docker-compose.yml up -d postgres`
      (the compose file lives in `infra/`, and it requires `POSTGRES_PASSWORD`) — in README.md, PLAN.md, AGENTS.md §5.
- [x] Maven run sequence: `./mvnw -pl backend/app spring-boot:run` needs installed siblings →
      `./mvnw install -DskipTests` first (README.md + PLAN.md getting started).
- [x] AGENTS.md §7 referenced `docker compose run backend` — the compose file has no backend service;
      replaced with the postgres service + `./mvnw -pl backend/app spring-boot:run` for Swagger UI.
- [x] README roadmap table was stale (M0–M6, all "Not started") → synced to M0–M11 done, links PLAN.md.
- [x] Env-var section rewritten: master secrets env-only (JWT_SIGNING_KEY, TOKEN_ENCRYPTION_KEY,
      WEBHOOK_SECRET, POSTGRES_PASSWORD); provider credentials via Sources UI (encrypted at rest,
      ADR-0007) with env fallback (exact GARMIN_*/WITHINGS_* names from application.yml).
- [x] Removed overclaims: "purged after 24 months" (no retention job exists), "consent per source",
      "logs scanned for token leakage in CI" (replaced with the true statement: redacted by
      construction + tests + repo secret scan).
- [x] API highlights updated with the M5–M10 endpoints (providers/credentials, connections,
      companion ingest, webhooks, export query param); repo layout gained `architecture/` and
      `.github/workflows/`; tech stack mentions Maven and ArchUnit; broken sentence about the
      build plan fixed.

## Verification

- Docs-only change; no code touched. `infra/scripts/scan-secrets.sh` clean; CI green.

## Lane

Docs. One branch, one PR.
