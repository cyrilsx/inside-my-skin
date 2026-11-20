package dev.healthhub.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
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

class SyncOrchestratorTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
    private static final UUID CONNECTION = UUID.randomUUID();

    private List<CanonicalEvent> events;
    private List<SyncAuditEntry> auditEntries;

    static class FakeConnectionStore implements ConnectionStore {
        final Map<UUID, ConnectionInfo> byId = new HashMap<>();

        @Override
        public ConnectionInfo saveNew(String provider, String label, Instant connectedAt) {
            var info = new ConnectionInfo(UUID.randomUUID(), provider, label, connectedAt, null, null);
            byId.put(info.id(), info);
            return info;
        }

        @Override
        public List<ConnectionInfo> list() {
            return new ArrayList<>(byId.values());
        }

        @Override
        public Optional<ConnectionInfo> find(UUID id) {
            return Optional.ofNullable(byId.get(id));
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
        }
    }

    static class FakeAuditStore implements SyncAuditStore {
        final List<SyncAuditEntry> entries = new ArrayList<>();

        @Override
        public void record(SyncAuditEntry entry) {
            entries.add(entry);
        }

        @Override
        public void deleteAll() {
        }
    }

    static class StubIngestionService extends IngestionService {
        StubIngestionService() {
            super(null, null, event -> {
            }, Clock.fixed(NOW, ZoneOffset.UTC));
        }

        @Override
        public IngestResult ingestMeasurement(UUID connectionId, String providerRecordId, Source source,
                MetricType metricType, double value, Instant occurredAt, ZoneOffset zoneOffsetAtCapture,
                String rawPayload) {
            return new IngestResult(IngestOutcome.ACCEPTED, 1L);
        }
    }

    private SyncOrchestrator orchestrator(ProviderSyncPort port, FakeConnectionStore store, FakeAuditStore audit) {
        return new SyncOrchestrator(List.of(port), new StubIngestionService(), audit, store, events::add,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @BeforeEach
    void setUp() {
        events = new ArrayList<>();
        auditEntries = new ArrayList<>();
    }

    @Test
    void successfulSyncPublishesSyncEventWithAuditEntry() {
        var store = new FakeConnectionStore();
        var connection = store.saveNew("garmin", "garmin", NOW);
        var audit = new FakeAuditStore();
        ProviderSyncPort port = new ProviderSyncPort() {
            @Override
            public String provider() {
                return "garmin";
            }

            @Override
            public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
                return List.of(new ProviderRecord("r1", Source.GARMIN, MetricType.STEPS, 100,
                        NOW, ZoneOffset.UTC, "{}"));
            }
        };

        var result = orchestrator(port, store, audit).sync(connection.id());

        assertEquals(1, result.ingested());
        assertEquals(1, events.size());
        var event = events.getFirst();
        assertEquals(EventType.SYNC, event.type());
        var payload = assertInstanceOf(SyncAuditEntry.class, event.payload());
        assertEquals("OK", payload.status());
        assertEquals(1, payload.ingested());
    }

    @Test
    void failingSyncPublishesSyncEventWithFailedStatus() {
        var store = new FakeConnectionStore();
        var connection = store.saveNew("garmin", "garmin", NOW);
        var audit = new FakeAuditStore();
        ProviderSyncPort port = new ProviderSyncPort() {
            @Override
            public String provider() {
                return "garmin";
            }

            @Override
            public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
                throw new IllegalStateException("vendor timeout");
            }
        };

        assertThrows(IllegalStateException.class, () -> orchestrator(port, store, audit).sync(connection.id()));

        assertEquals(1, events.size());
        var payload = assertInstanceOf(SyncAuditEntry.class, events.getFirst().payload());
        assertEquals("FAILED", payload.status());
        assertEquals("vendor timeout", payload.error());
    }
}
