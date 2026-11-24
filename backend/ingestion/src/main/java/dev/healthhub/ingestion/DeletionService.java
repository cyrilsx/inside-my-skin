package dev.healthhub.ingestion;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeletionService {

    private static final Logger log = LoggerFactory.getLogger(DeletionService.class);

    private final DeletionJobStore jobStore;
    private final CanonicalWritePort writePort;
    private final RawEventStore rawEventStore;
    private final SyncAuditStore syncAuditStore;
    private final ConnectionStore connectionStore;
    private final CanonicalEventPublisher eventPublisher;
    private final Clock clock;

    public DeletionService(DeletionJobStore jobStore, CanonicalWritePort writePort, RawEventStore rawEventStore,
            SyncAuditStore syncAuditStore, ConnectionStore connectionStore, CanonicalEventPublisher eventPublisher,
            Clock clock) {
        this.jobStore = jobStore;
        this.writePort = writePort;
        this.rawEventStore = rawEventStore;
        this.syncAuditStore = syncAuditStore;
        this.connectionStore = connectionStore;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public DeletionJob requestDeletion() {
        log.info("deletion requested");
        return jobStore.save(new DeletionJob(UUID.randomUUID(), "PENDING", Instant.now(clock), null));
    }

    public void executeDeletion(UUID jobId) {
        log.info("deletion job started: id={}", jobId);
        jobStore.updateStatus(jobId, "RUNNING", null);
        try {
            writePort.deleteAll();
            rawEventStore.deleteAll();
            syncAuditStore.deleteAll();
            connectionStore.deleteAll();
            var completedAt = Instant.now(clock);
            jobStore.updateStatus(jobId, "COMPLETED", completedAt);
            log.info("deletion job completed: id={}", jobId);
            publishDeletion(jobId, "COMPLETED", null, completedAt);
        } catch (Exception e) {
            log.error("deletion job failed: id={}", jobId, e);
            var failedAt = Instant.now(clock);
            jobStore.updateStatus(jobId, "FAILED", failedAt);
            publishDeletion(jobId, "FAILED", e.getMessage(), failedAt);
        }
    }

    private void publishDeletion(UUID jobId, String status, String error, Instant at) {
        var payload = error == null
                ? Map.of("jobId", jobId.toString(), "status", status)
                : Map.of("jobId", jobId.toString(), "status", status, "error", String.valueOf(error));
        eventPublisher.publish(new CanonicalEvent(UUID.randomUUID().toString(), EventType.DELETION, at, payload));
    }
}
