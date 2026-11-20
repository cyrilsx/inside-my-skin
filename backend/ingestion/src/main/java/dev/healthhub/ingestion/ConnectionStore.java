package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionStore {

    ConnectionInfo saveNew(String provider, String label, Instant connectedAt);

    List<ConnectionInfo> list();

    Optional<ConnectionInfo> find(UUID id);

    Optional<ConnectionInfo> findByExternalUserId(String provider, String externalUserId);

    void markSynced(UUID id, Instant finishedAt, String status);

    void delete(UUID id);
}
