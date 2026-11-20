# ADR-0004 — Timestamps and canonical units

Status: accepted (locked)

## Context

Health data crosses time zones and DST transitions; vendors report mixed units.

## Decision

Timestamps are stored as UTC instants together with the original zone offset at capture. A
timestamp without zone information is a bug. Canonical units in core-domain and beyond are
meters, kilograms, seconds (plus dimensionless counts, scores, and rates such as beats/minute);
conversions happen in adapters (inbound) and presentation only (outbound). Any rollup or
aggregation logic must include a DST-transition-day test.

## Consequences

- Local-day bucketing derives the local date from the stored offset, which is DST-safe by
  construction.
- Adapters own all unit conversion; core-domain never sees vendor units.
