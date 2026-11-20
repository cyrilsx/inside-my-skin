package dev.healthhub.ingestion;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionCredentialsPort {

    Optional<ProviderCredentials> credentialsFor(UUID connectionId);

    void save(UUID connectionId, String externalUserId, String accessToken, String refreshToken, Instant expiresAt);

    void revoke(UUID connectionId);
}
