package dev.healthhub.app.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.healthhub.api.ExportController;
import dev.healthhub.core.ActivityRecord;
import dev.healthhub.core.ActivityType;
import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ExportPort;
import dev.healthhub.ingestion.ExportSnapshot;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import dev.healthhub.app.security.ProblemDetailsSupport;
import dev.healthhub.app.security.SecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExportController.class)
@Import({SecurityConfig.class, ProblemDetailsSupport.class})
class ExportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ExportPort exportPort;

    @MockitoBean
    JwtDecoder jwtDecoder;

    private static ExportSnapshot sample() {
        var measurement = new Measurement(UUID.randomUUID(), "rec-1", Source.GARMIN, MetricType.STEPS, 8421,
                Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), Instant.parse("2026-03-29T12:00:00Z"));
        var activity = new ActivityRecord(UUID.randomUUID(), Source.HEALTH_CONNECT, ActivityType.RUNNING, "Morning run",
                Instant.parse("2026-03-29T06:00:00Z"), ZoneOffset.ofHours(2), 1800, 5000.0, 140.0);
        var connection = new ConnectionInfo(UUID.randomUUID(), "garmin", "watch",
                Instant.parse("2026-01-01T00:00:00Z"), null, null);
        return new ExportSnapshot(List.of(measurement), List.of(activity), List.of(), List.of(), List.of(),
                List.of(connection));
    }

    @Test
    void jsonExportReturnsAllCanonicalCollections() throws Exception {
        when(exportPort.exportAll()).thenReturn(sample());

        mockMvc.perform(get("/api/v1/export").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.measurements[0].metricType").value("STEPS"))
                .andExpect(jsonPath("$.measurements[0].value").value(8421))
                .andExpect(jsonPath("$.measurements[0].occurredAt").value("2026-03-29T03:30+02:00"))
                .andExpect(jsonPath("$.activities[0].type").value("RUNNING"))
                .andExpect(jsonPath("$.connections[0].provider").value("garmin"))
                .andExpect(jsonPath("$.connections[0].encryptedAccessToken").doesNotExist());
    }

    @Test
    void csvExportReturnsTextCsvWithSections() throws Exception {
        when(exportPort.exportAll()).thenReturn(sample());

        var result = mockMvc.perform(get("/api/v1/export").param("format", "csv").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("metricType")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("STEPS")))
                .andReturn();
        var body = result.getResponse().getContentAsString();
        org.hamcrest.MatcherAssert.assertThat(body, org.hamcrest.Matchers.containsString("provider,label"));
    }

    @Test
    void unknownFormatGets422Problem() throws Exception {
        mockMvc.perform(get("/api/v1/export").param("format", "xml").with(jwt()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void unauthenticatedGets401Problem() throws Exception {
        mockMvc.perform(get("/api/v1/export"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
}
