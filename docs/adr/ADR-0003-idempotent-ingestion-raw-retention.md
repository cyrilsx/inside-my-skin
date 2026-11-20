# ADR-0003 — Idempotent ingestion with raw retention

Status: accepted (locked)

## Context

Webhooks, scheduled pulls, and companion batch uploads can all deliver the same record more than
once; replays must not duplicate data, and mapping bugs must never lose the original payload.

## Decision

Every ingestion path is replay-safe under the `(connection_id, provider_record_id)` uniqueness
rule: raw vendor payloads are stored in JSONB first with a normalization status, then normalized
into canonical tables with upserts. Raw payloads are auditable, reprocessable, and purged after
24 months.

## Consequences

- Replaying an ingest is a no-op for already-seen `(connection_id, provider_record_id)`.
- Schema requires the unique constraints; tests must prove replay safety.
