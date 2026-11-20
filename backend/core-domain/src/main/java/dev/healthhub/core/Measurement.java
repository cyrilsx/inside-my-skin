package dev.healthhub.core;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public record Measurement(
        UUID connectionId,
        String providerRecordId,
        Source source,
        MetricType metricType,
        double value,
        Instant occurredAt,
        ZoneOffset zoneOffsetAtCapture,
        Instant receivedAt) {

    public Measurement {
        if (connectionId == null) {
            throw new IllegalArgumentException("connectionId is required");
        }
        if (providerRecordId == null || providerRecordId.isBlank()) {
            throw new IllegalArgumentException("providerRecordId is required");
        }
        if (source == null || metricType == null) {
            throw new IllegalArgumentException("source and metricType are required");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
        if (zoneOffsetAtCapture == null) {
            throw new IllegalArgumentException("zoneOffsetAtCapture is required: a timestamp without zone information is a bug");
        }
        if (receivedAt == null) {
            throw new IllegalArgumentException("receivedAt is required");
        }
    }

    public CanonicalUnit unit() {
        return metricType.canonicalUnit();
    }
}
