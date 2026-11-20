package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.UUID;

public record SyncResult(
        UUID connectionId,
        int fetched,
        int ingested,
        int duplicates,
        Instant finishedAt) {
}
