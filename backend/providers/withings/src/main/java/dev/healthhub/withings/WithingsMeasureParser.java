package dev.healthhub.withings;

import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ProviderRecord;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class WithingsMeasureParser {

    private static final double MMHG_TO_PA = 133.322;

    private final ObjectMapper objectMapper;

    WithingsMeasureParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    List<ProviderRecord> parse(JsonNode getmeasResponse) {
        var records = new ArrayList<ProviderRecord>();
        var groups = getmeasResponse.path("body").path("measuregrps");
        for (var group : groups) {
            var grpid = group.path("grpid").asLong();
            var occurredAt = Instant.ofEpochSecond(group.path("date").asLong());
            var zoneOffset = ZoneOffset.ofTotalSeconds(Math.toIntExact(group.path("timezone").asLong()));
            var rawPayload = write(group);
            for (var measure : group.path("measures")) {
                var type = measure.path("type").asInt();
                var raw = measure.path("value").asDouble() * Math.pow(10, measure.path("unit").asInt());
                var mapped = map(type, raw);
                if (mapped != null) {
                    records.add(new ProviderRecord(
                            grpid + ":" + type, Source.WITHINGS, mapped.metricType(), mapped.value(),
                            occurredAt, zoneOffset, rawPayload));
                }
            }
        }
        return records;
    }

    private record Mapped(MetricType metricType, double value) {
    }

    private Mapped map(int vendorMeasureType, double rawValue) {
        return switch (vendorMeasureType) {
            case 1 -> new Mapped(MetricType.WEIGHT_KG, rawValue);
            case 5 -> new Mapped(MetricType.SYSTOLIC_BP_PA, rawValue * MMHG_TO_PA);
            case 6 -> new Mapped(MetricType.DIASTOLIC_BP_PA, rawValue * MMHG_TO_PA);
            case 8 -> new Mapped(MetricType.HEART_RATE_BPM, rawValue);
            default -> null;
        };
    }

    private String write(JsonNode node) {
        return objectMapper.writeValueAsString(node);
    }
}
