package dev.healthhub.withings;

import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ProviderRecord;
import dev.healthhub.ingestion.ProviderSyncPort;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "healthhub.withings", name = "enabled")
public class WithingsMeasureAdapter implements ProviderSyncPort {

    private final WithingsProperties properties;
    private final RestClient restClient;
    private final WithingsMeasureParser parser;
    private final ConnectionCredentialsPort credentialsPort;
    private final Clock clock;

    public WithingsMeasureAdapter(WithingsProperties properties, ObjectMapper objectMapper,
            ConnectionCredentialsPort credentialsPort, Clock clock) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
        this.parser = new WithingsMeasureParser(objectMapper);
        this.credentialsPort = credentialsPort;
        this.clock = clock;
    }

    @Override
    public String provider() {
        return "withings";
    }

    @Override
    public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
        var credentials = credentialsPort.credentialsFor(connectionId)
                .orElseThrow(() -> new IllegalStateException("no credentials for connection " + connectionId));
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/measure")
                        .queryParam("action", "getmeas")
                        .queryParam("userid", credentials.externalUserId())
                        .queryParam("startdate", since.getEpochSecond())
                        .queryParam("enddate", Instant.now(clock).getEpochSecond())
                        .build())
                .header("Authorization", "Bearer " + credentials.accessToken())
                .retrieve()
                .body(JsonNode.class);
        if (response == null || response.path("status").asInt(-1) != 0) {
            throw new IllegalStateException("vendor getmeas failed: " + response);
        }
        return parser.parse(response);
    }
}
