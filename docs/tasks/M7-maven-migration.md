# M7 — Migrate the backend build from Gradle to Maven

Status: done

## Goal

Replace the root Gradle build with a Maven multi-module build (root `pom.xml` aggregating the nine
`backend/*` modules, committed `./mvnw` wrapper, Spring Boot 4.1 BOM), and update every place that
pinned Gradle commands (CI backend job, `AGENTS.md` §5, `PLAN.md`/`README.md` getting started). The
Android companion stays on Gradle because AGP only supports Gradle.

## Acceptance criteria

- [x] Root `pom.xml` (packaging pom) aggregates `backend/core-domain`, `ingestion`, `api`,
      `companion-api`, `insights`, `architecture`, `providers/withings`, `providers/garmin`, `app`;
      one `pom.xml` per module translating the old `build.gradle` dependencies 1:1 (same artifacts,
      same scopes; `api` → compile, `implementation` → runtime/compile as appropriate).
- [x] Java 25 via `maven.compiler.release=25` + `-parameters`; surefire 3.5.3 with
      `useManifestOnlyJar=false` (ArchUnit reads `java.class.path`); Spring Boot BOM 4.1.0 and
      Testcontainers BOM 1.21.4 imported; JUnit version left to the Boot BOM (Spring Framework 7
      requires JUnit 6 — do not pin junit-bom 5.x ahead of it).
- [x] Root Gradle files removed (`settings.gradle`, `build.gradle`, `gradle.properties`,
      `gradle/wrapper/`); `.gitignore` updated (`target/`, keep the wrapper jar exception);
      `./mvnw` + `.mvn/wrapper` committed.
- [x] ArchUnit boundary tests still run and pass under Maven (5 rules; rewritten as plain
      `@Test` methods calling `.check(...)` because surefire does not load ArchUnit's JUnit engine
      alongside JUnit 6).
- [x] CI backend job runs `./mvnw test` (no setup-gradle); android job unchanged (Gradle).
- [x] `AGENTS.md` §5 backend lane and `PLAN.md`/`README.md` getting started use `./mvnw`.
- [x] `./mvnw test` green (unit + slices locally; Testcontainers ITs in CI with Docker),
      `./mvnw package` produces the repackaged Spring Boot jar, app boots via
      `./mvnw -pl backend/app spring-boot:run` and passes the smoke check (ingest → SSE event →
      export → delete → DELETION event) against local PostgreSQL; frontend `npm run test` +
      `npm run build` green; secret scan green.

## Dependencies

M0–M6 merged. No contract or code changes — build system only.

## Lane

Backend build + CI. Frontend and Android code untouched.

## Notes

- `docs/tasks/M0..M6-*.md` keep their historical `./gradlew` lane notes; they record what was run
  at the time. The living instructions (`AGENTS.md`, `PLAN.md`) point at Maven.
