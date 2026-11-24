package dev.healthhub.ingestion;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SyncOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SyncOrchestrator.class);

    private final List<ProviderSyncPort> syncPorts;
    private final IngestionService ingestionService;
    private final SyncAuditStore auditStore;
    private final ConnectionStore connectionStore;
    private final CanonicalEventPublisher eventPublisher;
    private final Clock clock;

    public SyncOrchestrator(List<ProviderSyncPort> syncPorts, IngestionService ingestionService,
            SyncAuditStore auditStore, ConnectionStore connectionStore, CanonicalEventPublisher eventPublisher,
            Clock clock) {
        this.syncPorts = syncPorts;
        this.ingestionService = ingestionService;
        this.auditStore = auditStore;
        this.connectionStore = connectionStore;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public SyncResult sync(UUID connectionId) {
        var connection = connectionStore.find(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("unknown connection: " + connectionId));
        var syncPort = syncPorts.stream()
                .filter(p -> p.provider().equals(connection.provider()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no sync port for provider " + connection.provider()));
        var since = connection.lastSyncAt() != null ? connection.lastSyncAt() : Instant.EPOCH;
        var startedAt = Instant.now(clock);
        log.info("sync started: connection={} provider={}", connectionId, connection.provider());
        try {
            var records = syncPort.fetch(connectionId, since);
            int ingested = 0;
            int duplicates = 0;
            for (var record : records) {
                var result = ingestionService.ingestMeasurement(
                        connectionId, record.providerRecordId(), record.source(), record.metricType(), record.value(),
                        record.occurredAt(), record.zoneOffsetAtCapture(), record.rawPayload());
                if (result.outcome() == IngestOutcome.DUPLICATE) {
                    duplicates++;
                } else {
                    ingested++;
                }
            }
                var finishedAt = Instant.now(clock);
            log.info("sync completed: connection={} provider={} fetched={} ingested={} duplicates={}", connectionId,
                    connection.provider(), records.size(), ingested, duplicates);
            var syncResult = new SyncResult(connectionId, records.size(), ingested, duplicates, finishedAt);
            var entry = new SyncAuditEntry(connectionId, connection.provider(), startedAt, finishedAt,
                    "OK", records.size(), ingested, duplicates, null);
            auditStore.record(entry);
            eventPublisher.publish(new CanonicalEvent(
                    UUID.randomUUID().toString(), EventType.SYNC, finishedAt, entry));
            connectionStore.markSynced(connectionId, finishedAt, "OK");
            return syncResult;
        } catch (Exception e) {
            var finishedAt = Instant.now(clock);
            log.error("sync failed: connection={} provider={}", connectionId, connection.provider(), e);
            var entry = new SyncAuditEntry(connectionId, connection.provider(), startedAt, finishedAt,
                    "FAILED", 0, 0, 0, e.getMessage());
            auditStore.record(entry);
            eventPublisher.publish(new CanonicalEvent(
                    UUID.randomUUID().toString(), EventType.SYNC, finishedAt, entry));
            connectionStore.markSynced(connectionId, finishedAt, "FAILED");
            throw e;
        }
    }
}
