package dev.healthhub.app.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.healthhub.api.WebhookController;
import dev.healthhub.ingestion.ConnectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = WebhookController.class)
@org.springframework.test.context.TestPropertySource(properties = "healthhub.webhook.secret=test-webhook-secret")
class WebhookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConnectionService connectionService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void wrongSecretGetsRfc9457Problem() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/withings")
                        .header("X-Webhook-Secret", "wrong")
                        .contentType("application/json")
                        .content("{\"userid\":\"12345678\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(connectionService);
    }

    @Test
    void validSecretIsAcceptedAndDispatched() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/withings")
                        .header("X-Webhook-Secret", "test-webhook-secret")
                        .contentType("application/json")
                        .content("{\"userid\":\"12345678\"}"))
                .andExpect(status().isAccepted());
        verify(connectionService).handleWebhook(eq("withings"), any());
    }
}
