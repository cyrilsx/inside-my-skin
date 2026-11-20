package dev.healthhub.insights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.core.UsageDailyRecord;
import dev.healthhub.ingestion.ReadModelPort;
import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.MentalSessionRecord;
import dev.healthhub.core.MoodCheckinRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DailyRollupServiceTest {

    private static final UUID CONNECTION = UUID.randomUUID();

    private static ReadModelPort fakeReadModel(List<Measurement> measurements, List<UsageDailyRecord> usage) {
        return new ReadModelPort() {
            @Override
            public List<Measurement> measurements(MetricType type, Instant from, Instant to) {
                return measurements.stream().filter(m -> m.metricType() == type).toList();
            }

            @Override
            public List<ActivityRecord> activities(Instant from, Instant to) {
                return List.of();
            }

            @Override
            public List<MentalSessionRecord> mentalSessions(Instant from, Instant to) {
                return List.of();
            }

            @Override
            public List<UsageDailyRecord> usageDaily(LocalDate from, LocalDate to) {
                return usage;
            }

            @Override
            public List<MoodCheckinRecord> moodCheckins(Instant from, Instant to) {
                return List.of();
            }
        };
    }

    @Test
    void bucketsByLocalDayUsingCapturedOffset() {
        var m1 = new Measurement(CONNECTION, "r1", Source.GARMIN, MetricType.STEPS, 5000,
                Instant.parse("2026-03-28T23:30:00Z"), ZoneOffset.ofHours(1), Instant.now());
        var m2 = new Measurement(CONNECTION, "r2", Source.GARMIN, MetricType.STEPS, 3421,
                Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), Instant.now());
        var service = new DailyRollupService(fakeReadModel(List.of(m1, m2), List.of()));

        var rows = service.dailies(Instant.parse("2026-03-28T00:00:00Z"), Instant.parse("2026-03-30T00:00:00Z"));

        assertEquals(1, rows.size());
        assertEquals(LocalDate.of(2026, 3, 29), rows.get(0).date());
        assertEquals(8421L, rows.get(0).steps());
    }

    @Test
    void dstSpringForwardDayKeepsBothOffsetsInOneBucket() {
        var beforeJump = new Measurement(CONNECTION, "r1", Source.WITHINGS, MetricType.WEIGHT_KG, 72.0,
                Instant.parse("2026-03-29T00:30:00Z"), ZoneOffset.ofHours(1), Instant.now());
        var afterJump = new Measurement(CONNECTION, "r2", Source.WITHINGS, MetricType.WEIGHT_KG, 72.4,
                Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), Instant.now());
        var service = new DailyRollupService(fakeReadModel(List.of(beforeJump, afterJump), List.of()));

        var rows = service.dailies(Instant.parse("2026-03-29T00:00:00Z"), Instant.parse("2026-03-30T00:00:00Z"));

        assertEquals(1, rows.size());
        assertEquals(LocalDate.of(2026, 3, 29), rows.get(0).date());
        assertEquals(72.4, rows.get(0).weightKg());
    }

    @Test
    void usageScreenTimeJoinsTheDayRow() {
        var usage = List.of(new UsageDailyRecord(LocalDate.of(2026, 3, 29), 14400, true, List.of()));
        var service = new DailyRollupService(fakeReadModel(List.of(), usage));

        var rows = service.dailies(Instant.parse("2026-03-28T00:00:00Z"), Instant.parse("2026-03-30T00:00:00Z"));

        assertEquals(1, rows.size());
        assertEquals(14400L, rows.get(0).screenTimeS());
        assertNull(rows.get(0).steps());
    }
}
