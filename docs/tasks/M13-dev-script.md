# M13 — One-command local dev: `scripts/dev.sh`

Status: done

## Goal

A single script that launches the whole local stack — PostgreSQL, the Maven backend, and the
Vite frontend — with sane local secrets, clean shutdown on Ctrl+C, and log files.

## Acceptance criteria

- [x] `scripts/dev.sh` (executable, `bash -n` clean):
      `./scripts/dev.sh` starts postgres (`infra/docker-compose.yml`), waits for `pg_isready`,
      builds the backend if needed (`./mvnw install -DskipTests`), runs it on :8080, then starts
      the Vite dev server on :5173; Ctrl+C stops everything; `--down` also stops the container,
      `--no-db` uses an externally provided `DATABASE_URL`, `--build` forces a rebuild.
      Logs go to `logs/backend.log` / `logs/frontend.log` (gitignored).
- [x] Local secrets generated once into gitignored `.dev-env` (`POSTGRES_PASSWORD`,
      `JWT_SIGNING_KEY`, `TOKEN_ENCRYPTION_KEY`, `WEBHOOK_SECRET`) and reused across restarts so
      encrypted tokens stay decryptable; env vars override; script honors `JAVA_HOME` when `java`
      is not on PATH; requires JDK 25 / Node / Docker with clear errors.
- [x] Vite dev proxy: `server.proxy['/api'] → http://localhost:8080` (`HEALTHHUB_API_URL` override)
      so the SPA is same-origin in dev; production build unaffected.
- [x] `.gitignore`: `.dev-env`, `logs/`. README getting-started mentions `./scripts/dev.sh`.
- [x] Verified by smoke run (no Docker in sandbox → `--no-db` against a local PostgreSQL):
      dashboard 5173 200 serving the app, `GET 5173/api/v1/providers` proxied → 401
      application/problem+json (proxy + security intact), backend `/actuator/health` UP,
      swagger-ui 200; clean shutdown via trap.
- [x] Frontend `npm run test` (24) + `npm run build` green after the vite.config change; secret
      scan clean (`.dev-env` never committed); CI green.

## Dependencies

M0–M12 merged. Tooling only; no contract change.

## Lane

Dev tooling + frontend config. Verification: smoke run above, `npm run test && npm run build`, secret scan, CI.
