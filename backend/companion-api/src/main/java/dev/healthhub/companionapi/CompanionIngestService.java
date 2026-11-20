package dev.healthhub.companionapi;

import dev.healthhub.companionapi.IngestDtos.IngestBatchDto;
import dev.healthhub.companionapi.IngestDtos.IngestItemDto;
import dev.healthhub.companionapi.IngestDtos.IngestItemResultDto;
import dev.healthhub.companionapi.IngestDtos.IngestResultDto;
import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.ActivityType;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionKind;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.Source;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.ConnectionStore;
import dev.healthhub.ingestion.IngestOutcome;
import dev.healthhub.ingestion.IngestionService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class CompanionIngestService {

    private static final String COMPANION_PROVIDER = "health_connect";

    private final ConnectionStore connectionStore;
    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public CompanionIngestService(ConnectionStore connectionStore, IngestionService ingestionService,
            ObjectMapper objectMapper, Clock clock) {
        this.connectionStore = connectionStore;
        this.ingestionService = ingestionService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public IngestResultDto ingest(IngestBatchDto batch) {
        var connection = connectionStore.list().stream()
                .filter(c -> COMPANION_PROVIDER.equals(c.provider()))
                .findFirst()
                .orElseGet(() -> connectionStore.saveNew(COMPANION_PROVIDER, "Companion app", Instant.now(clock)));
        var results = new ArrayList<IngestItemResultDto>();
        int accepted = 0;
        int duplicates = 0;
        int rejected = 0;
        for (var item : batch.items()) {
            try {
                var outcome = ingestItem(connection.id(), item);
                results.add(new IngestItemResultDto(item.clientId(),
                        outcome == IngestOutcome.DUPLICATE ? "DUPLICATE" : "ACCEPTED", null));
                if (outcome == IngestOutcome.DUPLICATE) {
                    duplicates++;
                } else {
                    accepted++;
                }
            } catch (Exception e) {
                rejected++;
                results.add(new IngestItemResultDto(item.clientId(), "REJECTED", e.getMessage()));
            }
        }
        return new IngestResultDto(accepted, duplicates, rejected, results);
    }

    private IngestOutcome ingestItem(UUID connectionId, IngestItemDto item) {
        var raw = objectMapper.writeValueAsString(item);
        return switch (item.kind()) {
            case "MEASUREMENT" -> ingestionService.ingestMeasurement(
                    connectionId, item.clientId().toString(), Source.HEALTH_CONNECT,
                    MetricType.valueOf(item.metricType()), item.value(),
                    item.occurredAt().toInstant(), ZoneOffset.of(item.zoneOffset()), raw).outcome();
            case "ACTIVITY" -> ingestionService.ingestActivity(connectionId, item.clientId().toString(),
                    new ActivityRecord(UUID.randomUUID(), Source.HEALTH_CONNECT, ActivityType.valueOf(item.type()),
                            item.title(), item.startedAt().toInstant(), ZoneOffset.of(item.zoneOffset()),
                            item.durationS(), item.distanceM(), null), raw).outcome();
            case "MENTAL_SESSION" -> ingestionService.ingestMentalSession(connectionId, item.clientId().toString(),
                    new MentalSessionRecord(UUID.randomUUID(), Source.HEALTH_CONNECT,
                            MentalSessionKind.valueOf(item.sessionKind()),
                            item.startedAt().toInstant(), ZoneOffset.of(item.zoneOffset()), item.durationS()), raw).outcome();
            case "USAGE_DAILY" -> ingestionService.ingestUsageDaily(connectionId, item.clientId().toString(),
                    new UsageDailyRecord(item.date(), item.totalScreenTimeS(), true,
                            item.perApp().stream()
                                    .map(p -> new UsageDailyRecord.PerApp(p.app(), p.foregroundS()))
                                    .toList()), raw).outcome();
            case "MOOD_CHECKIN" -> ingestionService.ingestMoodCheckin(connectionId, item.clientId().toString(),
                    new MoodCheckinRecord(UUID.randomUUID(), item.occurredAt().toInstant(),
                            item.occurredAt().getOffset(), item.valence(), item.labels(), item.note()), raw).outcome();
            default -> throw new IllegalArgumentException("unknown kind: " + item.kind());
        };
    }
}
