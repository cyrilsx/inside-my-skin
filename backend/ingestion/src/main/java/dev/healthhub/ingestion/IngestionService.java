package dev.healthhub.ingestion;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.Source;
import dev.healthhub.core.UsageDailyRecord;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Consumer;

public class IngestionService {

    private final RawEventStore rawEventStore;
    private final CanonicalWritePort writePort;
    private final Clock clock;

    public IngestionService(RawEventStore rawEventStore, CanonicalWritePort writePort, Clock clock) {
        this.rawEventStore = rawEventStore;
        this.writePort = writePort;
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
        var measurement = new Measurement(
                connectionId, providerRecordId, source, metricType, value,
                occurredAt, zoneOffsetAtCapture, Instant.now(clock));
        return ingest(connectionId, providerRecordId, rawPayload,
                port -> port.upsertMeasurement(measurement));
    }

    public IngestResult ingestActivity(UUID connectionId, String providerRecordId, ActivityRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, rawPayload,
                port -> port.upsertActivity(connectionId, providerRecordId, record));
    }

    public IngestResult ingestMentalSession(UUID connectionId, String providerRecordId, MentalSessionRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, rawPayload,
                port -> port.upsertMentalSession(connectionId, providerRecordId, record));
    }

    public IngestResult ingestUsageDaily(UUID connectionId, String providerRecordId, UsageDailyRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, rawPayload,
                port -> port.upsertUsageDaily(connectionId, providerRecordId, record));
    }

    public IngestResult ingestMoodCheckin(UUID connectionId, String providerRecordId, MoodCheckinRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, rawPayload,
                port -> port.upsertMoodCheckin(connectionId, providerRecordId, record));
    }

    private IngestResult ingest(UUID connectionId, String providerRecordId, String rawPayload,
            Consumer<CanonicalWritePort> write) {
        var existing = rawEventStore.findByNaturalKey(connectionId, providerRecordId);
        var rawEvent = rawEventStore.saveIfAbsent(new RawEvent(
                null, connectionId, providerRecordId, rawPayload, Instant.now(clock), false));
        write.accept(writePort);
        return new IngestResult(
                existing.isPresent() ? IngestOutcome.DUPLICATE : IngestOutcome.ACCEPTED, rawEvent.id());
    }
}
