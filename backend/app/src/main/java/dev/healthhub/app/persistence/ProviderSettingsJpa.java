package dev.healthhub.app.persistence;

import dev.healthhub.app.security.TokenCipher;
import dev.healthhub.ingestion.ProviderSettingsPort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
public class ProviderSettingsJpa implements ProviderSettingsPort {

    private final ProviderSettingsRepository repository;
    private final TokenCipher tokenCipher;
    private final ObjectMapper objectMapper;

    public ProviderSettingsJpa(ProviderSettingsRepository repository, TokenCipher tokenCipher, ObjectMapper objectMapper) {
        this.repository = repository;
        this.tokenCipher = tokenCipher;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void save(String provider, Map<String, String> credentials) {
        var entity = repository.findById(provider).orElseGet(ProviderSettingsEntity::new);
        entity.setProvider(provider);
        entity.setCredentialsEnc(tokenCipher.encrypt(write(credentials)));
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    public Optional<Map<String, String>> find(String provider) {
        return repository.findById(provider).map(e -> read(tokenCipher.decrypt(e.getCredentialsEnc())));
    }

    @Override
    public List<String> keys(String provider) {
        return find(provider).map(m -> m.keySet().stream().sorted().toList()).orElse(List.of());
    }

    @Override
    public boolean isConfigured(String provider) {
        return repository.findById(provider).isPresent();
    }

    @Override
    @Transactional
    public void delete(String provider) {
        repository.deleteById(provider);
    }

    private String write(Map<String, String> credentials) {
        try {
            return objectMapper.writeValueAsString(credentials);
        } catch (Exception e) {
            throw new IllegalStateException("could not serialize provider credentials", e);
        }
    }

    private Map<String, String> read(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            throw new IllegalStateException("could not deserialize provider credentials", e);
        }
    }
}
