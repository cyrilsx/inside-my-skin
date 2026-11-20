package dev.healthhub.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MeasurementTest {

    private static final UUID CONNECTION = UUID.randomUUID();

    @Test
    void rejectsTimestampWithoutZoneOffset() {
        var occurredAt = Instant.parse("2026-03-29T01:30:00Z");
        assertThrows(IllegalArgumentException.class, () -> new Measurement(
                CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS, 1000,
                occurredAt, null, Instant.now()));
    }

    @Test
    void keepsOriginalZoneOffsetAtCapture() {
        var occurredAt = Instant.parse("2026-03-29T01:30:00Z");
        var measurement = new Measurement(
                CONNECTION, "rec-1", Source.WITHINGS, MetricType.WEIGHT_KG, 72.5,
                occurredAt, ZoneOffset.ofHours(2), Instant.parse("2026-03-29T02:00:00Z"));
        assertEquals(ZoneOffset.ofHours(2), measurement.zoneOffsetAtCapture());
        assertEquals(occurredAt, measurement.occurredAt());
        assertEquals(CanonicalUnit.KILOGRAM, measurement.unit());
    }
}
