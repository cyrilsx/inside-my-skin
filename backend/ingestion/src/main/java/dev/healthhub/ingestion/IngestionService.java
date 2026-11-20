package dev.healthhub.ingestion;

import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public class IngestionService {

    private final RawEventStore rawEventStore;
    private final MeasurementStore measurementStore;
    private final Clock clock;

    public IngestionService(RawEventStore rawEventStore, MeasurementStore measurementStore, Clock clock) {
        this.rawEventStore = rawEventStore;
        this.measurementStore = measurementStore;
        this.clock = clock;
    }

    public IngestResult ingestMeasurement(
            UUID connectionId,
            String providerRecordId,
            Source source,
            MetricType metricType,
            double value,
            Instant occurredAt,
            ZoneOffset zoneOffsetAtCapture,
            String rawPayload) {
        var existing = rawEventStore.findByNaturalKey(connectionId, providerRecordId);
        var rawEvent = rawEventStore.saveIfAbsent(new RawEvent(
                null, connectionId, providerRecordId, rawPayload, Instant.now(clock), false));
        var measurement = new Measurement(
                connectionId, providerRecordId, source, metricType, value,
                occurredAt, zoneOffsetAtCapture, Instant.now(clock));
        measurementStore.upsert(measurement);
        return new IngestResult(
                existing.isPresent() ? IngestOutcome.DUPLICATE : IngestOutcome.ACCEPTED, rawEvent.id());
    }
}
