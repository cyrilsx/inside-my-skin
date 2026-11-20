package dev.healthhub.companionapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class IngestDtos {

    private IngestDtos() {
    }

    public record IngestBatchDto(@NotNull List<@NotNull IngestItemDto> items) {
    }

    public record IngestItemDto(
            @NotBlank String kind,
            @NotNull UUID clientId,
            String metricType,
            Double value,
            OffsetDateTime occurredAt,
            String zoneOffset,
            String type,
            String title,
            OffsetDateTime startedAt,
            Long durationS,
            Double distanceM,
            String sessionKind,
            LocalDate date,
            Long totalScreenTimeS,
            List<PerAppDto> perApp,
            Double valence,
            List<String> labels,
            String note) {

        public record PerAppDto(@NotBlank String app, @NotNull Long foregroundS) {
        }
    }

    public record IngestItemResultDto(@NotNull UUID clientId, @NotBlank String status, Object problem) {
    }

    public record IngestResultDto(int acceptedCount, int duplicateCount, int rejectedCount,
            List<IngestItemResultDto> results) {
    }
}
