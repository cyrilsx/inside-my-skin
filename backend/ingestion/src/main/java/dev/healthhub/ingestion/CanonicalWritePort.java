package dev.healthhub.ingestion;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.UsageDailyRecord;
import java.util.UUID;

public interface CanonicalWritePort {

    void upsertMeasurement(Measurement measurement);

    void upsertActivity(UUID connectionId, String providerRecordId, ActivityRecord record);

    void upsertMentalSession(UUID connectionId, String providerRecordId, MentalSessionRecord record);

    void upsertUsageDaily(UUID connectionId, String providerRecordId, UsageDailyRecord record);

    void upsertMoodCheckin(UUID connectionId, String providerRecordId, MoodCheckinRecord record);
}
