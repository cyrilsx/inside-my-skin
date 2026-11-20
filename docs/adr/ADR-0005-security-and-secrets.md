# ADR-0005 — Security and secrets

Status: accepted (locked)

## Context

The platform stores the most sensitive category of personal data and holds vendor tokens.

## Decision

Spring Security resource-server JWT; `/api/**` requires a valid token, actuator health stays
public. All errors are RFC 9457 Problem Details, including validation errors. Secrets live only
in environment variables (`JWT_SIGNING_KEY`, `TOKEN_ENCRYPTION_KEY`, vendor credentials); provider
tokens are encrypted at rest with AES-GCM. No token or key appears in code, tests, fixtures, or
logs, and CI scans tracked files for secret patterns.

## Consequences

- Tests use mocked JWT decoders, never real keys.
- The secret-pattern scan is a required CI gate.
