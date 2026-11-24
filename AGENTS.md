# HealthHub: AI Agent Instructions

This file is the standing instruction set for any AI coding agent that works in this repository. The task backlog and the research rationale live in the build plan; this file is how you work. Read it fully before touching any file. If an instruction here conflicts with a task description, this file wins; report the conflict instead of improvising.

## 1. What you are building

A personal health and wellbeing aggregation platform. One user, four data sources (Garmin, Withings, Health Connect via the Android companion app, manual entry), one canonical API owned by us, a Vue dashboard, and an Android companion app. Physical metrics and mental wellbeing (screen time, meditation, mood) are equally first-class.

## 2. Reading order before any task

1. This file.
2. The relevant ADR set: all architecture decisions in `docs/adr/` are locked. You never reopen one inside a task. If a task seems to require violating an ADR, you are blocked; report it.
3. `docs/openapi.yaml`: the single source of truth for request and response shapes. Never hardcode a payload shape in a client or test that contradicts it.
4. The task text in `docs/tasks/` for your assigned TASK-ID, including its acceptance criteria and dependencies.
5. Existing neighbors: read the module you are changing, plus its tests, before writing code.

## 3. Hard rules, no exceptions

- One task at a time. Declare your TASK-ID at the start of your run. Do not modify files owned by another in-progress task.
- Never invent vendor endpoints, field names, auth flows, or scopes. All vendor interaction in tests runs against recorded fixtures in `docs/fixtures/`. If a needed fixture is missing, create one marked as synthetic in your report. Real endpoints only ever appear behind configuration, filled by the human.
- Idempotency is a functional requirement, not a nicety. Any ingestion path must be replay-safe under the `(connection_id, provider_record_id)` uniqueness rule.
- Timestamps are stored UTC with the original zone offset at capture. A timestamp without zone information is a bug, not a style issue. Any new rollup or aggregation logic must include a DST-transition-day test.
- Canonical units only in core-domain and beyond: meters, kilograms, seconds. Conversions happen in adapters (inbound) and presentation only (outbound).
- No vendor type, field name, enum, or naming leaks past the adapter module that owns it. UI labels are ours; if you catch yourself naming a column or a type after a vendor, stop.
- Secrets live in environment variables. No token or key in code, tests, fixtures committed to the repo, or logs. The test scan for secret patterns must keep passing. Exception (owner-approved, M10): users may submit their own provider credentials through the authenticated `PUT /api/v1/providers/{provider}/credentials` endpoint; they are stored AES-GCM encrypted at rest under the env-held `TOKEN_ENCRYPTION_KEY` master key, are never returned by the API (status exposes key names only), and never logged. Master keys (JWT_SIGNING_KEY, TOKEN_ENCRYPTION_KEY, WEBHOOK_SECRET, callback secrets) and vendor base URLs remain env-only.
- Mental wellbeing data is descriptive, never prescriptive. No UI text, API field, or insight job may generate recommendations or causal health claims. Wording like "you should" or "this causes" fails review by definition.
- Garmin data must never be used to train AI models. Do not add any dataset export, embedding pipeline, or model fine-tuning path whose input includes Garmin-sourced records.

## 4. Workflow per task

Follow this sequence for every task, in order:

1. **Claim**: state the TASK-ID and restate the goal and acceptance criteria in two sentences. If you cannot state them, you have not read enough; go back to step 2 of the reading order.
2. **Test first** for core-domain, ingestion, insights, and adapter logic: failing test, then implementation in the same commit. Frontend and Android: component or instrumentation tests alongside the implementation.
3. **Implement** the smallest change that satisfies the acceptance criteria. No speculative abstractions, no feature you were not asked for, no refactors outside the task's file scope.
4. **Verify**: run the full verification set for your lane (section 5). A task is not done while any check is red. Never skip, disable, or annotate a test to make it pass.
5. **Report** (section 6), including any decision you made beyond the task text.

## 5. Verification commands by lane

Backend lane:

```
./mvnw test                   # unit + integration, Testcontainers needs Docker (backend is Maven; android/ stays on Gradle)
docker compose up -d postgres # prerequisite for Testcontainers
```

Frontend lane:

```
cd frontend && npm run test
cd frontend && npm run build   # type + build gate
```

Android lane:

```
cd android && ./gradlew assembleDebug testDebugUnitTest
./gradlew connectedDebugAndroidTest   # emulator or device, API 34+
```

Cross-cutting: `docker compose up` must still boot the whole stack, and `docker compose run backend` (Swagger UI at `/swagger-ui`) must reflect `docs/openapi.yaml` when the task touches the API.

## 6. Report format

End every completed task with this exact structure:

- TASK-ID and status (done, blocked).
- Files touched, grouped by module.
- Tests added, with one line on what each protects.
- Acceptance criteria, restated and checked off one by one.
- Decisions taken beyond the task text, with one sentence of justification each.
- Discovered risks or follow-ups, as concrete task suggestions (what, why, where).

When blocked, report immediately with: what you need, which file or fixture is missing, and what you already tried. A blocked report is a success state; an improvised workaround is not.

## 7. Code conventions

Backend (Java 25, Spring Boot 4.1):

- Records for DTOs, entities only where JPA requires. Constructor injection, no field injection.
- Jackson 3 (`tools.jackson`) for JSON. Do not add Jackson 2 dependencies.
- Errors: RFC 9457 Problem Details, always, including validations. No bare strings in error bodies.
- Use the framework's built-in resilience (`@Retryable`, `RetryTemplate`, `@ConcurrencyLimit`) for vendor calls; no external resilience library.
- Flyway for schema. Never modify a shipped migration; always add a new one.
- Ports (interfaces) live outside providers modules; adapters implement them.

Frontend (Vue 3, TypeScript):

- Script setup with Composition API only. Pinia stores per domain, no cross-importing component internals.
- API access only through the generated client from `docs/openapi.yaml`. No hand-rolled fetch calls in components.
- Charts through the shared ECharts config; one chart config per visualization family.

Android (Kotlin, Compose):

- Inject a clock everywhere time is read; never call `System.currentTimeMillis` directly.
- Health Connect reads check feature availability per record type before use.
- Background work only through WorkManager; the app uploads aggregated deltas, never raw continuous streams.

## 8. Orchestrator instructions

If you are the agent dispatching other agents rather than coding directly:

- Dispatch at most three concurrent runs: one backend, one frontend, one Android. Two agents in the same lane collide on shared build files.
- Enforce the dependency graph from the build plan. The OpenAPI contract task gates every F-lane and companion upload task. Webhooks and scheduled sync need the ingestion and security tasks green first.
- Review every completed task against its acceptance criteria before marking it done. Reject on: invented vendor data, red or skipped tests, vendor naming leaks, prescriptive mental-health wording, an unreported schema change.
- When a change touches `docs/openapi.yaml`, regenerate clients and run the full pipeline before releasing the task; a contract change is never done until all three lanes compile against it.
- Human gates stay human. Never send a vendor an application, fill a form, or generate credentials on the user's behalf. Prepare the exact request text instead.

## 9. Doing good work here

The quality bar for this repo is trust, because the data is the most personal data there is. The machine agreement on what is going on: precise timestamps, honest estimates for usage stats, silent sync failures surfaced loudly in `sync_audit` and on the dashboard, raw payloads kept so mistakes are reversible. When in doubt, prefer the choice that survives the user reading the code in a year.
