package dev.healthhub.insights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.ActivityType;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.MoodCheckinRecord;
import dev.healthhub.core.Source;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.ReadModelPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyRollupServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T23:59:59Z");

    private static ActivityRecord activity(String id, Instant startedAt, ZoneOffset offset, long durationS, Double distanceM) {
        return new ActivityRecord(UUID.randomUUID(), Source.HEALTH_CONNECT, ActivityType.RUNNING, id,
                startedAt, offset, durationS, distanceM, null);
    }

    private static ReadModelPort fakeReadModel(List<ActivityRecord> activities) {
        return new ReadModelPort() {
            @Override
            public List<Measurement> measurements(MetricType type, Instant from, Instant to) {
                return List.of();
            }

            @Override
            public List<ActivityRecord> activities(Instant from, Instant to) {
                return activities;
            }

            @Override
            public List<MentalSessionRecord> mentalSessions(Instant from, Instant to) {
                return List.of();
            }

            @Override
            public List<UsageDailyRecord> usageDaily(LocalDate from, LocalDate to) {
                return List.of();
            }

            @Override
            public List<MoodCheckinRecord> moodCheckins(Instant from, Instant to) {
                return List.of();
            }
        };
    }

    @Test
    void aggregatesDurationAndDistancePerIsoWeek() {
        var service = new WeeklyRollupService(fakeReadModel(List.of(
                activity("a", Instant.parse("2026-03-23T06:00:00Z"), ZoneOffset.ofHours(1), 1800, 5000.0),
                activity("b", Instant.parse("2026-03-25T06:00:00Z"), ZoneOffset.ofHours(1), 2700, 7500.0),
                activity("c", Instant.parse("2026-03-30T06:00:00Z"), ZoneOffset.ofHours(2), 900, null))));

        var weeks = service.weeks(FROM, TO);

        assertEquals(2, weeks.size());
        var week1 = weeks.get(0);
        assertEquals(LocalDate.parse("2026-03-23"), week1.weekStart());
        assertEquals(2, week1.sessionCount());
        assertEquals(4500, week1.totalDurationS());
        assertEquals(12500.0, week1.totalDistanceM());
        var week2 = weeks.get(1);
        assertEquals(LocalDate.parse("2026-03-30"), week2.weekStart());
        assertEquals(1, week2.sessionCount());
        assertEquals(900, week2.totalDurationS());
        assertNull(week2.totalDistanceM());
    }

    @Test
    void springForwardTransitionDaySplitsByCapturedOffset() {
        // 2026-03-29: Europe springs forward, 02:00+01:00 becomes 03:00+02:00.
        // Same UTC day, but the late record is already on Monday 2026-03-30 local time.
        var service = new WeeklyRollupService(fakeReadModel(List.of(
                activity("sunday-evening", Instant.parse("2026-03-29T21:30:00Z"), ZoneOffset.ofHours(2), 600, null),
                activity("monday-morning", Instant.parse("2026-03-29T22:30:00Z"), ZoneOffset.ofHours(2), 600, null))));

        var weeks = service.weeks(FROM, TO);

        assertEquals(2, weeks.size());
        assertEquals(LocalDate.parse("2026-03-23"), weeks.get(0).weekStart());
        assertEquals(LocalDate.parse("2026-03-30"), weeks.get(1).weekStart());
    }

    @Test
    void fallBackTransitionDayStaysInSameWeek() {
        // 2026-10-25: Europe falls back, 03:00+02:00 becomes 02:00+01:00; both records are local Sunday.
        var service = new WeeklyRollupService(fakeReadModel(List.of(
                activity("before-fallback", Instant.parse("2026-10-25T00:30:00Z"), ZoneOffset.ofHours(2), 600, null),
                activity("after-fallback", Instant.parse("2026-10-25T01:30:00Z"), ZoneOffset.ofHours(1), 600, null))));

        var weeks = service.weeks(FROM, TO);

        assertEquals(1, weeks.size());
        assertEquals(LocalDate.parse("2026-10-19"), weeks.getFirst().weekStart());
        assertEquals(2, weeks.getFirst().sessionCount());
        assertEquals(1200, weeks.getFirst().totalDurationS());
    }
}
