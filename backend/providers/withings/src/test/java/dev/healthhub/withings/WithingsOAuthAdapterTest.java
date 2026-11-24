package dev.healthhub.withings;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.healthhub.ingestion.AuthorizationInit;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.ProviderCredentials;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class WithingsOAuthAdapterTest {

    static class FakeSettingsPort implements dev.healthhub.ingestion.ProviderSettingsPort {
        final java.util.Map<String, java.util.Map<String, String>> byProvider = new java.util.HashMap<>();

        @Override
        public void save(String provider, java.util.Map<String, String> credentials) {
            byProvider.put(provider, java.util.Map.copyOf(credentials));
        }

        @Override
        public java.util.Optional<java.util.Map<String, String>> find(String provider) {
            return java.util.Optional.ofNullable(byProvider.get(provider));
        }

        @Override
        public java.util.List<String> keys(String provider) {
            return find(provider).map(m -> m.keySet().stream().sorted().toList()).orElse(java.util.List.of());
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


    private WireMockServer wireMock;
    private WithingsOAuthAdapter adapter;
    private FakeSettingsPort settingsPort;
    private WithingsStateCodec stateCodec;
    private FakeConnectionStore connectionStore;
    private FakeCredentialsPort credentialsPort;

    static class FakeConnectionStore implements ConnectionStore {
        final Map<UUID, ConnectionInfo> byId = new HashMap<>();
        final List<ConnectionInfo> saved = new ArrayList<>();

        @Override
        public ConnectionInfo saveNew(String provider, String label, Instant connectedAt) {
            var info = new ConnectionInfo(UUID.randomUUID(), provider, label, connectedAt, null, null);
            byId.put(info.id(), info);
            saved.add(info);
            return info;
        }

        @Override
        public List<ConnectionInfo> list() {
            return List.copyOf(byId.values());
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

    static class FakeCredentialsPort implements ConnectionCredentialsPort {
        String externalUserId;
        String accessToken;

        @Override
        public Optional<ProviderCredentials> credentialsFor(UUID connectionId) {
            return Optional.empty();
        }

        @Override
        public void save(UUID connectionId, String externalUserId, String accessToken, String refreshToken,
                Instant expiresAt) {
            this.externalUserId = externalUserId;
            this.accessToken = accessToken;
        }

        @Override
        public void revoke(UUID connectionId) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        var fixture = Files.readString(Path.of("../../../docs/fixtures/withings/token-response-synthetic.json"));
        wireMock.stubFor(post(urlPathEqualTo("/v2/oauth2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(fixture)));

        var properties = new WithingsProperties();
        properties.setBaseUrl("http://localhost:" + wireMock.port());
        properties.setAuthorizeUrl("https://vendor.example/authorize");
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        properties.setCallbackSecret("test-callback-secret");
        properties.setRedirectUri("http://localhost:8080/callback");
        connectionStore = new FakeConnectionStore();
        credentialsPort = new FakeCredentialsPort();
        stateCodec = new WithingsStateCodec("test-callback-secret");
        settingsPort = new FakeSettingsPort();
        adapter = new WithingsOAuthAdapter(properties, JsonMapper.builder().build(),
                connectionStore, credentialsPort, settingsPort, Clock.fixed(Instant.parse("2026-03-30T12:00:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void authorizeReturnsVendorUrlWithSignedState() {
        AuthorizationInit init = adapter.startAuthorization();

        assertTrue(init.authorizationUrl().startsWith("https://vendor.example/authorize?"));
        assertTrue(init.authorizationUrl().contains("client_id=test-client-id"));
        assertTrue(init.authorizationUrl().contains("state=" + init.state()));
        assertEquals(1, connectionStore.saved.size());
    }

    @Test
    void callbackExchangesCodeAndStoresCredentials() {
        var init = adapter.startAuthorization();
        var connection = adapter.handleCallback("synthetic-code", init.state());

        assertEquals(connectionStore.saved.get(0).id(), connection.id());
        assertEquals("12345678", credentialsPort.externalUserId);
        assertEquals("synthetic-access-token", credentialsPort.accessToken);
    }

    @Test
    void callbackRejectsTamperedState() {
        var init = adapter.startAuthorization();
        var tampered = init.state().substring(0, init.state().length() - 2) + "ff";
        assertThrows(IllegalArgumentException.class, () -> adapter.handleCallback("synthetic-code", tampered));
    }

    @Test
    void stateCodecRoundTrips() {
        var id = UUID.randomUUID();
        assertEquals(id, stateCodec.verify(stateCodec.issue(id)));
    }

    @Test
    void userSuppliedCredentialsOverrideEnvironment() {
        settingsPort.save("withings", java.util.Map.of("clientId", "user-client-id", "clientSecret", "user-secret"));

        AuthorizationInit init = adapter.startAuthorization();

        org.junit.jupiter.api.Assertions.assertTrue(init.authorizationUrl().contains("client_id=user-client-id"),
                "authorize URL must use the user-supplied client id");
    }

    @Test
    void missingCredentialsFailWithClearMessage() {
        var emptyProps = new WithingsProperties();
        emptyProps.setBaseUrl("http://localhost:" + wireMock.port());
        emptyProps.setAuthorizeUrl("https://vendor.example/authorize");
        emptyProps.setCallbackSecret("test-callback-secret");
        emptyProps.setRedirectUri("http://localhost:8080/callback");
        var noCreds = new WithingsOAuthAdapter(emptyProps, JsonMapper.builder().build(),
                connectionStore, credentialsPort, new FakeSettingsPort(), Clock.fixed(Instant.parse("2026-03-30T12:00:00Z"), ZoneOffset.UTC));

        var ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, noCreds::startAuthorization);
        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("clientId"));
    }
}
