package dev.healthhub.app.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProviderSettingsRepository extends JpaRepository<ProviderSettingsEntity, String> {
}
