package dev.healthhub.app.persistence;

import dev.healthhub.core.MetricType;
import dev.healthhub.core.Source;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "measurements")
public class MeasurementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "connection_id", nullable = false)
    private UUID connectionId;

    @Column(name = "provider_record_id", nullable = false)
    private String providerRecordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private double value;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "zone_offset_at_capture", nullable = false)
    private String zoneOffsetAtCapture;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected MeasurementEntity() {
    }

    public MeasurementEntity(UUID connectionId, String providerRecordId, Source source, MetricType metricType,
            double value, Instant occurredAt, String zoneOffsetAtCapture, Instant receivedAt) {
        this.connectionId = connectionId;
        this.providerRecordId = providerRecordId;
        this.source = source;
        this.metricType = metricType;
        this.value = value;
        this.occurredAt = occurredAt;
        this.zoneOffsetAtCapture = zoneOffsetAtCapture;
        this.receivedAt = receivedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public String getProviderRecordId() {
        return providerRecordId;
    }

    public Source getSource() {
        return source;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getZoneOffsetAtCapture() {
        return zoneOffsetAtCapture;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
