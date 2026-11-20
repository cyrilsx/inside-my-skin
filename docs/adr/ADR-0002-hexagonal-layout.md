# ADR-0002 — Hexagonal layout

Status: accepted (locked)

## Context

Vendors change schemas, units, and auth flows; swapping one must not touch the rest of the system.

## Decision

Ports (interfaces) live outside the providers modules (`core-domain`, `ingestion`); adapters in
`providers/withings` and `providers/garmin` implement them. `core-domain` has no vendor
dependencies. Normalization to canonical units happens inbound in adapters, and only in
presentation outbound.

## Consequences

- No vendor type, field name, enum, or naming may leak past the adapter module that owns it.
- New providers are new adapter modules behind existing ports.
