package dev.healthhub.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConnectionServiceTest {

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
            byId.remove(id);
        }

        @Override
        public void deleteAll() {
            byId.clear();
        }
    }

    private static ProviderOAuthPort oauthPort(String provider) {
        return new ProviderOAuthPort() {
            @Override
            public String provider() {
                return provider;
            }

            @Override
            public AuthorizationInit startAuthorization() {
                return new AuthorizationInit("https://vendor.example/authorize", "state");
            }

            @Override
            public ConnectionInfo handleCallback(String code, String state) {
                throw new UnsupportedOperationException();
            }
        };
    }

    static class FakeSettingsPort implements ProviderSettingsPort {
        final Map<String, Map<String, String>> byProvider = new HashMap<>();

        @Override
        public void save(String provider, Map<String, String> credentials) {
            byProvider.put(provider, Map.copyOf(credentials));
        }

        @Override
        public Optional<Map<String, String>> find(String provider) {
            return Optional.ofNullable(byProvider.get(provider));
        }

        @Override
        public List<String> keys(String provider) {
            return find(provider).map(m -> m.keySet().stream().sorted().toList()).orElse(List.of());
        }

        @Override
        public boolean isConfigured(String provider) {
            return byProvider.containsKey(provider);
        }

        @Override
        public void delete(String provider) {
            byProvider.remove(provider);
        }
    }

    private ConnectionService serviceWith(FakeConnectionStore store, List<ProviderOAuthPort> oauthPorts) {
        return serviceWith(store, oauthPorts, List.of(), new FakeSettingsPort());
    }

    private ConnectionService serviceWith(FakeConnectionStore store, List<ProviderOAuthPort> oauthPorts,
            List<ProviderSyncPort> syncPorts, FakeSettingsPort settingsPort) {
        return new ConnectionService(store, new NoopCredentialsPort(), settingsPort, oauthPorts, syncPorts, List.of(),
                new SyncOrchestrator(List.of(), null, new SyncAuditStore() {
                    @Override
                    public void record(SyncAuditEntry entry) {
                    }

                    @Override
                    public void deleteAll() {
                    }
                }, store, e -> {
                }, Clock.systemUTC()));
    }

    static class NoopCredentialsPort implements ConnectionCredentialsPort {
        @Override
        public Optional<ProviderCredentials> credentialsFor(UUID connectionId) {
            return Optional.empty();
        }

        @Override
        public void save(UUID connectionId, String externalUserId, String accessToken, String refreshToken,
                Instant expiresAt) {
        }

        @Override
        public void revoke(UUID connectionId) {
        }
    }

    @Test
    void connectCreatesConnectionForKeyBasedProvider() {
        var store = new FakeConnectionStore();
        ProviderSyncPort garmin = new ProviderSyncPort() {
            @Override
            public String provider() {
                return "garmin";
            }

            @Override
            public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
                return List.of();
            }
        };
        var service = serviceWith(store, List.of(oauthPort("withings")), List.of(garmin), new FakeSettingsPort());

        var connection = service.connect("garmin");

        assertEquals("garmin", connection.provider());
        assertEquals(1, store.list().size());
    }

    @Test
    void connectRejectsOAuthProvider() {
        var store = new FakeConnectionStore();
        var service = serviceWith(store, List.of(oauthPort("withings")));

        assertThrows(IllegalArgumentException.class, () -> service.connect("withings"));
    }

    @Test
    void savesAndReportsCredentialsWithoutExposingValues() {
        var settings = new FakeSettingsPort();
        var service = serviceWith(new FakeConnectionStore(), List.of(oauthPort("withings")), List.of(), settings);

        service.saveCredentials("withings", Map.of("clientId", "user-id", "clientSecret", "user-secret"));

        assertEquals(Map.of("clientId", "user-id", "clientSecret", "user-secret"), settings.find("withings").orElseThrow());
        var status = service.credentialsStatus("withings");
        assertEquals(true, status.configured());
        assertEquals(List.of("clientId", "clientSecret"), status.keys());

        service.deleteCredentials("withings");
        assertEquals(false, service.credentialsStatus("withings").configured());
    }

    @Test
    void rejectsUnknownProviderAndInvalidCredentials() {
        var service = serviceWith(new FakeConnectionStore(), List.of(oauthPort("withings")));
        assertThrows(IllegalArgumentException.class, () -> service.saveCredentials("unknown", Map.of("k", "v")));
        assertThrows(IllegalArgumentException.class, () -> service.saveCredentials("withings", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> service.saveCredentials("withings", Map.of("bad key!", "v")));
        assertThrows(IllegalArgumentException.class, () -> service.saveCredentials("withings", Map.of("clientId", "  ")));
    }

    @Test
    void listsProvidersWithOAuthAndConfiguredFlags() {
        var settings = new FakeSettingsPort();
        settings.save("garmin", Map.of("personalKey", "k", "userId", "u1"));
        ProviderSyncPort garmin = new ProviderSyncPort() {
            @Override
            public String provider() {
                return "garmin";
            }

            @Override
            public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
                return List.of();
            }
        };
        var service = serviceWith(new FakeConnectionStore(), List.of(oauthPort("withings")), List.of(garmin), settings);

        var providers = service.listProviders();

        assertEquals(2, providers.size());
        assertEquals(new ProviderInfo("withings", true, false), providers.get(0));
        assertEquals(new ProviderInfo("garmin", false, true), providers.get(1));
    }
}
