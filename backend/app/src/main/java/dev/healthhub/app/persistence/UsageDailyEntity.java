package dev.healthhub.app.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "usage_daily")
public class UsageDailyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "connection_id", nullable = false)
    private UUID connectionId;

    @Column(name = "provider_record_id", nullable = false)
    private String providerRecordId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "total_screen_time_s", nullable = false)
    private long totalScreenTimeS;

    @Column(name = "is_estimate", nullable = false)
    private boolean estimate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String perApp;

    protected UsageDailyEntity() {
    }

    public UsageDailyEntity(UUID connectionId, String providerRecordId, LocalDate date, long totalScreenTimeS,
            boolean estimate, String perApp) {
        this.connectionId = connectionId;
        this.providerRecordId = providerRecordId;
        this.date = date;
        this.totalScreenTimeS = totalScreenTimeS;
        this.estimate = estimate;
        this.perApp = perApp;
    }

    public UUID getId() { return id; }
    public UUID getConnectionId() { return connectionId; }
    public String getProviderRecordId() { return providerRecordId; }
    public LocalDate getDate() { return date; }
    public long getTotalScreenTimeS() { return totalScreenTimeS; }
    public boolean isEstimate() { return estimate; }
    public String getPerApp() { return perApp; }
}
