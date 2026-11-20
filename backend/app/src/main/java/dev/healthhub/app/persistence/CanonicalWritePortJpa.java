package dev.healthhub.app.persistence;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.CanonicalWritePort;
import dev.healthhub.ingestion.MeasurementStore;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class CanonicalWritePortJpa implements CanonicalWritePort {

    private final MeasurementStore measurementStore;
    private final ActivityRepository activityRepository;
    private final MentalSessionRepository mentalSessionRepository;
    private final UsageDailyRepository usageDailyRepository;
    private final MoodCheckinRepository moodCheckinRepository;
    private final ObjectMapper objectMapper;

    public CanonicalWritePortJpa(MeasurementStore measurementStore, ActivityRepository activityRepository,
            MentalSessionRepository mentalSessionRepository, UsageDailyRepository usageDailyRepository,
            MoodCheckinRepository moodCheckinRepository, ObjectMapper objectMapper) {
        this.measurementStore = measurementStore;
        this.activityRepository = activityRepository;
        this.mentalSessionRepository = mentalSessionRepository;
        this.usageDailyRepository = usageDailyRepository;
        this.moodCheckinRepository = moodCheckinRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void upsertMeasurement(Measurement measurement) {
        measurementStore.upsert(measurement);
    }

    @Override
    @Transactional
    public void upsertActivity(UUID connectionId, String providerRecordId, ActivityRecord record) {
        activityRepository.findByConnectionIdAndProviderRecordId(connectionId, providerRecordId)
                .ifPresentOrElse(existing -> {
                    existing.setTitle(record.title());
                    activityRepository.save(existing);
                }, () -> activityRepository.save(new ActivityEntity(
                        connectionId, providerRecordId, record.source(), record.type(), record.title(),
                        record.startedAt(), record.zoneOffsetAtCapture().getId(), record.durationS(),
                        record.distanceM(), record.avgHeartRateBpm())));
    }

    @Override
    @Transactional
    public void upsertMentalSession(UUID connectionId, String providerRecordId, MentalSessionRecord record) {
        mentalSessionRepository.save(new MentalSessionEntity(
                connectionId, providerRecordId, record.source(), record.kind(), record.startedAt(),
                record.zoneOffsetAtCapture().getId(), record.durationS()));
    }

    @Override
    @Transactional
    public void upsertUsageDaily(UUID connectionId, String providerRecordId, UsageDailyRecord record) {
        usageDailyRepository.save(new UsageDailyEntity(
                connectionId, providerRecordId, record.date(), record.totalScreenTimeS(), record.isEstimate(),
                writeJson(record.perApp())));
    }

    @Override
    @Transactional
    public void upsertMoodCheckin(UUID connectionId, String providerRecordId, MoodCheckinRecord record) {
        moodCheckinRepository.save(new MoodCheckinEntity(
                connectionId, providerRecordId, record.occurredAt(), record.zoneOffsetAtCapture().getId(),
                record.valence(), writeJson(record.labels()), record.note()));
    }

    @Override
    @Transactional
    public void deleteAll() {
        measurementStore.deleteAll();
        activityRepository.deleteAll();
        mentalSessionRepository.deleteAll();
        usageDailyRepository.deleteAll();
        moodCheckinRepository.deleteAll();
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
