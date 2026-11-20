package dev.healthhub.app.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.healthhub.api.DeletionController;
import dev.healthhub.ingestion.DeletionJob;
import dev.healthhub.ingestion.DeletionJobLauncher;
import dev.healthhub.ingestion.DeletionService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import dev.healthhub.app.security.ProblemDetailsSupport;
import dev.healthhub.app.security.SecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeletionController.class)
@Import({SecurityConfig.class, ProblemDetailsSupport.class})
class DeletionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DeletionService deletionService;

    @MockitoBean
    DeletionJobLauncher launcher;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void deleteDataReturns202WithPendingJobAndLaunchesAsyncExecution() throws Exception {
        var jobId = UUID.randomUUID();
        when(deletionService.requestDeletion()).thenReturn(
                new DeletionJob(jobId, "PENDING", Instant.parse("2026-11-20T12:00:00Z"), null));

        mockMvc.perform(delete("/api/v1/data").with(jwt()))
                .andExpect(status().isAccepted())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-20T12:00Z"))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        verify(launcher).launch(jobId);
    }

    @Test
    void unauthenticatedGets401Problem() throws Exception {
        mockMvc.perform(delete("/api/v1/data"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401));

        verify(deletionService, org.mockito.Mockito.never()).requestDeletion();
        verify(launcher, org.mockito.Mockito.never()).launch(any());
    }
}
