package dev.healthhub.withings;

import dev.healthhub.ingestion.AuthorizationInit;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.ProviderOAuthPort;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "healthhub.withings", name = "enabled")
public class WithingsOAuthAdapter implements ProviderOAuthPort {

    private final WithingsProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ConnectionStore connectionStore;
    private final ConnectionCredentialsPort credentialsPort;
    private final WithingsStateCodec stateCodec;
    private final Clock clock;

    public WithingsOAuthAdapter(WithingsProperties properties, ObjectMapper objectMapper,
            ConnectionStore connectionStore, ConnectionCredentialsPort credentialsPort, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.connectionStore = connectionStore;
        this.credentialsPort = credentialsPort;
        this.clock = clock;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
        this.stateCodec = new WithingsStateCodec(properties.getCallbackSecret());
    }

    @Override
    public String provider() {
        return "withings";
    }

    @Override
    public AuthorizationInit startAuthorization() {
        var connection = connectionStore.saveNew(provider(), "Withings", Instant.now(clock));
        var state = stateCodec.issue(connection.id());
        var url = URI.create(properties.getAuthorizeUrl()
                + "?response_type=code"
                + "&client_id=" + properties.getClientId()
                + "&state=" + state
                + "&scope=" + properties.getScope()
                + "&redirect_uri=" + properties.getRedirectUri());
        return new AuthorizationInit(url.toString(), state);
    }

    @Override
    public ConnectionInfo handleCallback(String code, String state) {
        var connectionId = stateCodec.verify(state);
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
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

    private void assertStatusOk(JsonNode response) {
        if (response == null || response.path("status").asInt(-1) != 0) {
            throw new IllegalStateException("vendor token exchange failed: " + response);
        }
    }
}
