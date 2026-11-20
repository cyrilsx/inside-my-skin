package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class ConnectionService {

    private final ConnectionStore connectionStore;
    private final ConnectionCredentialsPort credentialsPort;
    private final List<ProviderOAuthPort> oauthPorts;
    private final List<WebhookHandlerPort> webhookHandlers;
    private final SyncOrchestrator syncOrchestrator;

    public ConnectionService(ConnectionStore connectionStore, ConnectionCredentialsPort credentialsPort,
            List<ProviderOAuthPort> oauthPorts, List<WebhookHandlerPort> webhookHandlers,
            SyncOrchestrator syncOrchestrator) {
        this.connectionStore = connectionStore;
        this.credentialsPort = credentialsPort;
        this.oauthPorts = oauthPorts;
        this.webhookHandlers = webhookHandlers;
        this.syncOrchestrator = syncOrchestrator;
    }

    public List<ConnectionInfo> list() {
        return connectionStore.list();
    }

    public AuthorizationInit startAuthorization(String provider) {
        return oauthPort(provider).startAuthorization();
    }

    public ConnectionInfo connect(String provider) {
        var oauthBased = oauthPorts.stream().anyMatch(p -> p.provider().equals(provider));
        if (oauthBased) {
            throw new IllegalArgumentException("provider " + provider + " requires OAuth; use /authorize");
        }
        return connectionStore.saveNew(provider, provider, Instant.now());
    }

    public ConnectionInfo handleCallback(String provider, String code, String state) {
        return oauthPort(provider).handleCallback(code, state);
    }

    public SyncResult sync(UUID connectionId) {
        return syncOrchestrator.sync(connectionId);
    }

    public void revoke(UUID connectionId) {
        connectionStore.find(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("unknown connection: " + connectionId));
        credentialsPort.revoke(connectionId);
        connectionStore.delete(connectionId);
    }

    public void handleWebhook(String provider, JsonNode body) {
        webhookHandlers.stream()
                .filter(h -> h.provider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown webhook provider: " + provider))
                .handle(body);
    }

    private ProviderOAuthPort oauthPort(String provider) {
        return oauthPorts.stream()
                .filter(p -> p.provider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown provider: " + provider));
    }
}
