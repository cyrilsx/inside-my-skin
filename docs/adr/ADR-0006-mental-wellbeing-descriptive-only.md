# ADR-0006 — Mental wellbeing is descriptive, never prescriptive; Garmin data never trains AI

Status: accepted (locked)

## Context

Mood, screen time, and meditation data are first-class, but this platform must not turn personal
data into advice, and Garmin's developer agreement prohibits training AI models on its data.

## Decision

No UI text, API field, or insight job generates recommendations or causal health claims; wording
like "you should" or "this causes" fails review. No dataset export, embedding pipeline, or model
fine-tuning path may take Garmin-sourced records as input. Usage stats are labeled as estimates.

## Consequences

- Review rejects prescriptive wording by definition.
- Export paths carry no vendor terms to downstream consumers.
