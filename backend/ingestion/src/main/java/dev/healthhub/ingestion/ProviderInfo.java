package dev.healthhub.ingestion;

public record ProviderInfo(
        String provider,
        boolean oauth,
        boolean credentialsConfigured) {
}
