package dev.healthhub.insights;

import dev.healthhub.core.Source;
import java.time.LocalDate;
import java.util.Set;

public record DailyRow(
        LocalDate date,
        Set<Source> sources,
        Long steps,
        Double distanceM,
        Long sleepSeconds,
        Double restingHeartRateBpm,
        Double hrvRmssdS,
        Double stressScore,
        Double weightKg,
        Long screenTimeS) {
}
