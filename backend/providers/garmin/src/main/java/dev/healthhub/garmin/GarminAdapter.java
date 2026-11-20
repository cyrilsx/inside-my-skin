package dev.healthhub.garmin;

import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ProviderRecord;
import dev.healthhub.ingestion.ProviderSyncPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "healthhub.garmin", name = "enabled")
public class GarminAdapter implements ProviderSyncPort {

    private static final int MAX_LOOKBACK_DAYS = 31;

    private final GarminProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public GarminAdapter(GarminProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
        var builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        this.restClient = builder.build();
    }

    @Override
    public String provider() {
        return "garmin";
    }

    @Override
    public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
        var records = new ArrayList<ProviderRecord>();
        var today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        var from = since.atZone(ZoneOffset.UTC).toLocalDate();
        if (from.isAfter(today)) {
            return records;
        }
        var start = from.isBefore(today.minusDays(MAX_LOOKBACK_DAYS)) ? today.minusDays(MAX_LOOKBACK_DAYS) : from;
        for (var date = start; !date.isAfter(today); date = date.plusDays(1)) {
            records.addAll(fetchDay(date));
        }
        return records;
    }

    private List<ProviderRecord> fetchDay(LocalDate date) {
        var records = new ArrayList<ProviderRecord>();
        var summary = get("/modern/proxy/usersummary-service/usersummary/daily/" + properties.getUserId() + "/" + date);
        if (summary != null) {
            var anchor = anchor(date);
            if (summary.has("totalSteps")) {
                records.add(record("daily:" + date + ":steps", MetricType.STEPS, summary.get("totalSteps").asDouble(), anchor, summary));
            }
            if (summary.has("restingHeartRate")) {
                records.add(record("daily:" + date + ":resting-hr", MetricType.RESTING_HEART_RATE_BPM,
                        summary.get("restingHeartRate").asDouble(), anchor, summary));
            }
        }
        var sleep = get("/modern/proxy/sleep-service/sleep/daily/" + properties.getUserId() + "/" + date);
        if (sleep != null && sleep.has("dailySleepDTO")) {
            var dto = sleep.get("dailySleepDTO");
            if (dto.has("sleepTimeSeconds")) {
                var startedAt = Instant.ofEpochSecond(dto.path("sleepStartTimestampGMT").asLong());
                records.add(record("sleep:" + date, MetricType.SLEEP_SECONDS, dto.get("sleepTimeSeconds").asDouble(),
                        startedAt, dto));
            }
        }
        var hrv = get("/modern/proxy/hrv-service/hrv/daily/" + properties.getUserId() + "/" + date);
        if (hrv != null && hrv.has("hrvSummary")) {
            var lastHrv = hrv.get("hrvSummary").path("lastHrv");
            if (lastHrv.has("hrvValue")) {
                var ms = lastHrv.get("hrvValue").asDouble();
                var occurredAt = Instant.ofEpochSecond(lastHrv.path("startTimeGMT").asLong());
                records.add(record("hrv:" + date, MetricType.HRV_RMSSD_S, ms / 1000.0, occurredAt, hrv));
            }
        }
        return records;
    }

    private static Instant anchor(LocalDate date) {
        return date.atTime(12, 0).toInstant(ZoneOffset.UTC);
    }

    private ProviderRecord record(String id, MetricType type, double value, Instant occurredAt, JsonNode raw) {
        return new ProviderRecord(id, Source.GARMIN, type, value, occurredAt, ZoneOffset.UTC,
                objectMapper.writeValueAsString(raw));
    }

    private JsonNode get(String path) {
        var spec = restClient.get().uri(path);
        if (!properties.getKeyHeader().isBlank() && !properties.getPersonalKey().isBlank()) {
            spec = spec.header(properties.getKeyHeader(), properties.getPersonalKey());
        }
        return spec.retrieve().body(JsonNode.class);
    }
}
