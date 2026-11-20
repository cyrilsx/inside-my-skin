package dev.healthhub.api;

import dev.healthhub.api.dto.Dtos.ActivityDto;
import dev.healthhub.api.dto.Dtos.ConnectionDto;
import dev.healthhub.api.dto.Dtos.ExportDto;
import dev.healthhub.api.dto.Dtos.MeasurementDto;
import dev.healthhub.api.dto.Dtos.MentalSessionDto;
import dev.healthhub.api.dto.Dtos.MoodCheckinDto;
import dev.healthhub.api.dto.Dtos.UsageDailyDto;
import dev.healthhub.ingestion.ExportPort;
import dev.healthhub.ingestion.ExportSnapshot;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class ExportController {

    private final ExportPort exportPort;

    public ExportController(ExportPort exportPort) {
        this.exportPort = exportPort;
    }

    @GetMapping("/export")
    public ResponseEntity<?> export(@RequestParam(name = "format", defaultValue = "json") String format) {
        if (!"json".equals(format) && !"csv".equals(format)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "format must be 'json' or 'csv': " + format);
        }
        var snapshot = exportPort.exportAll();
        if ("csv".equals(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"healthhub-export.csv\"")
                    .body(toCsv(snapshot));
        }
        return ResponseEntity.ok(toDto(snapshot));
    }

    private static ExportDto toDto(ExportSnapshot s) {
        return new ExportDto(
                s.measurements().stream()
                        .map(m -> new MeasurementDto(m.providerRecordId(), m.source(), m.metricType().name(),
                                m.value(), m.unit().name(),
                                OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                                m.zoneOffsetAtCapture().getId(),
                                OffsetDateTime.ofInstant(m.receivedAt(), ZoneOffset.UTC).toString()))
                        .toList(),
                s.activities().stream()
                        .map(a -> new ActivityDto(a.id(), a.source(), a.type().name(), a.title(),
                                OffsetDateTime.ofInstant(a.startedAt(), a.zoneOffsetAtCapture()).toString(),
                                a.durationS(), a.distanceM(), a.avgHeartRateBpm()))
                        .toList(),
                s.mentalSessions().stream()
                        .map(m -> new MentalSessionDto(m.id(), m.source(), m.kind().name(),
                                OffsetDateTime.ofInstant(m.startedAt(), m.zoneOffsetAtCapture()).toString(),
                                m.durationS()))
                        .toList(),
                s.usageDaily().stream()
                        .map(u -> new UsageDailyDto(u.date(), u.totalScreenTimeS(), u.isEstimate(),
                                u.perApp().stream()
                                        .map(p -> new UsageDailyDto.PerAppDto(p.app(), p.foregroundS()))
                                        .toList()))
                        .toList(),
                s.moodCheckins().stream()
                        .map(m -> new MoodCheckinDto(m.id(),
                                OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                                m.valence(), m.labels(), m.note()))
                        .toList(),
                s.connections().stream()
                        .map(c -> new ConnectionDto(c.id(), c.provider(), c.label(),
                                OffsetDateTime.ofInstant(c.connectedAt(), ZoneOffset.UTC).toString(),
                                c.lastSyncAt() == null ? null
                                        : OffsetDateTime.ofInstant(c.lastSyncAt(), ZoneOffset.UTC).toString(),
                                c.lastSyncStatus()))
                        .toList());
    }

    private static String toCsv(ExportSnapshot s) {
        var out = new StringBuilder();
        section(out, List.of("providerRecordId", "source", "metricType", "value", "unit", "occurredAt",
                        "zoneOffset", "receivedAt"),
                s.measurements().stream()
                        .map(m -> List.of(m.providerRecordId(), m.source().name(), m.metricType().name(),
                                Double.toString(m.value()), m.unit().name(),
                                OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                                m.zoneOffsetAtCapture().getId(),
                                OffsetDateTime.ofInstant(m.receivedAt(), ZoneOffset.UTC).toString()))
                        .toList());
        section(out, List.of("id", "source", "type", "title", "startedAt", "zoneOffset", "durationS",
                        "distanceM", "avgHeartRateBpm"),
                s.activities().stream()
                        .map(a -> List.of(a.id().toString(), a.source().name(), a.type().name(),
                                nullable(a.title()),
                                OffsetDateTime.ofInstant(a.startedAt(), a.zoneOffsetAtCapture()).toString(),
                                a.zoneOffsetAtCapture().getId(), Long.toString(a.durationS()),
                                nullable(a.distanceM()), nullable(a.avgHeartRateBpm())))
                        .toList());
        section(out, List.of("id", "source", "kind", "startedAt", "zoneOffset", "durationS"),
                s.mentalSessions().stream()
                        .map(m -> List.of(m.id().toString(), m.source().name(), m.kind().name(),
                                OffsetDateTime.ofInstant(m.startedAt(), m.zoneOffsetAtCapture()).toString(),
                                m.zoneOffsetAtCapture().getId(), Long.toString(m.durationS())))
                        .toList());
        section(out, List.of("date", "totalScreenTimeS", "isEstimate", "perApp"),
                s.usageDaily().stream()
                        .map(u -> List.of(u.date().toString(), Long.toString(u.totalScreenTimeS()),
                                Boolean.toString(u.isEstimate()),
                                u.perApp().stream()
                                        .map(p -> p.app() + ":" + p.foregroundS())
                                        .reduce((a, b) -> a + ";" + b)
                                        .orElse("")))
                        .toList());
        section(out, List.of("id", "occurredAt", "zoneOffset", "valence", "labels", "note"),
                s.moodCheckins().stream()
                        .map(m -> List.of(m.id().toString(),
                                OffsetDateTime.ofInstant(m.occurredAt(), m.zoneOffsetAtCapture()).toString(),
                                m.zoneOffsetAtCapture().getId(), Double.toString(m.valence()),
                                String.join(";", m.labels()), nullable(m.note())))
                        .toList());
        section(out, List.of("id", "provider", "label", "connectedAt", "lastSyncAt", "lastSyncStatus"),
                s.connections().stream()
                        .map(c -> List.of(c.id().toString(), c.provider(), nullable(c.label()),
                                OffsetDateTime.ofInstant(c.connectedAt(), ZoneOffset.UTC).toString(),
                                c.lastSyncAt() == null ? "" : OffsetDateTime.ofInstant(c.lastSyncAt(), ZoneOffset.UTC).toString(),
                                nullable(c.lastSyncStatus())))
                        .toList());
        return out.toString();
    }

    private static void section(StringBuilder out, List<String> header, List<List<String>> rows) {
        out.append(String.join(",", header.stream().map(ExportController::csv).toList())).append('\n');
        for (var row : rows) {
            var cells = new ArrayList<String>(row.size());
            for (var cell : row) {
                cells.add(csv(cell));
            }
            out.append(String.join(",", cells)).append('\n');
        }
        out.append('\n');
    }

    private static String csv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static String nullable(Object value) {
        return value == null ? "" : value.toString();
    }
}
