package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class ConnectionService {

    private static final Pattern CREDENTIAL_KEY = Pattern.compile("[A-Za-z0-9_.-]{1,64}");
    private static final int MAX_ENTRIES = 16;
    private static final int MAX_VALUE_LENGTH = 4096;

    private final ConnectionStore connectionStore;
    private final ConnectionCredentialsPort credentialsPort;
    private final ProviderSettingsPort settingsPort;
    private final List<ProviderOAuthPort> oauthPorts;
    private final List<ProviderSyncPort> syncPorts;
    private final List<WebhookHandlerPort> webhookHandlers;
    private final SyncOrchestrator syncOrchestrator;

    public ConnectionService(ConnectionStore connectionStore, ConnectionCredentialsPort credentialsPort,
            ProviderSettingsPort settingsPort, List<ProviderOAuthPort> oauthPorts, List<ProviderSyncPort> syncPorts,
            List<WebhookHandlerPort> webhookHandlers, SyncOrchestrator syncOrchestrator) {
        this.connectionStore = connectionStore;
        this.credentialsPort = credentialsPort;
        this.settingsPort = settingsPort;
        this.oauthPorts = oauthPorts;
        this.syncPorts = syncPorts;
        this.webhookHandlers = webhookHandlers;
        this.syncOrchestrator = syncOrchestrator;
    }

    public List<ConnectionInfo> list() {
        return connectionStore.list();
    }

    public List<ProviderInfo> listProviders() {
        var providers = new LinkedHashMap<String, ProviderInfo>();
        for (var p : oauthPorts) {
            providers.put(p.provider(), new ProviderInfo(p.provider(), true, settingsPort.isConfigured(p.provider())));
        }
        for (var p : syncPorts) {
            providers.putIfAbsent(p.provider(), new ProviderInfo(p.provider(), false, settingsPort.isConfigured(p.provider())));
        }
        for (var h : webhookHandlers) {
            providers.putIfAbsent(h.provider(), new ProviderInfo(h.provider(), false, settingsPort.isConfigured(h.provider())));
        }
        return List.copyOf(providers.values());
    }

    public void saveCredentials(String provider, Map<String, String> credentials) {
        requireKnownProvider(provider);
        if (credentials == null || credentials.isEmpty() || credentials.size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("credentials must contain between 1 and " + MAX_ENTRIES + " entries");
        }
        for (var entry : credentials.entrySet()) {
            if (!CREDENTIAL_KEY.matcher(entry.getKey()).matches()) {
                throw new IllegalArgumentException("invalid credential key: " + entry.getKey());
            }
            var value = entry.getValue();
            if (value == null || value.isBlank() || value.length() > MAX_VALUE_LENGTH) {
                throw new IllegalArgumentException("invalid credential value for key: " + entry.getKey());
            }
        }
        settingsPort.save(provider, credentials);
    }

    public CredentialsStatus credentialsStatus(String provider) {
        requireKnownProvider(provider);
        return new CredentialsStatus(settingsPort.isConfigured(provider), settingsPort.keys(provider));
    }

    public void deleteCredentials(String provider) {
        requireKnownProvider(provider);
        settingsPort.delete(provider);
    }

    public AuthorizationInit startAuthorization(String provider) {
        return oauthPort(provider).startAuthorization();
    }

    public ConnectionInfo connect(String provider) {
        var oauthBased = oauthPorts.stream().anyMatch(p -> p.provider().equals(provider));
        if (oauthBased) {
            throw new IllegalArgumentException("provider " + provider + " requires OAuth; use /authorize");
        }
        requireKnownProvider(provider);
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

    private void requireKnownProvider(String provider) {
        var known = oauthPorts.stream().anyMatch(p -> p.provider().equals(provider))
                || syncPorts.stream().anyMatch(p -> p.provider().equals(provider))
                || webhookHandlers.stream().anyMatch(h -> h.provider().equals(provider));
        if (!known) {
            throw new IllegalArgumentException("unknown provider: " + provider);
        }
    }

    private ProviderOAuthPort oauthPort(String provider) {
        return oauthPorts.stream()
                .filter(p -> p.provider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown provider: " + provider));
    }
}
