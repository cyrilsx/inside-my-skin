package dev.healthhub.app.persistence;

import dev.healthhub.ingestion.DeletionJob;
import dev.healthhub.ingestion.DeletionJobStore;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeletionJobStoreJpa implements DeletionJobStore {

    private final DeletionJobRepository repository;

    public DeletionJobStoreJpa(DeletionJobRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public DeletionJob save(DeletionJob job) {
        var saved = repository.save(new DeletionJobEntity(job.id(), job.status(), job.createdAt(), job.completedAt()));
        return toRecord(saved);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, String status, Instant completedAt) {
        var entity = repository.findById(id).orElseThrow();
        entity.setStatus(status);
        entity.setCompletedAt(completedAt);
        repository.save(entity);
    }

    @Override
    public Optional<DeletionJob> find(UUID id) {
        return repository.findById(id).map(DeletionJobStoreJpa::toRecord);
    }

    private static DeletionJob toRecord(DeletionJobEntity entity) {
        return new DeletionJob(entity.getId(), entity.getStatus(), entity.getCreatedAt(), entity.getCompletedAt());
    }
}
