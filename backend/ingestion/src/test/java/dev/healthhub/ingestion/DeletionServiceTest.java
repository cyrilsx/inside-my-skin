package dev.healthhub.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeletionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private InMemoryJobStore jobStore;
    private RecordingWritePort writePort;
    private RecordingRawEventStore rawEventStore;
    private RecordingSyncAuditStore auditStore;
    private RecordingConnectionStore connectionStore;
    private List<CanonicalEvent> events;
    private List<String> callOrder;
    private DeletionService service;

    class InMemoryJobStore implements DeletionJobStore {
        final Map<UUID, DeletionJob> byId = new HashMap<>();

        @Override
        public DeletionJob save(DeletionJob job) {
            byId.put(job.id(), job);
            return job;
        }

        @Override
        public void updateStatus(UUID id, String status, Instant completedAt) {
            var job = byId.get(id);
            byId.put(id, new DeletionJob(job.id(), status, job.createdAt(), completedAt));
        }

        @Override
        public Optional<DeletionJob> find(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    class RecordingWritePort implements CanonicalWritePort {
        @Override
        public void upsertMeasurement(Measurement measurement) {
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
            callOrder.add("canonical");
        }
    }

    class RecordingRawEventStore implements RawEventStore {
        @Override
        public Optional<RawEvent> findByNaturalKey(UUID connectionId, String providerRecordId) {
            return Optional.empty();
        }

        @Override
        public RawEvent saveIfAbsent(RawEvent event) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            callOrder.add("raw");
        }
    }

    class RecordingSyncAuditStore implements SyncAuditStore {
        @Override
        public void record(SyncAuditEntry entry) {
        }

        @Override
        public void deleteAll() {
            callOrder.add("audit");
        }
    }

    class RecordingConnectionStore implements ConnectionStore {
        @Override
        public ConnectionInfo saveNew(String provider, String label, Instant connectedAt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ConnectionInfo> list() {
            return List.of();
        }

        @Override
        public Optional<ConnectionInfo> find(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<ConnectionInfo> findByExternalUserId(String provider, String externalUserId) {
            return Optional.empty();
        }

        @Override
        public void markSynced(UUID id, Instant finishedAt, String status) {
        }

        @Override
        public void delete(UUID id) {
        }

        @Override
        public void deleteAll() {
            callOrder.add("connections");
        }
    }

    @BeforeEach
    void setUp() {
        jobStore = new InMemoryJobStore();
        writePort = new RecordingWritePort();
        rawEventStore = new RecordingRawEventStore();
        auditStore = new RecordingSyncAuditStore();
        connectionStore = new RecordingConnectionStore();
        events = new ArrayList<>();
        callOrder = new ArrayList<>();
        service = new DeletionService(jobStore, writePort, rawEventStore, auditStore, connectionStore,
                events::add, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void requestDeletionReturnsPendingJob() {
        var job = service.requestDeletion();

        assertEquals("PENDING", job.status());
        assertEquals(NOW, job.createdAt());
        assertNull(job.completedAt());
    }

    @Test
    void executeDeletionDeletesEverythingConnectionsLastAndCompletesWithEvent() {
        var job = service.requestDeletion();

        service.executeDeletion(job.id());

        assertEquals(List.of("canonical", "raw", "audit", "connections"), callOrder);
        var completed = jobStore.find(job.id()).orElseThrow();
        assertEquals("COMPLETED", completed.status());
        assertEquals(NOW, completed.completedAt());
        assertEquals(1, events.size());
        var event = events.getFirst();
        assertEquals(EventType.DELETION, event.type());
        var payload = assertInstanceOf(Map.class, event.payload());
        assertEquals("COMPLETED", payload.get("status"));
        assertEquals(job.id().toString(), payload.get("jobId"));
    }

    @Test
    void executeDeletionFailureMarksJobFailedAndPublishesEvent() {
        var failingWritePort = new RecordingWritePort() {
            @Override
            public void deleteAll() {
                callOrder.add("canonical");
                throw new IllegalStateException("db down");
            }
        };
        var failing = new DeletionService(jobStore, failingWritePort, rawEventStore, auditStore,
                connectionStore, events::add, Clock.fixed(NOW, ZoneOffset.UTC));
        var job = failing.requestDeletion();

        failing.executeDeletion(job.id());

        var failed = jobStore.find(job.id()).orElseThrow();
        assertEquals("FAILED", failed.status());
        assertTrue(callOrder.contains("canonical"));
        assertEquals(1, events.size());
        var payload = assertInstanceOf(Map.class, events.getFirst().payload());
        assertEquals("FAILED", payload.get("status"));
        assertEquals("db down", payload.get("error"));
    }
}
