package dev.healthhub.app.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<ConnectionEntity, UUID> {
}
