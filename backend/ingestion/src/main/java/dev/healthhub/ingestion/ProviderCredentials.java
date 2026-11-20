package dev.healthhub.ingestion;

import java.time.Instant;

public record ProviderCredentials(
        String externalUserId,
        String accessToken,
        String refreshToken,
        Instant expiresAt) {

    public ProviderCredentials {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken is required");
        }
    }
}
