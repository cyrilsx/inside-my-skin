package dev.healthhub.app.persistence;

import dev.healthhub.core.ActivityType;
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
@Table(name = "activities")
public class ActivityEntity {

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
    private ActivityType type;

    private String title;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "zone_offset_at_capture", nullable = false)
    private String zoneOffsetAtCapture;

    @Column(name = "duration_s", nullable = false)
    private long durationS;

    @Column(name = "distance_m")
    private Double distanceM;

    @Column(name = "avg_heart_rate_bpm")
    private Double avgHeartRateBpm;

    protected ActivityEntity() {
    }

    public ActivityEntity(UUID connectionId, String providerRecordId, Source source, ActivityType type,
            String title, Instant startedAt, String zoneOffsetAtCapture, long durationS, Double distanceM,
            Double avgHeartRateBpm) {
        this.connectionId = connectionId;
        this.providerRecordId = providerRecordId;
        this.source = source;
        this.type = type;
        this.title = title;
        this.startedAt = startedAt;
        this.zoneOffsetAtCapture = zoneOffsetAtCapture;
        this.durationS = durationS;
        this.distanceM = distanceM;
        this.avgHeartRateBpm = avgHeartRateBpm;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public String getProviderRecordId() { return providerRecordId; }
    public Source getSource() { return source; }
    public ActivityType getType() { return type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getStartedAt() { return startedAt; }
    public String getZoneOffsetAtCapture() { return zoneOffsetAtCapture; }
    public long getDurationS() { return durationS; }
    public Double getDistanceM() { return distanceM; }
    public Double getAvgHeartRateBpm() { return avgHeartRateBpm; }
}
