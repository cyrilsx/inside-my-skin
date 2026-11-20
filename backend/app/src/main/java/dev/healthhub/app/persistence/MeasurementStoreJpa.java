package dev.healthhub.app.persistence;

import dev.healthhub.core.Measurement;
import dev.healthhub.core.MetricType;
import dev.healthhub.ingestion.MeasurementStore;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MeasurementStoreJpa implements MeasurementStore {

    private final MeasurementRepository repository;

    public MeasurementStoreJpa(MeasurementRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Measurement upsert(Measurement measurement) {
        var existing = repository.findByConnectionIdAndProviderRecordIdAndMetricType(
                measurement.connectionId(), measurement.providerRecordId(), measurement.metricType());
        if (existing.isPresent()) {
            var entity = existing.get();
            entity.setValue(measurement.value());
            return toRecord(repository.save(entity));
        }
        var saved = repository.save(new MeasurementEntity(
                measurement.connectionId(), measurement.providerRecordId(), measurement.source(),
                measurement.metricType(), measurement.value(), measurement.occurredAt(),
                measurement.zoneOffsetAtCapture().getId(), measurement.receivedAt()));
        return toRecord(saved);
    }

    @Override
    public Optional<Measurement> findByNaturalKey(UUID connectionId, String providerRecordId, MetricType metricType) {
        return repository.findByConnectionIdAndProviderRecordIdAndMetricType(connectionId, providerRecordId, metricType)
                .map(MeasurementStoreJpa::toRecord);
    }

    private static Measurement toRecord(MeasurementEntity entity) {
        return new Measurement(entity.getConnectionId(), entity.getProviderRecordId(), entity.getSource(),
                entity.getMetricType(), entity.getValue(), entity.getOccurredAt(),
                ZoneOffset.of(entity.getZoneOffsetAtCapture()), entity.getReceivedAt());
    }
}
