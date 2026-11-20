# M5 — Vue dashboard: Today, Trends, Activities, Mental, Sources

Status: done

## Goal

Build the Vue 3 dashboard (Today, Trends, Activities, Mental, Sources) on the generated client
from `docs/openapi.yaml`, with Pinia stores per domain and the shared ECharts config — and the
backend read/ingest endpoints the dashboard needs, so the loop companion→ingest→dashboard works
end to end. Screen time is labeled as an estimate; mental wellbeing stays descriptive.

## Acceptance criteria

- [x] Five views + router/nav; Pinia stores per domain; all API access via the generated client.
- [x] Charts only through the shared ECharts config (`src/charts/echarts.ts`).
- [x] Backend: `POST /api/v1/companion/ingest` (replay-safe by clientId), read endpoints
      (`/dailies`, `/series/{metricType}`, `/activities`, `/mental/sessions`, `/mental/usage/daily`,
      `/mood/checkins`); Flyway V3 adds activities/mental_sessions/usage_daily/mood_checkins.
- [x] Daily rollup buckets by local day from the captured zone offset, with DST-transition tests.
- [x] `npm run test` + `npm run build` green; `gradle test` green in CI; secret scan green.
- [x] Smoke-tested end to end against real PostgreSQL: ingest → replay duplicate → dailies/mood/usage/series.

## Dependencies

M1–M4 merged (contract, ingestion, providers, companion app).

## Lane

Frontend + backend read/ingest. Android unchanged (already uploads to `/companion/ingest`).
