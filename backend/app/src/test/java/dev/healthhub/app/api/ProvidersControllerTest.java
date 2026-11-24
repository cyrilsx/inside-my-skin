package dev.healthhub.app.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.healthhub.api.ProvidersController;
import dev.healthhub.ingestion.ConnectionService;
import dev.healthhub.ingestion.CredentialsStatus;
import dev.healthhub.ingestion.ProviderInfo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import dev.healthhub.app.security.ProblemDetailsSupport;
import dev.healthhub.app.security.SecurityConfig;

@WebMvcTest(controllers = ProvidersController.class)
@Import({SecurityConfig.class, ProblemDetailsSupport.class, dev.healthhub.api.ApiExceptionAdvice.class})
class ProvidersControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConnectionService connectionService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void listsProviders() throws Exception {
        org.mockito.Mockito.when(connectionService.listProviders())
                .thenReturn(List.of(new ProviderInfo("withings", true, true), new ProviderInfo("garmin", false, false)));

        mockMvc.perform(get("/api/v1/providers").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].provider").value("withings"))
                .andExpect(jsonPath("$.items[0].oauth").value(true))
                .andExpect(jsonPath("$.items[1].credentialsConfigured").value(false));
    }

    @Test
    void statusReturnsOnlyKeyNamesNeverValues() throws Exception {
        org.mockito.Mockito.when(connectionService.credentialsStatus("withings"))
                .thenReturn(new CredentialsStatus(true, List.of("clientId", "clientSecret")));

        mockMvc.perform(get("/api/v1/providers/withings/credentials").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.keys[0]").value("clientId"))
                .andExpect(jsonPath("$.keys[1]").value("clientSecret"));
    }

    @Test
    void putSavesCredentialsAndReturns204() throws Exception {
        mockMvc.perform(put("/api/v1/providers/garmin/credentials").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credentials\":{\"personalKey\":\"k\",\"userId\":\"u1\"}}"))
                .andExpect(status().isNoContent());
        verify(connectionService).saveCredentials(eq("garmin"), eq(Map.of("personalKey", "k", "userId", "u1")));
    }

    @Test
    void putWithInvalidCredentialsGets422Problem() throws Exception {
        doThrow(new IllegalArgumentException("credentials must contain between 1 and 16 entries"))
                .when(connectionService).saveCredentials(eq("garmin"), eq(Map.of()));

        mockMvc.perform(put("/api/v1/providers/garmin/credentials").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credentials\":{}}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void unknownProviderGets422() throws Exception {
        doThrow(new IllegalArgumentException("unknown provider: nope"))
                .when(connectionService).saveCredentials(eq("nope"), any());

        mockMvc.perform(put("/api/v1/providers/nope/credentials").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"credentials\":{\"k\":\"v\"}}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/providers/withings/credentials").with(jwt()))
                .andExpect(status().isNoContent());
        verify(connectionService).deleteCredentials("withings");
    }
}
