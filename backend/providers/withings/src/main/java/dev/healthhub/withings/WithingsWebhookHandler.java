package dev.healthhub.withings;

import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.SyncOrchestrator;
import dev.healthhub.ingestion.WebhookHandlerPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@ConditionalOnProperty(prefix = "healthhub.withings", name = "enabled")
public class WithingsWebhookHandler implements WebhookHandlerPort {

    private final ConnectionStore connectionStore;
    private final SyncOrchestrator syncOrchestrator;

    public WithingsWebhookHandler(ConnectionStore connectionStore, SyncOrchestrator syncOrchestrator) {
        this.connectionStore = connectionStore;
        this.syncOrchestrator = syncOrchestrator;
    }

    @Override
    public String provider() {
        return "withings";
    }

    @Override
    public void handle(JsonNode body) {
        var externalUserId = body.path("userid").asText("");
        if (externalUserId.isBlank()) {
            throw new IllegalArgumentException("webhook body missing userid");
        }
        var connection = connectionStore.findByExternalUserId(provider(), externalUserId)
                .orElseThrow(() -> new IllegalArgumentException("no connection for vendor user " + externalUserId));
        syncOrchestrator.sync(connection.id());
    }
}
