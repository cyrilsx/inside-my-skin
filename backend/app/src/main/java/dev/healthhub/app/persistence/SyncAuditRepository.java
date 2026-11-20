package dev.healthhub.app.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncAuditRepository extends JpaRepository<SyncAuditEntity, Long> {

    List<SyncAuditEntity> findByConnectionIdOrderByStartedAtDesc(UUID connectionId);
}
