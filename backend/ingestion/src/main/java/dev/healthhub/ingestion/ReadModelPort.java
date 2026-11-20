package dev.healthhub.ingestion;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.UsageDailyRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ReadModelPort {

    List<Measurement> measurements(MetricType type, Instant from, Instant to);

    List<ActivityRecord> activities(Instant from, Instant to);

    List<MentalSessionRecord> mentalSessions(Instant from, Instant to);

    List<UsageDailyRecord> usageDaily(LocalDate from, LocalDate to);

    List<MoodCheckinRecord> moodCheckins(Instant from, Instant to);
}
