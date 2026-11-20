package dev.healthhub.app.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "connections")
public class ConnectionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String provider;

    private String label;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ConnectionEntity() {
    }

    public ConnectionEntity(UUID id, String provider, String label, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.provider = provider;
        this.label = label;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getLabel() {
        return label;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
