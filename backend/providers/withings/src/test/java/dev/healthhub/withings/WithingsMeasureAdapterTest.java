package dev.healthhub.withings;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ProviderCredentials;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class WithingsMeasureAdapterTest {

    private static final UUID CONNECTION = UUID.randomUUID();

    private WireMockServer wireMock;
    private WithingsMeasureAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        var fixture = Files.readString(Path.of("../../../docs/fixtures/withings/measure-getmeas-synthetic.json"));
        wireMock.stubFor(get(urlPathEqualTo("/v2/measure"))
                .withQueryParam("action", equalTo("getmeas"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(fixture)));

        var properties = new WithingsProperties();
        properties.setBaseUrl("http://localhost:" + wireMock.port());
        ConnectionCredentialsPort credentialsPort = new ConnectionCredentialsPort() {
            @Override
            public Optional<ProviderCredentials> credentialsFor(UUID connectionId) {
                return Optional.of(new ProviderCredentials("12345678", "synthetic-access-token",
                        "synthetic-refresh-token", Instant.now().plusSeconds(10800)));
            }

            @Override
            public void save(UUID connectionId, String externalUserId, String accessToken, String refreshToken,
                    Instant expiresAt) {
            }

            @Override
            public void revoke(UUID connectionId) {
            }
        };
        adapter = new WithingsMeasureAdapter(properties, JsonMapper.builder().build(),
                credentialsPort, Clock.fixed(Instant.parse("2026-03-30T12:00:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void parsesFixtureIntoCanonicalRecords() {
        var records = adapter.fetch(CONNECTION, Instant.EPOCH);

        assertEquals(5, records.size());
        var weight = records.stream()
                .filter(r -> r.metricType() == MetricType.WEIGHT_KG && r.providerRecordId().equals("900000001:1"))
                .findFirst().orElseThrow();
        assertEquals(72.5, weight.value(), 1e-9);
        assertEquals(Source.WITHINGS, weight.source());
        assertEquals(Instant.ofEpochSecond(1774681200L), weight.occurredAt());
        assertEquals(ZoneOffset.ofHours(2), weight.zoneOffsetAtCapture());
        assertTrue(weight.rawPayload().contains("900000001"));
    }

    @Test
    void convertsBloodPressureToPascals() {
        var records = adapter.fetch(CONNECTION, Instant.EPOCH);

        var systolic = records.stream()
                .filter(r -> r.metricType() == MetricType.SYSTOLIC_BP_PA)
                .findFirst().orElseThrow();
        assertEquals(118 * 133.322, systolic.value(), 1e-6);
        var heartRate = records.stream()
                .filter(r -> r.metricType() == MetricType.HEART_RATE_BPM)
                .findFirst().orElseThrow();
        assertEquals(58.0, heartRate.value(), 1e-9);
    }
}
