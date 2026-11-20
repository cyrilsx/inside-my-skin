package dev.healthhub.api;

import dev.healthhub.api.dto.Dtos.ActivityDto;
import dev.healthhub.api.dto.Dtos.ActivityListDto;
import dev.healthhub.api.dto.Dtos.DailyDto;
import dev.healthhub.api.dto.Dtos.DailyListDto;
import dev.healthhub.api.dto.Dtos.MentalSessionDto;
import dev.healthhub.api.dto.Dtos.MentalSessionListDto;
import dev.healthhub.api.dto.Dtos.MoodCheckinDto;
import dev.healthhub.api.dto.Dtos.MoodCheckinListDto;
import dev.healthhub.api.dto.Dtos.SeriesDto;
import dev.healthhub.api.dto.Dtos.SeriesPointDto;
import dev.healthhub.api.dto.Dtos.UsageDailyDto;
import dev.healthhub.api.dto.Dtos.UsageDailyListDto;
import dev.healthhub.core.MetricType;
import dev.healthhub.ingestion.ReadModelPort;
import dev.healthhub.insights.DailyRollupService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class ReadControllers {

    private final DailyRollupService rollupService;
    private final ReadModelPort readModel;

    public ReadControllers(DailyRollupService rollupService, ReadModelPort readModel) {
        this.rollupService = rollupService;
        this.readModel = readModel;
    }

    @GetMapping("/dailies")
    public DailyListDto dailies(@RequestParam String from, @RequestParam String to) {
        var rows = rollupService.dailies(parseInstant(from), parseInstant(to));
        return new DailyListDto(rows.stream()
                .map(r -> new DailyDto(r.date(), r.sources(), r.steps(), r.distanceM(), r.sleepSeconds(),
                        r.restingHeartRateBpm(), r.hrvRmssdS(), r.stressScore(), r.weightKg(), r.screenTimeS()))
                .toList());
    }

    @GetMapping("/series/{metricType}")
    public SeriesDto series(@PathVariable String metricType, @RequestParam String from, @RequestParam String to) {
        MetricType type;
        try {
            type = MetricType.valueOf(metricType);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown metricType: " + metricType, e);
        }
        var points = readModel.measurements(type, parseInstant(from), parseInstant(to)).stream()
                .map(m -> new SeriesPointDto(
                        OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                        m.zoneOffsetAtCapture().getId(),
                        m.value(),
                        m.unit().name(),
                        m.source()))
                .toList();
        return new SeriesDto(type.name(), type.canonicalUnit().name(), points);
    }

    @GetMapping("/activities")
    public ActivityListDto activities(@RequestParam String from, @RequestParam String to) {
        return new ActivityListDto(readModel.activities(parseInstant(from), parseInstant(to)).stream()
                .map(a -> new ActivityDto(a.id(), a.source(), a.type().name(), a.title(),
                        OffsetDateTime.ofInstant(a.startedAt(), a.zoneOffsetAtCapture()).toString(),
                        a.durationS(), a.distanceM(), a.avgHeartRateBpm()))
                .toList());
    }

    @GetMapping("/mental/sessions")
    public MentalSessionListDto mentalSessions(@RequestParam String from, @RequestParam String to) {
        return new MentalSessionListDto(readModel.mentalSessions(parseInstant(from), parseInstant(to)).stream()
                .map(s -> new MentalSessionDto(s.id(), s.source(), s.kind().name(),
                        OffsetDateTime.ofInstant(s.startedAt(), s.zoneOffsetAtCapture()).toString(), s.durationS()))
                .toList());
    }

    @GetMapping("/mental/usage/daily")
    public UsageDailyListDto usageDaily(@RequestParam String from, @RequestParam String to) {
        return new UsageDailyListDto(readModel.usageDaily(LocalDate.parse(from), LocalDate.parse(to)).stream()
                .map(u -> new UsageDailyDto(u.date(), u.totalScreenTimeS(), u.isEstimate(),
                        u.perApp().stream().map(p -> new UsageDailyDto.PerAppDto(p.app(), p.foregroundS())).toList()))
                .toList());
    }

    @GetMapping("/mood/checkins")
    public MoodCheckinListDto moodCheckins(@RequestParam String from, @RequestParam String to) {
        return new MoodCheckinListDto(readModel.moodCheckins(parseInstant(from), parseInstant(to)).stream()
                .map(m -> new MoodCheckinDto(m.id(),
                        OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                        m.valence(), m.labels(), m.note()))
                .toList());
    }

    private static Instant parseInstant(String value) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (Exception e) {
            try {
                return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
            } catch (Exception e2) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "invalid date-time: " + value + " (expected ISO 8601, e.g. 2026-03-29T00:00:00+02:00)", e2);
            }
        }
    }
}
