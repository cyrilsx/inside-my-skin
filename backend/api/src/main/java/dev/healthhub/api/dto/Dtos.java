package dev.healthhub.api.dto;

import dev.healthhub.core.Source;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Dtos {

    private Dtos() {
    }

    public record DailyDto(
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

    public record DailyListDto(List<DailyDto> items) {
    }

    public record SeriesPointDto(
            String occurredAt,
            String zoneOffset,
            double value,
            String unit,
            Source source) {
    }

    public record SeriesDto(String metricType, String unit, List<SeriesPointDto> points) {
    }

    public record ActivityDto(
            UUID id,
            Source source,
            String type,
            String title,
            String startedAt,
            long durationS,
            Double distanceM,
            Double avgHeartRateBpm) {
    }

    public record ActivityListDto(List<ActivityDto> items) {
    }

    public record MentalSessionDto(
            UUID id,
            Source source,
            String kind,
            String startedAt,
            long durationS) {
    }

    public record MentalSessionListDto(List<MentalSessionDto> items) {
    }

    public record UsageDailyDto(
            LocalDate date,
            long totalScreenTimeS,
            boolean isEstimate,
            List<PerAppDto> perApp) {

        public record PerAppDto(String app, long foregroundS) {
        }
    }

    public record UsageDailyListDto(List<UsageDailyDto> items) {
    }

    public record MoodCheckinDto(
            UUID id,
            String occurredAt,
            double valence,
            List<String> labels,
            String note) {
    }

    public record MoodCheckinListDto(List<MoodCheckinDto> items) {
    }
}
