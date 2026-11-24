package dev.healthhub.ingestion;

import java.util.List;

/** Credential presence for a provider — never contains secret values. */
public record CredentialsStatus(
        boolean configured,
        List<String> keys) {
}
