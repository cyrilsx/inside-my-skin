package dev.healthhub.ingestion;

import dev.healthhub.core.Measurement;
import java.util.Optional;
import java.util.UUID;

public interface MeasurementStore {

    Measurement upsert(Measurement measurement);

    Optional<Measurement> findByNaturalKey(UUID connectionId, String providerRecordId, dev.healthhub.core.MetricType metricType);

    void deleteAll();
}
