package dev.healthhub.ingestion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * User-supplied provider credentials (client id/secret, API keys, vendor account ids), stored
 * encrypted at rest by the app module. Adapters resolve these first and fall back to environment
 * configuration, so a provider can be added dynamically at runtime without redeploying env vars.
 */
public interface ProviderSettingsPort {

    void save(String provider, Map<String, String> credentials);

    /** Decrypted credentials, if any were saved. */
    Optional<Map<String, String>> find(String provider);

    /** Key names only — secret values are never exposed. */
    List<String> keys(String provider);

    boolean isConfigured(String provider);

    void delete(String provider);
}
