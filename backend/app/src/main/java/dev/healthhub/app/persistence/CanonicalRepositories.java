package dev.healthhub.app.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ActivityRepository extends JpaRepository<ActivityEntity, UUID> {
    Optional<ActivityEntity> findByConnectionIdAndProviderRecordId(UUID connectionId, String providerRecordId);

    @Query("SELECT a FROM ActivityEntity a WHERE a.startedAt BETWEEN :from AND :to ORDER BY a.startedAt")
    List<ActivityEntity> findInRange(@Param("from") Instant from, @Param("to") Instant to);

    List<ActivityEntity> findAllByOrderByStartedAtAsc();
}

interface MentalSessionRepository extends JpaRepository<MentalSessionEntity, UUID> {
    Optional<MentalSessionEntity> findByConnectionIdAndProviderRecordId(UUID connectionId, String providerRecordId);

    @Query("SELECT m FROM MentalSessionEntity m WHERE m.startedAt BETWEEN :from AND :to ORDER BY m.startedAt")
    List<MentalSessionEntity> findInRange(@Param("from") Instant from, @Param("to") Instant to);

    List<MentalSessionEntity> findAllByOrderByStartedAtAsc();
}

interface UsageDailyRepository extends JpaRepository<UsageDailyEntity, UUID> {
    Optional<UsageDailyEntity> findByConnectionIdAndProviderRecordId(UUID connectionId, String providerRecordId);

    @Query("SELECT u FROM UsageDailyEntity u WHERE u.date BETWEEN :from AND :to ORDER BY u.date")
    List<UsageDailyEntity> findInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<UsageDailyEntity> findAllByOrderByDateAsc();
}

interface MoodCheckinRepository extends JpaRepository<MoodCheckinEntity, UUID> {
    Optional<MoodCheckinEntity> findByConnectionIdAndProviderRecordId(UUID connectionId, String providerRecordId);

    @Query("SELECT m FROM MoodCheckinEntity m WHERE m.occurredAt BETWEEN :from AND :to ORDER BY m.occurredAt")
    List<MoodCheckinEntity> findInRange(@Param("from") Instant from, @Param("to") Instant to);

    List<MoodCheckinEntity> findAllByOrderByOccurredAtAsc();
}
