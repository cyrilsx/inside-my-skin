package dev.healthhub.app;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class HealthHubApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @MockitoBean
    JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void securityProps(DynamicPropertyRegistry registry) {
        var keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        registry.add("TOKEN_ENCRYPTION_KEY", () -> Base64.getEncoder().encodeToString(keyBytes));
    }

    @Test
    void contextLoads() {
    }
}
