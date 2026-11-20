package dev.healthhub.app.persistence;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ExportPort;
import dev.healthhub.ingestion.ExportSnapshot;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ExportPortJpa implements ExportPort {

    private final MeasurementRepository measurementRepository;
    private final ActivityRepository activityRepository;
    private final MentalSessionRepository mentalSessionRepository;
    private final UsageDailyRepository usageDailyRepository;
    private final MoodCheckinRepository moodCheckinRepository;
    private final ConnectionRepository connectionRepository;
    private final ObjectMapper objectMapper;

    public ExportPortJpa(MeasurementRepository measurementRepository, ActivityRepository activityRepository,
            MentalSessionRepository mentalSessionRepository, UsageDailyRepository usageDailyRepository,
            MoodCheckinRepository moodCheckinRepository, ConnectionRepository connectionRepository,
            ObjectMapper objectMapper) {
        this.measurementRepository = measurementRepository;
        this.activityRepository = activityRepository;
        this.mentalSessionRepository = mentalSessionRepository;
        this.usageDailyRepository = usageDailyRepository;
        this.moodCheckinRepository = moodCheckinRepository;
        this.connectionRepository = connectionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExportSnapshot exportAll() {
        var measurements = measurementRepository.findAllByOrderByOccurredAtAsc().stream()
                .map(e -> new Measurement(e.getConnectionId(), e.getProviderRecordId(), e.getSource(),
                        e.getMetricType(), e.getValue(), e.getOccurredAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getReceivedAt()))
                .toList();
        var activities = activityRepository.findAllByOrderByStartedAtAsc().stream()
                .map(e -> new ActivityRecord(e.getId(), e.getSource(), e.getType(), e.getTitle(), e.getStartedAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getDurationS(), e.getDistanceM(),
                        e.getAvgHeartRateBpm()))
                .toList();
        var mentalSessions = mentalSessionRepository.findAllByOrderByStartedAtAsc().stream()
                .map(e -> new MentalSessionRecord(e.getId(), e.getSource(), e.getKind(), e.getStartedAt(),
                        ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getDurationS()))
                .toList();
        var usageDaily = usageDailyRepository.findAllByOrderByDateAsc().stream()
                .map(e -> {
                    List<UsageDailyRecord.PerApp> perApp = new ArrayList<>();
                    for (JsonNode n : objectMapper.readTree(e.getPerApp())) {
                        perApp.add(new UsageDailyRecord.PerApp(n.get("app").asText(), n.get("foregroundS").asLong()));
                    }
                    return new UsageDailyRecord(e.getDate(), e.getTotalScreenTimeS(), e.isEstimate(), perApp);
                })
                .toList();
        var moodCheckins = moodCheckinRepository.findAllByOrderByOccurredAtAsc().stream()
                .map(e -> {
                    List<String> labels = new ArrayList<>();
                    for (JsonNode n : objectMapper.readTree(e.getLabels())) {
                        labels.add(n.asText());
                    }
                    return new MoodCheckinRecord(e.getId(), e.getOccurredAt(),
                            ZoneOffset.of(e.getZoneOffsetAtCapture()), e.getValence(), labels, e.getNote());
                })
                .toList();
        var connections = connectionRepository.findAll().stream()
                .map(e -> new ConnectionInfo(e.getId(), e.getProvider(), e.getLabel(),
                        e.getCreatedAt(), e.getLastSyncAt(), e.getLastSyncStatus()))
                .toList();
        return new ExportSnapshot(measurements, activities, mentalSessions, usageDaily, moodCheckins, connections);
    }
}
