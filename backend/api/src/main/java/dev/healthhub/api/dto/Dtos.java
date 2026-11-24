package dev.healthhub.api.dto;

import dev.healthhub.ingestion.ProviderInfo;

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

    public record EventDto(
            String id,
            String type,
            String occurredAt,
            Object payload) {
    }

    public record DeletionJobDto(
            UUID id,
            String status,
            String createdAt,
            String completedAt) {
    }

    public record MeasurementDto(
            String providerRecordId,
            Source source,
            String metricType,
            double value,
            String unit,
            String occurredAt,
            String zoneOffset,
            String receivedAt) {
    }

    public record ConnectionDto(
            UUID id,
            String provider,
            String label,
            String connectedAt,
            String lastSyncAt,
            String lastSyncStatus) {
    }

    public record ExportDto(
            List<MeasurementDto> measurements,
            List<ActivityDto> activities,
            List<MentalSessionDto> mentalSessions,
            List<UsageDailyDto> usageDaily,
            List<MoodCheckinDto> moodCheckins,
            List<ConnectionDto> connections) {
    }

    public record ProviderListDto(java.util.List<ProviderInfo> items) {
    }
}
