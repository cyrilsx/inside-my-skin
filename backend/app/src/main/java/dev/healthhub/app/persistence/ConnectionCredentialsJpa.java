package dev.healthhub.app.persistence;

import dev.healthhub.app.security.TokenCipher;
import dev.healthhub.ingestion.ConnectionCredentialsPort;
import dev.healthhub.ingestion.ProviderCredentials;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConnectionCredentialsJpa implements ConnectionCredentialsPort {

    private final ConnectionRepository repository;
    private final TokenCipher tokenCipher;

    public ConnectionCredentialsJpa(ConnectionRepository repository, TokenCipher tokenCipher) {
        this.repository = repository;
        this.tokenCipher = tokenCipher;
    }

    @Override
    public Optional<ProviderCredentials> credentialsFor(UUID connectionId) {
        return repository.findById(connectionId)
                .filter(e -> e.getEncryptedAccessToken() != null)
                .map(e -> new ProviderCredentials(e.getExternalUserId(),
                        tokenCipher.decrypt(e.getEncryptedAccessToken()),
                        e.getEncryptedRefreshToken() != null ? tokenCipher.decrypt(e.getEncryptedRefreshToken()) : null,
                        e.getTokenExpiresAt()));
    }

    @Override
    @Transactional
    public void save(UUID connectionId, String externalUserId, String accessToken, String refreshToken, Instant expiresAt) {
        var entity = repository.findById(connectionId).orElseThrow();
        entity.setExternalUserId(externalUserId);
        entity.setEncryptedAccessToken(tokenCipher.encrypt(accessToken));
        entity.setEncryptedRefreshToken(refreshToken != null ? tokenCipher.encrypt(refreshToken) : null);
        entity.setTokenExpiresAt(expiresAt);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void revoke(UUID connectionId) {
        var entity = repository.findById(connectionId).orElseThrow();
        entity.setEncryptedAccessToken(null);
        entity.setEncryptedRefreshToken(null);
        entity.setTokenExpiresAt(null);
        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }
}
