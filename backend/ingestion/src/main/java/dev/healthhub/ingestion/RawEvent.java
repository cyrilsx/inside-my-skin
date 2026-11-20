package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.UUID;

public record RawEvent(
        Long id,
        UUID connectionId,
        String providerRecordId,
        String payload,
        Instant receivedAt,
        boolean normalized) {

    public RawEvent {
        if (connectionId == null || providerRecordId == null || providerRecordId.isBlank()
                || payload == null || receivedAt == null) {
            throw new IllegalArgumentException("connectionId, providerRecordId, payload and receivedAt are required");
        }
    }
}
