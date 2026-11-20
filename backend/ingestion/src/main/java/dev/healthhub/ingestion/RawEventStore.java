package dev.healthhub.ingestion;

import java.util.Optional;
import java.util.UUID;

public interface RawEventStore {

    Optional<RawEvent> findByNaturalKey(UUID connectionId, String providerRecordId);

    RawEvent saveIfAbsent(RawEvent event);
}
