package dev.healthhub.app.persistence;

import dev.healthhub.ingestion.SyncAuditEntry;
import dev.healthhub.ingestion.SyncAuditStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SyncAuditStoreJpa implements SyncAuditStore {

    private final SyncAuditRepository repository;

    public SyncAuditStoreJpa(SyncAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void record(SyncAuditEntry entry) {
        repository.save(new SyncAuditEntity(entry.connectionId(), entry.provider(), entry.startedAt(),
                entry.finishedAt(), entry.status(), entry.fetched(), entry.ingested(), entry.duplicates(),
                entry.error()));
    }
}
