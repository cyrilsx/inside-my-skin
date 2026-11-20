package dev.healthhub.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class LocalDay {

    private LocalDay() {
    }

    public static LocalDate of(Instant occurredAt, ZoneOffset zoneOffsetAtCapture) {
        if (occurredAt == null || zoneOffsetAtCapture == null) {
            throw new IllegalArgumentException("occurredAt and zoneOffsetAtCapture are required");
        }
        return occurredAt.atOffset(zoneOffsetAtCapture).toLocalDate();
    }
}
