package dev.healthhub.app.persistence;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.ReadModelPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReadModelPortJpa implements ReadModelPort {

    private final MeasurementRepository measurementRepository;
    private final ActivityRepository activityRepository;
    private final MentalSessionRepository mentalSessionRepository;
    private final UsageDailyRepository usageDailyRepository;
    private final MoodCheckinRepository moodCheckinRepository;
    private final ObjectMapper objectMapper;

    public ReadModelPortJpa(MeasurementRepository measurementRepository, ActivityRepository activityRepository,
            MentalSessionRepository mentalSessionRepository, UsageDailyRepository usageDailyRepository,
            MoodCheckinRepository moodCheckinRepository, ObjectMapper objectMapper) {
        this.measurementRepository = measurementRepository;
        this.activityRepository = activityRepository;
        this.mentalSessionRepository = mentalSessionRepository;
        this.usageDailyRepository = usageDailyRepository;
        this.moodCheckinRepository = moodCheckinRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Measurement> measurements(MetricType type, Instant from, Instant to) {
        return measurementRepository.findByMetricTypeAndOccurredAtBetweenOrderByOccurredAt(type, from, to)
                .stream()
                .map(e -> new Measurement(e.getConnectionId(), e.getProviderRecordId(), e.getSource(),
                        e.getMetricType(), e.getValue(), e.getOccurredAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getReceivedAt()))
                .toList();
    }

    @Override
    public List<ActivityRecord> activities(Instant from, Instant to) {
        return activityRepository.findInRange(from, to).stream()
                .map(e -> new ActivityRecord(e.getId(), e.getSource(), e.getType(), e.getTitle(), e.getStartedAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getDurationS(), e.getDistanceM(),
                        e.getAvgHeartRateBpm()))
                .toList();
    }

    @Override
    public List<MentalSessionRecord> mentalSessions(Instant from, Instant to) {
        return mentalSessionRepository.findInRange(from, to).stream()
                .map(e -> new MentalSessionRecord(e.getId(), e.getSource(), e.getKind(), e.getStartedAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getDurationS()))
                .toList();
    }

    @Override
    public List<UsageDailyRecord> usageDaily(LocalDate from, LocalDate to) {
        return usageDailyRepository.findInRange(from, to).stream()
                .map(e -> {
                    List<UsageDailyRecord.PerApp> perApp = new java.util.ArrayList<>();
                    for (JsonNode n : objectMapper.readTree(e.getPerApp())) {
                        perApp.add(new UsageDailyRecord.PerApp(n.get("app").asText(), n.get("foregroundS").asLong()));
                    }
                    return new UsageDailyRecord(e.getDate(), e.getTotalScreenTimeS(), e.isEstimate(), perApp);
                })
                .toList();
    }

    @Override
    public List<MoodCheckinRecord> moodCheckins(Instant from, Instant to) {
        return moodCheckinRepository.findInRange(from, to).stream()
                .map(e -> {
                    JsonNode labels = objectMapper.readTree(e.getLabels());
                    List<String> labelList = new java.util.ArrayList<>();
                    labels.forEach(n -> labelList.add(n.asText()));
                    return new MoodCheckinRecord(e.getId(), e.getOccurredAt(),
                            ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getValence(), labelList, e.getNote());
                })
                .toList();
    }
}
