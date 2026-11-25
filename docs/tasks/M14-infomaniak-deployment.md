# M14 — Production deployment on Infomaniak Cloud

Status: done

## Goal

Make HealthHub deployable on Infomaniak Cloud: one production Docker image (backend + built SPA),
prod compose topology, prod profile, TLS-ready security headers, Jelastic (Public Cloud) and
Cloud Server (VM) guides, and CI building/pushing the image to GHCR. ADR-0009.

## Acceptance criteria

- [x] `infra/docker/Dockerfile` (multi-stage: maven:3.9-eclipse-temurin-25 → jar, node:22 →
      `frontend/dist`, eclipse-temurin:25-jre + curl healthcheck). CI `docker` job builds it on
      every PR and pushes `ghcr.io/cyrilsx/inside-my-skin:<sha>`/`latest` on main.
- [x] `infra/docker-compose.prod.yml` (app + postgres:17, healthchecks, restart policies,
      pgdata volume, all secrets from env with `:?` guards); CI compose job also validates it.
- [x] `application-prod.yml` (validate DDL, Flyway, actuator health-only, forward-headers
      framework, graceful shutdown).
- [x] `SpaConfig`: serves SPA from `spring.web.resources.static-locations` (image sets
      `file:/app/static/` first), index.html fallback for client routes, server paths never
      rewritten. `SpaConfigTest` (4 tests: fallback, api-not-rewritten, api-works, headers).
- [x] Security headers: CSP + Referrer-Policy added in `SecurityConfig`; asserted in tests.
- [x] `infra/infomaniak/manifest.jps` (Jelastic one-click: docker node + managed PostgreSQL 17,
      env-var secrets) + `docs/deployment/infomaniak.md` (both options, secrets checklist,
      ops: logs/backups/upgrades/rollback) + `.env.prod.example` (gitignored `.env.prod`).
- [x] Runtime smoke with prod profile against real PostgreSQL: `GET /` 200 SPA, `GET /trends`
      200 index.html fallback, JS asset 200, `GET /api/v1/dailies` 401 problem+json with CSP,
      `/actuator/health` UP, `/swagger-ui` 200.
- [x] Full `./mvnw test` green (excl. Testcontainers ITs, CI-only); frontend tests+build green;
      secret scan green; README points to the deployment doc; PLAN.md row.

## Dependencies

M0–M13 merged. No contract change.

## Lane

Infra + app (web config, prod profile, headers) + CI. Verification: mvn tests, frontend build,
prod-profile smoke run, CI docker build, compose config validation, secret scan.

## Notes

- Docker daemon unavailable in the sandbox → the image build itself is verified by the CI
  `docker` job; the runtime (jar + real frontend/dist + prod profile) was verified locally.
- Jelastic manifest is best-effort; the manual setup in docs/deployment/infomaniak.md is the
  reference path.
