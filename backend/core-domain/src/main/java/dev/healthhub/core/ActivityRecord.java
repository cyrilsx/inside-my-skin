package dev.healthhub.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public record ActivityRecord(
        UUID id,
        Source source,
        ActivityType type,
        String title,
        Instant startedAt,
        ZoneOffset zoneOffsetAtCapture,
        long durationS,
        Double distanceM,
        Double avgHeartRateBpm) {

    public ActivityRecord {
        if (id == null || source == null || type == null || startedAt == null || zoneOffsetAtCapture == null) {
            throw new IllegalArgumentException("id, source, type, startedAt and zoneOffsetAtCapture are required");
        }
        if (durationS < 0) {
            throw new IllegalArgumentException("durationS must be >= 0");
        }
    }
}
