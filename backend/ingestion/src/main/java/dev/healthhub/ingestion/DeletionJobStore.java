package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DeletionJobStore {

    DeletionJob save(DeletionJob job);

    void updateStatus(UUID id, String status, Instant completedAt);

    Optional<DeletionJob> find(UUID id);
}
