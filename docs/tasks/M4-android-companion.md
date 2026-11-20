# M4 — Android companion app: Health Connect, usage stats, mood, upload

Status: done

## Goal

Build the Android companion app that reads Health Connect (steps, sleep, heart rate, exercise,
mindfulness) with per-record-type feature availability checks, aggregates screen time via
UsageStatsManager (marked as estimates), captures manual mood check-ins, and uploads aggregated
deltas to `POST /api/v1/companion/ingest` through WorkManager with the generated API client —
never raw streams, replay-safe via client ids.

## Acceptance criteria

- [x] `android/` builds: `gradle -p android :app:assembleDebug :app:testDebugUnitTest` green
      (CI job added); the API client is generated from `docs/openapi.yaml` by
      openapi-generator, no hand-rolled fetch.
- [x] Clock injected everywhere time is read (no direct `System.currentTimeMillis`).
- [x] Health Connect reads check feature availability per record type before use.
- [x] Screen time aggregated per day/per app via UsageStatsManager, flagged `isEstimate`.
- [x] Mood check-ins (valence + labels + note) enqueue for upload; descriptive only, no advice.
- [x] Uploads only via WorkManager, batched as deltas, replay-safe by `clientId`; results applied.
- [x] Device token stored encrypted (EncryptedSharedPreferences); base URL via DataStore.
- [x] Unit tests for aggregation/mapping logic with a fake clock; instrumented tests need an
      emulator (follow-up).

## Dependencies

M1 (contract incl. `/companion/ingest`, security), M2/M3 (ingestion idempotency) — merged.

## Lane

Android. Contract already covers the upload endpoint; no openapi change expected.
