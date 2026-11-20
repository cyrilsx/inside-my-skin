package dev.healthhub.app.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.healthhub.app.persistence.ConnectionEntity;
import dev.healthhub.app.persistence.ConnectionRepository;
import dev.healthhub.app.persistence.DeletionJobRepository;
import dev.healthhub.app.persistence.RawEventEntity;
import dev.healthhub.app.persistence.RawEventRepository;
import dev.healthhub.app.persistence.SyncAuditEntity;
import dev.healthhub.app.persistence.SyncAuditRepository;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.DeletionService;
import dev.healthhub.ingestion.MeasurementStore;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DeletionServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @MockitoBean
    JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void securityProps(DynamicPropertyRegistry registry) {
        var keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        registry.add("TOKEN_ENCRYPTION_KEY", () -> Base64.getEncoder().encodeToString(keyBytes));
    }

    @Autowired
    DeletionService deletionService;

    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    RawEventRepository rawEventRepository;

    @Autowired
    SyncAuditRepository syncAuditRepository;

    @Autowired
    DeletionJobRepository deletionJobRepository;

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
    void deletionRemovesAllDataAndTokensAndCompletesTheJob() {
        measurementStore.upsert(new dev.healthhub.core.Measurement(
                connectionId, "rec-1", Source.GARMIN, MetricType.STEPS, 8421,
                Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), Instant.parse("2026-03-29T12:00:00Z")));
        rawEventRepository.save(new RawEventEntity(
                connectionId, "rec-1", "{\"raw\":true}", Instant.parse("2026-03-29T12:00:00Z"), false));
        syncAuditRepository.save(new SyncAuditEntity(
                connectionId, "garmin", Instant.parse("2026-03-29T12:00:00Z"), Instant.parse("2026-03-29T12:01:00Z"),
                "OK", 1, 1, 0, null));

        var job = deletionService.requestDeletion();
        deletionService.executeDeletion(job.id());

        assertEquals(java.util.Optional.empty(),
                measurementStore.findByNaturalKey(connectionId, "rec-1", MetricType.STEPS));
        assertEquals(0, rawEventRepository.count());
        assertEquals(0, syncAuditRepository.count());
        assertEquals(0, connectionRepository.count());
        var completed = deletionJobRepository.findById(job.id()).orElseThrow();
        assertEquals("COMPLETED", completed.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(completed.getCompletedAt());
    }
}
