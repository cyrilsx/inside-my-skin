package dev.healthhub.app.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import dev.healthhub.app.security.ProblemDetailsSupport;
import dev.healthhub.app.security.SecurityConfig;

/**
 * The test classpath carries src/test/resources/static/index.html, so resource handling works
 * in the slice: client routes must fall back to index.html, server paths must not.
 */
@WebMvcTest(controllers = SpaConfigTest.EmptyController.class)
@Import({SecurityConfig.class, ProblemDetailsSupport.class, SpaConfig.class, SpaConfigTest.EmptyController.class})
class SpaConfigTest {

    @org.springframework.web.bind.annotation.RestController
    static class EmptyController {
        @org.springframework.web.bind.annotation.GetMapping("/api/v1/ping")
        String ping() {
            return "pong";
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void clientRouteFallsBackToIndexHtml() throws Exception {
        mockMvc.perform(get("/trends"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("spa-index")));
    }

    @Test
    void apiPathIsNotRewrittenToIndexHtml() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void apiStillWorks() throws Exception {
        mockMvc.perform(get("/api/v1/ping")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void securityHeadersArePresent() throws Exception {
        // 401 (no jwt) still carries the security headers — they apply to every response
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("X-Content-Type-Options", "nosniff"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Security-Policy", org.hamcrest.Matchers.containsString("default-src 'self'")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Referrer-Policy", "no-referrer"));
    }
}
