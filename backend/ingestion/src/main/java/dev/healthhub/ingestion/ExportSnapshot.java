package dev.healthhub.ingestion;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.UsageDailyRecord;
import java.util.List;

public record ExportSnapshot(
        List<Measurement> measurements,
        List<ActivityRecord> activities,
        List<MentalSessionRecord> mentalSessions,
        List<UsageDailyRecord> usageDaily,
        List<MoodCheckinRecord> moodCheckins,
        List<ConnectionInfo> connections) {

    public ExportSnapshot {
        measurements = measurements == null ? List.of() : List.copyOf(measurements);
        activities = activities == null ? List.of() : List.copyOf(activities);
        mentalSessions = mentalSessions == null ? List.of() : List.copyOf(mentalSessions);
        usageDaily = usageDaily == null ? List.of() : List.copyOf(usageDaily);
        moodCheckins = moodCheckins == null ? List.of() : List.copyOf(moodCheckins);
        connections = connections == null ? List.of() : List.copyOf(connections);
    }
}
