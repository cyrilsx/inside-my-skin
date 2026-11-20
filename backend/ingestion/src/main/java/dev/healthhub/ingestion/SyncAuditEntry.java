package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.UUID;

public record SyncAuditEntry(
        UUID connectionId,
        String provider,
        Instant startedAt,
        Instant finishedAt,
        String status,
        int fetched,
        int ingested,
        int duplicates,
        String error) {
}
