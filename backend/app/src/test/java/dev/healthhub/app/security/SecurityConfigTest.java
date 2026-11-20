package dev.healthhub.app.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.healthhub.api.WebhookController;
import dev.healthhub.ingestion.ConnectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = WebhookController.class)
@Import({SecurityConfig.class, ProblemDetailsSupport.class})
@TestPropertySource(properties = "healthhub.webhook.secret=test-webhook-secret")
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    ConnectionService connectionService;

    @Test
    void apiWithoutTokenReturnsRfc9457Problem() throws Exception {
        mockMvc.perform(get("/api/v1/dailies")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-02T00:00:00Z"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void apiWithTokenPassesAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/dailies")
                        .with(jwt())
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-02T00:00:00Z"))
                .andExpect(status().isNotFound());
    }

    @Test
    void webhookPathIsNotJwtProtected() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/withings")
                        .header("X-Webhook-Secret", "test-webhook-secret")
                        .contentType("application/json")
                        .content("{\"userid\":\"12345678\"}"))
                .andExpect(status().isAccepted());
    }
}
