package dev.healthhub.ingestion;

import java.time.Instant;

public record CanonicalEvent(
        String id,
        EventType type,
        Instant occurredAt,
        Object payload) {

    public CanonicalEvent {
        if (id == null || id.isBlank() || type == null || occurredAt == null || payload == null) {
            throw new IllegalArgumentException("id, type, occurredAt and payload are required");
        }
    }
}
