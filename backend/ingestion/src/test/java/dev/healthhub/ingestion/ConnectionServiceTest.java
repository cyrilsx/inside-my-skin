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

    private ConnectionService serviceWith(FakeConnectionStore store, List<ProviderOAuthPort> oauthPorts) {
        return new ConnectionService(store, new NoopCredentialsPort(), oauthPorts, List.of(),
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
        var service = serviceWith(store, List.of(oauthPort("withings")));

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
}
