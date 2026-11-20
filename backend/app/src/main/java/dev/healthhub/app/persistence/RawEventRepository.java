package dev.healthhub.app.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawEventRepository extends JpaRepository<RawEventEntity, Long> {

    Optional<RawEventEntity> findByConnectionIdAndProviderRecordId(UUID connectionId, String providerRecordId);
}
