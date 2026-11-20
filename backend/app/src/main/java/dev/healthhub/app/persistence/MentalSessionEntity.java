package dev.healthhub.app.persistence;

import dev.healthhub.core.MentalSessionKind;
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
@Table(name = "mental_sessions")
public class MentalSessionEntity {

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
    @Column(nullable = false)
    private MentalSessionKind kind;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "zone_offset_at_capture", nullable = false)
    private String zoneOffsetAtCapture;

    @Column(name = "duration_s", nullable = false)
    private long durationS;

    protected MentalSessionEntity() {
    }

    public MentalSessionEntity(UUID connectionId, String providerRecordId, Source source, MentalSessionKind kind,
            Instant startedAt, String zoneOffsetAtCapture, long durationS) {
        this.connectionId = connectionId;
        this.providerRecordId = providerRecordId;
        this.source = source;
        this.kind = kind;
        this.startedAt = startedAt;
        this.zoneOffsetAtCapture = zoneOffsetAtCapture;
        this.durationS = durationS;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public String getProviderRecordId() { return providerRecordId; }
    public Source getSource() { return source; }
    public MentalSessionKind getKind() { return kind; }
    public Instant getStartedAt() { return startedAt; }
    public String getZoneOffsetAtCapture() { return zoneOffsetAtCapture; }
    public long getDurationS() { return durationS; }
}
