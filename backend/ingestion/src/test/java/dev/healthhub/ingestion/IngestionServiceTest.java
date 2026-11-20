package dev.healthhub.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IngestionServiceTest {

    private InMemoryRawEventStore rawEventStore;
    private InMemoryMeasurementStore measurementStore;
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

        private static String key(UUID connectionId, String providerRecordId) {
            return connectionId + "|" + providerRecordId;
        }

        int size() {
            return byKey.size();
        }
    }

    static class InMemoryMeasurementStore implements MeasurementStore {
        private final Map<String, Measurement> byKey = new HashMap<>();

        @Override
        public Measurement upsert(Measurement measurement) {
            byKey.put(key(measurement), measurement);
            return measurement;
        }

        @Override
        public Optional<Measurement> findByNaturalKey(UUID connectionId, String providerRecordId, MetricType metricType) {
            return Optional.ofNullable(byKey.get(key(connectionId, providerRecordId, metricType)));
        }

        private static String key(Measurement m) {
            return key(m.connectionId(), m.providerRecordId(), m.metricType());
        }

        private static String key(UUID connectionId, String providerRecordId, MetricType metricType) {
            return connectionId + "|" + providerRecordId + "|" + metricType;
        }

        int size() {
            return byKey.size();
        }
    }

    @BeforeEach
    void setUp() {
        rawEventStore = new InMemoryRawEventStore();
        measurementStore = new InMemoryMeasurementStore();
        service = new IngestionService(rawEventStore, measurementStore, Clock.fixed(Instant.parse("2026-03-29T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void firstIngestIsAcceptedAndStoresRawPayload() {
        var result = service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS,
                8421, OCCURRED, OFFSET, "{\"raw\":true}");

        assertEquals(IngestOutcome.ACCEPTED, result.outcome());
        assertEquals(1, rawEventStore.size());
        assertEquals(1, measurementStore.size());
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
        assertEquals(1, measurementStore.size());
    }

    @Test
    void upsertOfSameKeyUpdatesValueWithoutDuplicating() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.WITHINGS, MetricType.WEIGHT_KG,
                72.5, OCCURRED, OFFSET, "{}");
        service.ingestMeasurement(CONNECTION, "rec-1", Source.WITHINGS, MetricType.WEIGHT_KG,
                72.9, OCCURRED, OFFSET, "{}");

        assertEquals(1, measurementStore.size());
        var stored = measurementStore.findByNaturalKey(CONNECTION, "rec-1", MetricType.WEIGHT_KG).orElseThrow();
        assertEquals(72.9, stored.value());
        assertTrue(stored.zoneOffsetAtCapture().equals(OFFSET));
    }

    @Test
    void differentRecordIdsAreIndependent() {
        service.ingestMeasurement(CONNECTION, "rec-1", Source.GARMIN, MetricType.STEPS, 100, OCCURRED, OFFSET, "{}");
        var other = service.ingestMeasurement(CONNECTION, "rec-2", Source.GARMIN, MetricType.STEPS, 200, OCCURRED, OFFSET, "{}");

        assertEquals(IngestOutcome.ACCEPTED, other.outcome());
        assertEquals(2, rawEventStore.size());
        assertEquals(2, measurementStore.size());
    }
}
