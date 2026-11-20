package dev.healthhub.app.persistence;

import dev.healthhub.ingestion.RawEvent;
import dev.healthhub.ingestion.RawEventStore;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RawEventStoreJpa implements RawEventStore {

    private final RawEventRepository repository;

    public RawEventStoreJpa(RawEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RawEvent> findByNaturalKey(UUID connectionId, String providerRecordId) {
        return repository.findByConnectionIdAndProviderRecordId(connectionId, providerRecordId).map(RawEventStoreJpa::toRecord);
    }

    @Override
    @Transactional
    public RawEvent saveIfAbsent(RawEvent event) {
        var existing = repository.findByConnectionIdAndProviderRecordId(event.connectionId(), event.providerRecordId());
        if (existing.isPresent()) {
            return toRecord(existing.get());
        }
        try {
            var saved = repository.save(new RawEventEntity(
                    event.connectionId(), event.providerRecordId(), event.payload(), event.receivedAt(), event.normalized()));
            return toRecord(saved);
        } catch (DataIntegrityViolationException raceLost) {
            return toRecord(repository
                    .findByConnectionIdAndProviderRecordId(event.connectionId(), event.providerRecordId())
                    .orElseThrow());
        }
    }

    private static RawEvent toRecord(RawEventEntity entity) {
        return new RawEvent(entity.getId(), entity.getConnectionId(), entity.getProviderRecordId(),
                entity.getPayload(), entity.getReceivedAt(), entity.isNormalized());
    }
}
