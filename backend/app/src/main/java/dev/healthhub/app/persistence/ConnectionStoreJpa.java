package dev.healthhub.app.persistence;

import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ConnectionStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConnectionStoreJpa implements ConnectionStore {

    private final ConnectionRepository repository;

    public ConnectionStoreJpa(ConnectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ConnectionInfo saveNew(String provider, String label, Instant connectedAt) {
        var entity = repository.save(new ConnectionEntity(UUID.randomUUID(), provider, label, connectedAt, connectedAt));
        return toInfo(entity);
    }

    @Override
    public List<ConnectionInfo> list() {
        return repository.findAll().stream().map(ConnectionStoreJpa::toInfo).toList();
    }

    @Override
    public Optional<ConnectionInfo> find(UUID id) {
        return repository.findById(id).map(ConnectionStoreJpa::toInfo);
    }

    @Override
    public Optional<ConnectionInfo> findByExternalUserId(String provider, String externalUserId) {
        return repository.findByProviderAndExternalUserId(provider, externalUserId).map(ConnectionStoreJpa::toInfo);
    }

    @Override
    @Transactional
    public void markSynced(UUID id, Instant finishedAt, String status) {
        var entity = repository.findById(id).orElseThrow();
        entity.setLastSyncAt(finishedAt);
        entity.setLastSyncStatus(status);
        entity.setUpdatedAt(finishedAt);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private static ConnectionInfo toInfo(ConnectionEntity entity) {
        return new ConnectionInfo(entity.getId(), entity.getProvider(), entity.getLabel(),
                entity.getCreatedAt(), entity.getLastSyncAt(), entity.getLastSyncStatus());
    }
}
