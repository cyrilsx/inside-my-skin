package dev.healthhub.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public record MentalSessionRecord(
        UUID id,
        Source source,
        MentalSessionKind kind,
        Instant startedAt,
        ZoneOffset zoneOffsetAtCapture,
        long durationS) {

    public MentalSessionRecord {
        if (id == null || source == null || kind == null || startedAt == null || zoneOffsetAtCapture == null) {
            throw new IllegalArgumentException("id, source, kind, startedAt and zoneOffsetAtCapture are required");
        }
        if (durationS < 0) {
            throw new IllegalArgumentException("durationS must be >= 0");
        }
    }
}
