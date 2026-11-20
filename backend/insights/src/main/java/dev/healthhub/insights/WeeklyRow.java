package dev.healthhub.insights;

import dev.healthhub.core.Source;
import java.time.LocalDate;
import java.util.Set;

public record WeeklyRow(
        LocalDate weekStart,
        Set<Source> sources,
        long sessionCount,
        long totalDurationS,
        Double totalDistanceM) {
}
