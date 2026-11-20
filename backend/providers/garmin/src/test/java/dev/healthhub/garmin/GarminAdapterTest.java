package dev.healthhub.garmin;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class GarminAdapterTest {

    private WireMockServer wireMock;
    private GarminAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        stub("/modern/proxy/usersummary-service/usersummary/daily/u1/2026-03-29",
                "../../../docs/fixtures/garmin/daily-summary-synthetic.json");
        stub("/modern/proxy/sleep-service/sleep/daily/u1/2026-03-29",
                "../../../docs/fixtures/garmin/sleep-daily-synthetic.json");
        stub("/modern/proxy/hrv-service/hrv/daily/u1/2026-03-29",
                "../../../docs/fixtures/garmin/hrv-daily-synthetic.json");

        var properties = new GarminProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://localhost:" + wireMock.port());
        properties.setUserId("u1");
        properties.setKeyHeader("X-Test-Key");
        properties.setPersonalKey("test-personal-key");
        adapter = new GarminAdapter(properties, JsonMapper.builder().build(),
                Clock.fixed(Instant.parse("2026-03-29T20:00:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    private void stub(String path, String fixture) throws Exception {
        wireMock.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(Files.readString(Path.of(fixture)))));
    }

    @Test
    void mapsDailyFixturesToCanonicalRecords() {
        var records = adapter.fetch(UUID.randomUUID(), Instant.parse("2026-03-29T00:00:00Z"));

        assertEquals(4, records.size());
        var steps = records.stream().filter(r -> r.metricType() == MetricType.STEPS).findFirst().orElseThrow();
        assertEquals(8421.0, steps.value(), 1e-9);
        assertEquals(Source.GARMIN, steps.source());
        assertEquals("daily:2026-03-29:steps", steps.providerRecordId());
        assertEquals(ZoneOffset.UTC, steps.zoneOffsetAtCapture());

        var restingHr = records.stream()
                .filter(r -> r.metricType() == MetricType.RESTING_HEART_RATE_BPM).findFirst().orElseThrow();
        assertEquals(52.0, restingHr.value(), 1e-9);

        var sleep = records.stream().filter(r -> r.metricType() == MetricType.SLEEP_SECONDS).findFirst().orElseThrow();
        assertEquals(27000.0, sleep.value(), 1e-9);
        assertEquals(Instant.ofEpochSecond(1774672800L), sleep.occurredAt());

        var hrv = records.stream().filter(r -> r.metricType() == MetricType.HRV_RMSSD_S).findFirst().orElseThrow();
        assertEquals(0.045, hrv.value(), 1e-9);
    }

    @Test
    void sendsConfiguredKeyHeader() {
        adapter.fetch(UUID.randomUUID(), Instant.parse("2026-03-29T00:00:00Z"));
        var requests = wireMock.findAll(getRequestedFor(urlPathEqualTo(
                "/modern/proxy/usersummary-service/usersummary/daily/u1/2026-03-29")));
        assertTrue(requests.stream().allMatch(r -> "test-personal-key".equals(r.getHeader("X-Test-Key"))));
    }

    @Test
    void futureSinceReturnsNothing() {
        var records = adapter.fetch(UUID.randomUUID(), Instant.parse("2026-03-30T00:00:00Z"));
        assertTrue(records.isEmpty());
    }
}
