package dev.healthhub.withings;

import dev.healthhub.ingestion.AuthorizationInit;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.ProviderOAuthPort;
import dev.healthhub.ingestion.ProviderSettingsPort;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class WithingsOAuthAdapter implements ProviderOAuthPort {

    private final WithingsProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ConnectionStore connectionStore;
    private final ConnectionCredentialsPort credentialsPort;
    private final ProviderSettingsPort settingsPort;
    private final Clock clock;

    public WithingsOAuthAdapter(WithingsProperties properties, ObjectMapper objectMapper,
            ConnectionStore connectionStore, ConnectionCredentialsPort credentialsPort,
            ProviderSettingsPort settingsPort, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.connectionStore = connectionStore;
        this.credentialsPort = credentialsPort;
        this.settingsPort = settingsPort;
        this.clock = clock;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    // created lazily: the callback secret may come from the environment and is only
    // required when an authorization flow actually runs
    private WithingsStateCodec stateCodec() {
        return new WithingsStateCodec(properties.getCallbackSecret());
    }

    @Override
    public String provider() {
        return "withings";
    }

    @Override
    public AuthorizationInit startAuthorization() {
        var connection = connectionStore.saveNew(provider(), "Withings", Instant.now(clock));
        var state = stateCodec().issue(connection.id());
        var url = URI.create(properties.getAuthorizeUrl()
                + "?response_type=code"
                + "&client_id=" + clientId()
                + "&state=" + state
                + "&scope=" + properties.getScope()
                + "&redirect_uri=" + properties.getRedirectUri());
        return new AuthorizationInit(url.toString(), state);
    }

    @Override
    public ConnectionInfo handleCallback(String code, String state) {
        var connectionId = stateCodec().verify(state);
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId());
        body.add("client_secret", clientSecret());
        body.add("code", code);
        body.add("redirect_uri", properties.getRedirectUri());
        var response = restClient.post()
                .uri("/v2/oauth2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        assertStatusOk(response);
        var tokenBody = response.get("body");
        var expiresAt = Instant.now(clock).plusSeconds(tokenBody.get("expires_in").asLong());
        credentialsPort.save(connectionId,
                tokenBody.get("userid").asText(),
                tokenBody.get("access_token").asText(),
                tokenBody.get("refresh_token").asText(),
                expiresAt);
        return connectionStore.find(connectionId)
                .orElseThrow(() -> new IllegalStateException("connection vanished: " + connectionId));
    }

    /** User-supplied credentials (encrypted at rest) win over environment configuration. */
    private Map<String, String> userCredentials() {
        return settingsPort.find(provider()).orElse(Map.of());
    }

    private String clientId() {
        var value = userCredentials().get("clientId");
        if (value == null || value.isBlank()) {
            value = properties.getClientId();
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("withings clientId is not configured: save it via PUT /api/v1/providers/withings/credentials or set healthhub.withings.client-id");
        }
        return value;
    }

    private String clientSecret() {
        var value = userCredentials().get("clientSecret");
        if (value == null || value.isBlank()) {
            value = properties.getClientSecret();
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("withings clientSecret is not configured: save it via PUT /api/v1/providers/withings/credentials or set healthhub.withings.client-secret");
        }
        return value;
    }

    private void assertStatusOk(JsonNode response) {
        if (response == null || response.path("status").asInt(-1) != 0) {
            throw new IllegalStateException("vendor token exchange failed: " + response);
        }
    }
}
