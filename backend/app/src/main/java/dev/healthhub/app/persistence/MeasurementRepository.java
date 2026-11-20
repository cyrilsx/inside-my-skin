package dev.healthhub.app.persistence;

import dev.healthhub.core.MetricType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<MeasurementEntity, UUID> {

    Optional<MeasurementEntity> findByConnectionIdAndProviderRecordIdAndMetricType(
            UUID connectionId, String providerRecordId, MetricType metricType);
}
