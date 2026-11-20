package dev.healthhub.app.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<ConnectionEntity, UUID> {

    Optional<ConnectionEntity> findByProviderAndExternalUserId(String provider, String externalUserId);
}
