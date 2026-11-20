package dev.healthhub.app.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_audit")
public class SyncAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connection_id", nullable = false)
    private UUID connectionId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int fetched;

    @Column(nullable = false)
    private int ingested;

    @Column(nullable = false)
    private int duplicates;

    private String error;

    protected SyncAuditEntity() {
    }

    public SyncAuditEntity(UUID connectionId, String provider, Instant startedAt, Instant finishedAt,
            String status, int fetched, int ingested, int duplicates, String error) {
        this.connectionId = connectionId;
        this.provider = provider;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.fetched = fetched;
        this.ingested = ingested;
        this.duplicates = duplicates;
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
