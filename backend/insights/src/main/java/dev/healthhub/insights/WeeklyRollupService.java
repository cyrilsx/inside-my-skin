package dev.healthhub.insights;

import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.LocalDay;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ReadModelPort;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class WeeklyRollupService {

    private final ReadModelPort readModel;

    public WeeklyRollupService(ReadModelPort readModel) {
        this.readModel = readModel;
    }

    public List<WeeklyRow> weeks(Instant from, Instant to) {
        var byWeek = new TreeMap<LocalDate, WeekAccumulator>();
        for (var activity : readModel.activities(from, to)) {
            var localDay = LocalDay.of(activity.startedAt(), activity.zoneOffsetAtCapture());
            var weekStart = localDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            byWeek.computeIfAbsent(weekStart, w -> new WeekAccumulator()).add(activity);
        }
        var rows = new ArrayList<WeeklyRow>(byWeek.size());
        for (var entry : byWeek.entrySet()) {
            rows.add(entry.getValue().toRow(entry.getKey()));
        }
        return rows;
    }

    private static class WeekAccumulator {
        private final HashSet<Source> sources = new HashSet<>();
        private long sessionCount;
        private long totalDurationS;
        private double totalDistanceM;
        private boolean hasDistance;

        void add(ActivityRecord a) {
            sources.add(a.source());
            sessionCount++;
            totalDurationS += a.durationS();
            if (a.distanceM() != null) {
                totalDistanceM += a.distanceM();
                hasDistance = true;
            }
        }

        WeeklyRow toRow(LocalDate weekStart) {
            return new WeeklyRow(weekStart, sources, sessionCount, totalDurationS,
                    hasDistance ? totalDistanceM : null);
        }
    }
}
