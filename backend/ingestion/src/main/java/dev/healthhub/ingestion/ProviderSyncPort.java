package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProviderSyncPort {

    String provider();

    List<ProviderRecord> fetch(UUID connectionId, Instant since);
}
