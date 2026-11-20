package dev.healthhub.app.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.healthhub.app.persistence.ConnectionEntity;
import dev.healthhub.app.persistence.ConnectionRepository;
import dev.healthhub.app.persistence.RawEventRepository;
import dev.healthhub.app.persistence.SyncAuditRepository;
import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ConnectionService;
import dev.healthhub.ingestion.ProviderRecord;
import dev.healthhub.ingestion.ProviderSyncPort;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ConnectionServiceIT {

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

    @TestConfiguration
    static class StubSyncPortConfig {
        @Bean
        @Primary
        ProviderSyncPort stubSyncPort() {
            return new ProviderSyncPort() {
                @Override
                public String provider() {
                    return "withings";
                }

                @Override
                public List<ProviderRecord> fetch(UUID connectionId, Instant since) {
                    return List.of(
                            new ProviderRecord("grp-1:1", Source.WITHINGS, MetricType.WEIGHT_KG, 72.5,
                                    Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), "{\"g\":1}"),
                            new ProviderRecord("grp-1:5", Source.WITHINGS, MetricType.SYSTOLIC_BP_PA, 15732.0,
                                    Instant.parse("2026-03-29T01:30:00Z"), ZoneOffset.ofHours(2), "{\"g\":1}"));
                }
            };
        }
    }

    @Autowired
    ConnectionService connectionService;

    @Autowired
    ConnectionCredentialsPort credentialsPort;

    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    RawEventRepository rawEventRepository;

    @Autowired
    SyncAuditRepository syncAuditRepository;

    @Test
    void tokensAreEncryptedAtRestAndDecryptable() {
        var connectionId = UUID.randomUUID();
        connectionRepository.save(new ConnectionEntity(
                connectionId, "withings", "Withings", Instant.now(), Instant.now()));
        credentialsPort.save(connectionId, "12345678", "plaintext-access-token", "plaintext-refresh-token",
                Instant.now().plusSeconds(10800));

        var entity = connectionRepository.findById(connectionId).orElseThrow();
        assertNotEquals("plaintext-access-token", entity.getEncryptedAccessToken());
        assertTrue(entity.getEncryptedAccessToken().length() > 40);

        var credentials = credentialsPort.credentialsFor(connectionId).orElseThrow();
        assertEquals("plaintext-access-token", credentials.accessToken());
        assertEquals("plaintext-refresh-token", credentials.refreshToken());
        assertEquals("12345678", credentials.externalUserId());
    }

    @Test
    void syncIsIdempotentAndAudited() {
        var connectionId = UUID.randomUUID();
        connectionRepository.save(new ConnectionEntity(
                connectionId, "withings", "Withings", Instant.now(), Instant.now()));

        var first = connectionService.sync(connectionId);
        var second = connectionService.sync(connectionId);

        assertEquals(2, first.fetched());
        assertEquals(2, first.ingested());
        assertEquals(0, first.duplicates());
        assertEquals(2, second.fetched());
        assertEquals(0, second.ingested());
        assertEquals(2, second.duplicates());
        assertEquals(2, rawEventRepository.count());
        var audits = syncAuditRepository.findByConnectionIdOrderByStartedAtDesc(connectionId);
        assertEquals(2, audits.size());
        assertEquals("OK", audits.get(0).getStatus());
        assertEquals("OK", audits.get(1).getStatus());
    }
}
