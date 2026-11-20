package dev.healthhub.app.sync;

import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.SyncOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "healthhub.sync", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncScheduler.class);

    private final ConnectionStore connectionStore;
    private final SyncOrchestrator syncOrchestrator;

    public SyncScheduler(ConnectionStore connectionStore, SyncOrchestrator syncOrchestrator) {
        this.connectionStore = connectionStore;
        this.syncOrchestrator = syncOrchestrator;
    }

    @Scheduled(
            fixedDelayString = "${healthhub.sync.interval-ms:3600000}",
            initialDelayString = "${healthhub.sync.initial-delay-ms:60000}")
    public void syncAll() {
        for (var connection : connectionStore.list()) {
            try {
                syncOrchestrator.sync(connection.id());
            } catch (Exception e) {
                log.error("Scheduled sync failed for connection {} ({})", connection.id(), connection.provider(), e);
            }
        }
    }
}
