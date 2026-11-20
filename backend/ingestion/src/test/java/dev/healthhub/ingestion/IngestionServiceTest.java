package dev.healthhub.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IngestionServiceTest {

    private InMemoryRawEventStore rawEventStore;
    private InMemoryWritePort writePort;
    private List<CanonicalEvent> publishedEvents;
    private IngestionService service;

    private static final UUID CONNECTION = UUID.randomUUID();
    private static final Instant OCCURRED = Instant.parse("2026-03-29T01:30:00Z");
    private static final ZoneOffset OFFSET = ZoneOffset.ofHours(2);

    static class InMemoryRawEventStore implements RawEventStore {
        private final Map<String, RawEvent> byKey = new HashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public Optional<RawEvent> findByNaturalKey(UUID connectionId, String providerRecordId) {
            return Optional.ofNullable(byKey.get(key(connectionId, providerRecordId)));
        }

        @Override
        public RawEvent saveIfAbsent(RawEvent event) {
            var key = key(event.connectionId(), event.providerRecordId());
            var existing = byKey.get(key);
            if (existing != null) {
                return existing;
            }
            var saved = new RawEvent(ids.incrementAndGet(), event.connectionId(), event.providerRecordId(),
                    event.payload(), event.receivedAt(), event.normalized());
            byKey.put(key, saved);
            return saved;
        }

        @Override
        public void deleteAll() {
            byKey.clear();
        }

        private static String key(UUID connectionId, String providerRecordId) {
            return connectionId + "|" + providerRecordId;
        }

        int size() {
            return byKey.size();
        }
    }

    static class InMemoryWritePort implements CanonicalWritePort {
        final Map<String, Measurement> measurements = new HashMap<>();

        @Override
        public void upsertMeasurement(Measurement measurement) {
            measurements.put(measurement.connectionId() + "|" + measurement.providerRecordId() + "|" + measurement.metricType(), measurement);
        }

        @Override
        public void upsertActivity(UUID connectionId, String providerRecordId, ActivityRecord record) {
        }

        @Override
        public void upsertMentalSession(UUID connectionId, String providerRecordId, MentalSessionRecord record) {
        }

        @Override
        public void upsertUsageDaily(UUID connectionId, String providerRecordId, UsageDailyRecord record) {
        }

        @Override
        public void upsertMoodCheckin(UUID connectionId, String providerRecordId, MoodCheckinRecord record) {
        }

        @Override
        public void deleteAll() {
            measurements.clear();
        }

        int size() {
            return measurements.size();
        }
    }

    @BeforeEach
    void setUp() {
        rawEventStore = new InMemoryRawEventStore();
        writePort = new InMemoryWritePort();
        publishedEvents = new ArrayList<>();
        service = new IngestionService(rawEventStore, writePort, publishedEvents::add,
                Clock.fixed(Instant.parse("2026-03-29T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void firstIngestIsAcceptedAndStoresRawPayload() {
        var result = service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS,
                8421, OCCURRED, OFFSET, "{\"raw\":true}");

        assertEquals(IngestOutcome.ACCEPTED, result.outcome());
        assertEquals(1, rawEventStore.size());
        assertEquals(1, writePort.size());
        var stored = rawEventStore.findByNaturalKey(CONNECTION, "rec-1").orElseThrow();
        assertEquals("{\"raw\":true}", stored.payload());
    }

    @Test
    void replayOfSameNaturalKeyIsDuplicateAndDoesNotDuplicateData() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS,
                8421, OCCURRED, OFFSET, "{\"raw\":true}");
        var replay = service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS,
                8421, OCCURRED, OFFSET, "{\"raw\":true}");

        assertEquals(IngestOutcome.DUPLICATE, replay.outcome());
        assertEquals(1, rawEventStore.size());
        assertEquals(1, writePort.size());
    }

    @Test
    void upsertOfSameKeyUpdatesValueWithoutDuplicating() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.WITHINGS, MetricType.WEIGHT_KG,
                72.5, OCCURRED, OFFSET, "{}");
        service.ingestMeasurement(CONNECTION, "rec-1", Source.WITHINGS, MetricType.WEIGHT_KG,
                72.9, OCCURRED, OFFSET, "{}");

        assertEquals(1, writePort.size());
        var stored = writePort.measurements.get(CONNECTION + "|rec-1|" + MetricType.WEIGHT_KG);
        assertEquals(72.9, stored.value());
        assertTrue(stored.zoneOffsetAtCapture().equals(OFFSET));
    }

    @Test
    void differentRecordIdsAreIndependent() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS, 100, OCCURRED, OFFSET, "{}");
        var other = service.ingestMeasurement(CONNECTION, "rec-2", Source.GARMIN, MetricType.STEPS, 200, OCCURRED, OFFSET, "{}");

        assertEquals(IngestOutcome.ACCEPTED, other.outcome());
        assertEquals(2, rawEventStore.size());
        assertEquals(2, writePort.size());
    }

    @Test
    void acceptedIngestPublishesCanonicalEventWithTypeAndPayload() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS,
                8421, OCCURRED, OFFSET, "{}");

        assertEquals(1, publishedEvents.size());
        var event = publishedEvents.getFirst();
        assertEquals(EventType.MEASUREMENT, event.type());
        assertEquals(Instant.parse("2026-03-29T12:00:00Z"), event.occurredAt());
        var payload = (Measurement) event.payload();
        assertEquals(8421, payload.value());
        assertEquals(MetricType.STEPS, payload.metricType());
    }

    @Test
    void duplicateReplayPublishesNoEvent() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS, 100, OCCURRED, OFFSET, "{}");
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS, 100, OCCURRED, OFFSET, "{}");

        assertEquals(1, publishedEvents.size());
    }
}
