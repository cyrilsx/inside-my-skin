package dev.healthhub.insights;

import dev.healthhub.core.LocalDay;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.ingestion.ReadModelPort;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class DailyRollupService {

    private final ReadModelPort readModel;

    public DailyRollupService(ReadModelPort readModel) {
        this.readModel = readModel;
    }

    public List<DailyRow> dailies(Instant from, Instant to) {
        var byDay = new TreeMap<LocalDate, DayAccumulator>();
        for (var type : MetricType.values()) {
            for (var m : readModel.measurements(type, from, to)) {
                var day = LocalDay.of(m.occurredAt(), m.zoneOffsetAtCapture());
                byDay.computeIfAbsent(day, d -> new DayAccumulator()).add(m);
            }
        }
        var usage = readModel.usageDaily(
                LocalDay.of(from, java.time.ZoneOffset.UTC), LocalDay.of(to, java.time.ZoneOffset.UTC));
        for (var u : usage) {
            byDay.computeIfAbsent(u.date(), d -> new DayAccumulator()).screenTimeS = u.totalScreenTimeS();
        }
        var rows = new ArrayList<DailyRow>();
        for (var entry : byDay.entrySet()) {
            rows.add(entry.getValue().toRow(entry.getKey()));
        }
        return rows;
    }

    private static class DayAccumulator {
        private final HashSet<dev.healthhub.core.Source> sources = new HashSet<>();
        private final Map<MetricType, List<Double>> values = new EnumMap<>(MetricType.class);
        private long screenTimeS;

        void add(Measurement m) {
            sources.add(m.source());
            values.computeIfAbsent(m.metricType(), t -> new ArrayList<>()).add(m.value());
        }

        DailyRow toRow(LocalDate date) {
            return new DailyRow(
                    date,
                    sources,
                    sum(MetricType.STEPS),
                    sumD(MetricType.DISTANCE_M),
                    sum(MetricType.SLEEP_SECONDS),
                    avg(MetricType.RESTING_HEART_RATE_BPM),
                    avg(MetricType.HRV_RMSSD_S),
                    avg(MetricType.STRESS_SCORE),
                    last(MetricType.WEIGHT_KG),
                    screenTimeS > 0 ? screenTimeS : null);
        }

        private Long sum(MetricType type) {
            var list = values.get(type);
            return list == null ? null : (long) list.stream().mapToDouble(Double::doubleValue).sum();
        }

        private Double sumD(MetricType type) {
            var list = values.get(type);
            return list == null ? null : list.stream().mapToDouble(Double::doubleValue).sum();
        }

        private Double avg(MetricType type) {
            var list = values.get(type);
            return list == null || list.isEmpty() ? null : list.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        }

        private Double last(MetricType type) {
            var list = values.get(type);
            return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
        }
    }
}
