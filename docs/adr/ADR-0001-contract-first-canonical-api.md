# ADR-0001 — Contract-first canonical API

Status: accepted (locked)

## Context

Four data sources with different schemas must be served to a Vue dashboard and an Android
companion app. Vendor payloads are not a stable contract.

## Decision

`docs/openapi.yaml` is the single source of truth for request and response shapes. It is written
before implementation. All clients (Vue, Android) are generated from it; no client hand-rolls
payload shapes. Backend serves the contract and Swagger UI at `/swagger-ui` reflects the file.

## Consequences

- Contract changes require regenerating clients and running all lanes before release.
- Canonical naming only in the contract; vendor names never appear as types or fields past adapters.
