package dev.healthhub.app.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mood_checkins")
public class MoodCheckinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "connection_id", nullable = false)
    private UUID connectionId;

    @Column(name = "provider_record_id", nullable = false)
    private String providerRecordId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "zone_offset_at_capture", nullable = false)
    private String zoneOffsetAtCapture;

    @Column(nullable = false)
    private double valence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String labels;

    private String note;

    protected MoodCheckinEntity() {
    }

    public MoodCheckinEntity(UUID connectionId, String providerRecordId, Instant occurredAt,
            String zoneOffsetAtCapture, double valence, String labels, String note) {
        this.connectionId = connectionId;
        this.providerRecordId = providerRecordId;
        this.occurredAt = occurredAt;
        this.zoneOffsetAtCapture = zoneOffsetAtCapture;
        this.valence = valence;
        this.labels = labels;
        this.note = note;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public String getProviderRecordId() { return providerRecordId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getZoneOffsetAtCapture() { return zoneOffsetAtCapture; }
    public double getValence() { return valence; }
    public String getLabels() { return labels; }
    public String getNote() { return note; }
}
