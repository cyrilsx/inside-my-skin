package dev.healthhub.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public record MoodCheckinRecord(
        UUID id,
        Instant occurredAt,
        ZoneOffset zoneOffsetAtCapture,
        double valence,
        List<String> labels,
        String note) {

    public MoodCheckinRecord {
        if (id == null || occurredAt == null || zoneOffsetAtCapture == null) {
            throw new IllegalArgumentException("id, occurredAt and zoneOffsetAtCapture are required");
        }
        if (valence < -1.0 || valence > 1.0) {
            throw new IllegalArgumentException("valence must be in [-1, 1]");
        }
        labels = labels == null ? List.of() : List.copyOf(labels);
    }
}
