# ADR-0009 — Production deployment on Infomaniak Cloud

Status: accepted (M14)

## Context

HealthHub must run somewhere trustworthy and Swiss-hosted. The app is a Spring Boot backend +
static Vue SPA + PostgreSQL. Infomaniak offers two relevant products: **Public Cloud (Jelastic
PaaS)** and **Cloud Servers (VMs)**.

## Decision

- **One container**: `infra/docker/Dockerfile` multi-stage build — Maven/JDK 25 builds the
  backend jar, Node 22 builds `frontend/dist`, Temurin 25 JRE runs it. The backend serves the
  SPA same-origin via `SpaConfig` (SPA fallback to `index.html` for client routes; `/api/**`,
  `/actuator/**`, `/swagger*`, `/v3/api-docs`, `/openapi.yaml`, `/webhooks/**`, `/connect/**`
  are never rewritten) using `spring.web.resources.static-locations` (`file:/app/static/` first
  in the image). One deployable = one origin = no CORS, HSTS straightforward.
- **Topology**: app container + `postgres:17` with healthchecks, named volume, restart
  policies — declared in `infra/docker-compose.prod.yml`.
- **Prod profile** (`application-prod.yml`): `ddl-auto: validate`, Flyway on boot,
  actuator limited to `/actuator/health` (no details), `forward-headers-strategy: framework`
  so Jelastic's TLS-terminating proxy works, graceful shutdown.
- **Security headers** (always on): CSP (`default-src 'self'`, inline styles allowed for
  ECharts, `frame-ancestors 'none'`), `Referrer-Policy: no-referrer`, plus Spring Security
  defaults (nosniff, DENY, HSTS over HTTPS).
- **Infomaniak Public Cloud (Jelastic)**: one-click manifest `infra/infomaniak/manifest.jps`
  (docker node from GHCR + managed PostgreSQL node); secrets as Jelastic env vars. CI builds
  and pushes `ghcr.io/cyrilsx/inside-my-skin:<sha>` (+`latest` on main) via a `docker` job.
- **Infomaniak Cloud Server (VM)**: git clone + `docker compose -f infra/docker-compose.prod.yml
  up -d --build` with `.env.prod` (gitignored, chmod 600; template `.env.prod.example`),
  Caddy/nginx in front for Let's Encrypt. Both paths documented in
  `docs/deployment/infomaniak.md`.
- **Secrets**: env-only everywhere (AGENTS.md §3); never in images or git; `scan-secrets.sh`
  in CI. Provider credentials via the dashboard (AES-GCM at rest), env fallback.

## Consequences

- Positive: single image deploy, Swiss hosting, TLS by the platform, healthchecked, secret
  hygiene enforced by CI; rollback = previous image tag.
- Accepted: the Jelastic manifest is a best-effort starting point (Jelastic versions evolve);
  the manual two-node setup in the docs is the verified path. Docker image build itself is
  verified by the CI `docker` job; local runtime behavior was smoke-tested with the prod
  profile (SPA + fallback + API 401 + health all green).
- Backups: Jelastic managed PG backups on PaaS; `pg_dump` cron on a VM (documented).
