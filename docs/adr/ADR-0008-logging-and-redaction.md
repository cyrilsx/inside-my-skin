# ADR-0008 — Logging for problem tracking, with mandatory redaction of sensitive data

Status: accepted (M11)

## Context

We need logs to diagnose problems in production (sync failures, rejected ingests, auth issues),
but the system handles health data, OAuth tokens and user-supplied provider credentials. Logs
must therefore never contain secrets, tokens, payloads or credential values.

## Decision

- **Request logging**: `RequestLoggingFilter` (app module) logs one line per request — method,
  path (query string passed through `Redactor`), status, duration, and a per-request `requestId`
  in MDC (also in the logback pattern). Request/response **bodies are never logged** and the
  `Authorization` header is never logged; bodies and headers are where tokens and credential
  payloads live.
- **Redaction**: `Redactor.apply(String)` masks the value of any `key=value` / `"key":"value"` /
  `key: value` pair whose key matches secret-ish names (secret, token, password, api/personal key,
  authorization, credentials, oauth code/state, client/user id), plus `Bearer <token>`. It is
  applied to URIs, exception messages and free text before logging. Over-redaction is accepted.
- **Global handler**: `GlobalExceptionHandler` turns unexpected exceptions into RFC 9457 500
  problems and logs them with the redacted message; known `ErrorResponse`s keep their status and
  are logged at WARN with redacted detail.
- **Service logs** (INFO unless noted): sync start/completed/failed with counts
  (SyncOrchestrator), deletion job lifecycle (DeletionService), companion batch counts
  accepted/duplicates/rejected (CompanionIngestService — REJECTED items log clientId + exception
  class name only, never the payload or `e.getMessage()`, and the API response carries a generic
  "rejected" detail), credentials saved/deleted with **key names only** (ConnectionService),
  OAuth authorize/callback/connect/revoke/webhook events (provider + ids, never code/state/tokens).
- **Frontend**: `utils/logger.ts` wraps console with the same redaction; used by the global error
  handler. **Android**: `util/SafeLog.kt` redacts before `android.util.Log`; UploadWorker logs
  batch counts and exception class names only — never the queue payload or device token.
- **Logback**: `logback-spring.xml` — console pattern with `[req=%X{requestId:-}]`, `dev.healthhub`
  at INFO.

## Consequences

- Positive: problems are traceable end-to-end via requestId; secrets/tokens/health payloads cannot
  reach logs through the paths we control; tests (RedactorTest, RequestLoggingFilterTest with a
  logback ListAppender asserting the secret never appears, GlobalExceptionHandlerTest, SafeLogTest,
  logger.spec) keep it that way.
- Accepted limitation: a stack trace's exception message is redacted by pattern, not by
  construction — modules that build exception messages from payloads were changed to not do that
  (companion ingest) or to keep payloads out of messages by convention.
- AGENTS.md §3 gained a hard rule: no secrets/PII in logs; redact before logging.
