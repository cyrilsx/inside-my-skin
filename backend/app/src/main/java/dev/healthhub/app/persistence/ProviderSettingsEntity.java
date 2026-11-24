package dev.healthhub.app.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "provider_settings")
public class ProviderSettingsEntity {

    @Id
    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "credentials_enc", nullable = false)
    private String credentialsEnc;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCredentialsEnc() {
        return credentialsEnc;
    }

    public void setCredentialsEnc(String credentialsEnc) {
        this.credentialsEnc = credentialsEnc;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
