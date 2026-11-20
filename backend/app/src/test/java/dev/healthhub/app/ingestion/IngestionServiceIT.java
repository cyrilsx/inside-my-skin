package dev.healthhub.app.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.healthhub.app.persistence.ConnectionEntity;
import dev.healthhub.app.persistence.ConnectionRepository;
import dev.healthhub.app.persistence.RawEventEntity;
import dev.healthhub.app.persistence.RawEventRepository;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.IngestOutcome;
import dev.healthhub.ingestion.IngestionService;
import dev.healthhub.ingestion.MeasurementStore;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class IngestionServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    IngestionService ingestionService;

    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    RawEventRepository rawEventRepository;

    @Autowired
    MeasurementStore measurementStore;

    private UUID connectionId;

    @BeforeEach
    void setUp() {
        connectionId = UUID.randomUUID();
        connectionRepository.save(new ConnectionEntity(
                connectionId, "GARMIN", "watch", Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    void replayIsIdempotentUnderConnectionAndProviderRecordId() {
        var occurred = Instant.parse("2026-03-29T01:30:00Z");
        var offset = ZoneOffset.ofHours(2);

        var first = ingestionService.ingestMeasurement(
                connectionId, "rec-1", Source.GARMIN, MetricType.STEPS, 8421, occurred, offset, "{\"raw\":true}");
        var replay = ingestionService.ingestMeasurement(
                connectionId, "rec-1", Source.GARMIN, MetricType.STEPS, 8421, occurred, offset, "{\"raw\":true}");

        assertEquals(IngestOutcome.ACCEPTED, first.outcome());
        assertEquals(IngestOutcome.DUPLICATE, replay.outcome());
        assertEquals(1, rawEventRepository.findByConnectionIdAndProviderRecordId(connectionId, "rec-1").stream().count());
    }

    @Test
    void rawPayloadIsRetainedWithOriginalZoneOffset() {
        var occurred = Instant.parse("2026-03-29T01:30:00Z");
        ingestionService.ingestMeasurement(
                connectionId, "rec-2", Source.WITHINGS, MetricType.WEIGHT_KG, 72.5, occurred, ZoneOffset.ofHours(2), "{\"raw\":true}");

        var stored = rawEventRepository.findByConnectionIdAndProviderRecordId(connectionId, "rec-2").orElseThrow();
        assertEquals("{\"raw\":true}", stored.getPayload());
    }

    @Test
    void uniqueConstraintRejectsDuplicateNaturalKey() {
        var event = new RawEventEntity(connectionId, "rec-3", "{}", Instant.parse("2026-01-01T00:00:00Z"), false);
        rawEventRepository.saveAndFlush(event);
        assertThrows(DataIntegrityViolationException.class, () -> {
            rawEventRepository.saveAndFlush(new RawEventEntity(connectionId, "rec-3", "{}", Instant.parse("2026-01-01T00:00:00Z"), false));
        });
    }

    @Test
    void measurementKeepsUtcInstantAndOriginalZoneOffsetAcrossDstFallback() {
        var occurred = Instant.parse("2026-10-25T00:30:00Z");
        var offset = ZoneOffset.ofHours(2);
        ingestionService.ingestMeasurement(
                connectionId, "rec-4", Source.GARMIN, MetricType.HRV_RMSSD_S, 0.045, occurred, offset, "{}");

        var stored = measurementStore
                .findByNaturalKey(connectionId, "rec-4", MetricType.HRV_RMSSD_S).orElseThrow();
        assertEquals(occurred, stored.occurredAt());
        assertEquals(offset, stored.zoneOffsetAtCapture());
        assertEquals(0.045, stored.value());
        assertTrue(stored.receivedAt() != null);
    }
}
