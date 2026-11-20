package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.UUID;

public record DeletionJob(
        UUID id,
        String status,
        Instant createdAt,
        Instant completedAt) {

    public DeletionJob {
        if (id == null || status == null || status.isBlank() || createdAt == null) {
            throw new IllegalArgumentException("id, status and createdAt are required");
        }
    }
}
