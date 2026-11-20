package dev.healthhub.ingestion;

import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import java.time.Instant;
import java.time.ZoneOffset;

public record ProviderRecord(
        String providerRecordId,
        Source source,
        MetricType metricType,
        double value,
        Instant occurredAt,
        ZoneOffset zoneOffsetAtCapture,
        String rawPayload) {

    public ProviderRecord {
        if (providerRecordId == null || providerRecordId.isBlank() || source == null || metricType == null
                || occurredAt == null || zoneOffsetAtCapture == null || rawPayload == null) {
            throw new IllegalArgumentException("providerRecordId, source, metricType, occurredAt, zoneOffsetAtCapture and rawPayload are required");
        }
    }
}
