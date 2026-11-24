# M11 — Observability logs with mandatory redaction (no sensitive data in logs)

Status: done

## Goal

Add problem-tracking logs across backend, frontend and Android, with a hard guarantee that
secrets (tokens, credentials), health payloads and PII never appear in logs. See ADR-0008.

## Acceptance criteria

- [x] `Redactor` (app/logging): masks secret-ish `key=value` / JSON pairs and `Bearer <token>`;
      unit-tested incl. quote preservation and over-redaction.
- [x] `RequestLoggingFilter`: one log line per request (method, redacted path+query, status,
      duration, MDC requestId); bodies and Authorization header never logged; 5xx at ERROR;
      logback-spring.xml pattern includes requestId. Filter test asserts the secret never appears
      in captured log output.
- [x] `GlobalExceptionHandler`: unexpected → 500 RFC 9457 problem + redacted error log; known
      ErrorResponse keeps its status. Unit-tested (500 + redaction).
- [x] Service INFO logs without secrets: sync lifecycle + counts, deletion job lifecycle,
      companion batch counts (REJECTED = clientId + exception class only, response detail generic),
      credentials saved/deleted with key names only, oauth/connect/revoke/webhook events without
      code/state/tokens.
- [x] Frontend `utils/logger.ts` (same redaction) used by the global error handler + spec;
      Android `util/SafeLog.kt` + UploadWorker logs counts/class names only + SafeLogTest.
- [x] `./mvnw test` green; frontend `npm run test` (24) + `npm run build` green; secret scan green;
      AGENTS.md §3 hard rule added; ADR-0008; PLAN.md row.

## Dependencies

M0–M10 merged. No contract change.

## Lane

Backend + frontend + android (log calls only). Verification: `./mvnw test`, `cd frontend && npm run test && npm run build`, android unit tests in CI, secret scan.

## Notes

- Android Kotlin verified by CI (no local SDK in the sandbox); SafeLog.redact is a pure function
  covered by SafeLogTest.
