# M0 — Monorepo skeleton, green CI

Status: done

## Goal

Create the repository layout from PLAN.md (backend modules, frontend, android, infra, docs)
with build scaffolding for each lane, and a CI pipeline that is green on every push.

## Acceptance criteria

- [x] Top-level layout matches PLAN.md: `backend/`, `frontend/`, `android/`, `infra/`, `docs/`.
- [x] Backend is a Gradle multi-module build (Java 25 toolchain, Spring Boot 4.1 in `backend:app`)
      with a context-loads test.
- [x] Frontend is a Vue 3 + TypeScript + Vite + Pinia + ECharts scaffold where
      `npm run test` and `npm run build` pass.
- [x] Android companion has a Gradle/Compose skeleton (compiles from M4 lane verification).
- [x] `infra/docker-compose.yml` provides PostgreSQL for local dev and Testcontainers.
- [x] CI runs on push/PR: secret-pattern scan, compose validation, backend tests, frontend test + build.
- [x] `.gitignore` matches the actual stack (the previous one was Angular-specific).
- [x] `AGENTS.md` carries the standing agent instructions.

## Dependencies

None. First task in the build plan.

## Lane

Cross-cutting. Backend and Android verification run in CI (sandbox has no JDK);
frontend verification runs locally and in CI.
