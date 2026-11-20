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
    private final CanonicalEventPublisher eventPublisher;
    private final Clock clock;

    public IngestionService(RawEventStore rawEventStore, CanonicalWritePort writePort,
            CanonicalEventPublisher eventPublisher, Clock clock) {
        this.rawEventStore = rawEventStore;
        this.writePort = writePort;
        this.eventPublisher = eventPublisher;
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
        return ingest(connectionId, providerRecordId, EventType.MEASUREMENT, measurement, rawPayload,
                port -> port.upsertMeasurement(measurement));
    }

    public IngestResult ingestActivity(UUID connectionId, String providerRecordId, ActivityRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, EventType.ACTIVITY, record, rawPayload,
                port -> port.upsertActivity(connectionId, providerRecordId, record));
    }

    public IngestResult ingestMentalSession(UUID connectionId, String providerRecordId, MentalSessionRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, EventType.MENTAL_SESSION, record, rawPayload,
                port -> port.upsertMentalSession(connectionId, providerRecordId, record));
    }

    public IngestResult ingestUsageDaily(UUID connectionId, String providerRecordId, UsageDailyRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, EventType.USAGE_DAILY, record, rawPayload,
                port -> port.upsertUsageDaily(connectionId, providerRecordId, record));
    }

    public IngestResult ingestMoodCheckin(UUID connectionId, String providerRecordId, MoodCheckinRecord record, String rawPayload) {
        return ingest(connectionId, providerRecordId, EventType.MOOD_CHECKIN, record, rawPayload,
                port -> port.upsertMoodCheckin(connectionId, providerRecordId, record));
    }

    private IngestResult ingest(UUID connectionId, String providerRecordId, EventType type, Object payload,
            String rawPayload, Consumer<CanonicalWritePort> write) {
        var existing = rawEventStore.findByNaturalKey(connectionId, providerRecordId);
        var rawEvent = rawEventStore.saveIfAbsent(new RawEvent(
                null, connectionId, providerRecordId, rawPayload, Instant.now(clock), false));
        write.accept(writePort);
        var outcome = existing.isPresent() ? IngestOutcome.DUPLICATE : IngestOutcome.ACCEPTED;
        if (outcome == IngestOutcome.ACCEPTED) {
            eventPublisher.publish(new CanonicalEvent(
                    UUID.randomUUID().toString(), type, Instant.now(clock), payload));
        }
        return new IngestResult(outcome, rawEvent.id());
    }
}
