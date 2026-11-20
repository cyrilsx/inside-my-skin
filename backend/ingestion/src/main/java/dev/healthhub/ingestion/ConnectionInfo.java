package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.UUID;

public record ConnectionInfo(
        UUID id,
        String provider,
        String label,
        Instant connectedAt,
        Instant lastSyncAt,
        String lastSyncStatus) {
}
