package dev.healthhub.companion.data.healthconnect

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dev.healthhub.companion.api.models.ActivityType
import dev.healthhub.companion.api.models.IngestActivity
import dev.healthhub.companion.api.models.IngestMeasurement
import dev.healthhub.companion.api.models.IngestMentalSession
import dev.healthhub.companion.api.models.MentalSessionKind
import dev.healthhub.companion.api.models.MetricType
import dev.healthhub.companion.data.local.PendingUploadDao
import dev.healthhub.companion.data.local.PendingUploadEntity
import dev.healthhub.companion.domain.DayAnchors
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@OptIn(ExperimentalMindfulnessSessionApi::class)
class HealthConnectRepository(
    private val client: HealthConnectClient,
    private val dao: PendingUploadDao,
    private val clock: Clock,
    private val zone: ZoneId
) {

    private val mapper = jacksonObjectMapper()

    private fun featureAvailable(feature: Int): Boolean {
        return client.features.getFeatureStatus(feature) ==
            HealthConnectFeatures.Companion.FEATURE_STATUS_AVAILABLE
    }

    suspend fun enqueueDeltas(since: Instant) {
        val now = Instant.now(clock)
        enqueueSteps(since, now)
        enqueueSleep(since, now)
        enqueueHeartRate(since, now)
        enqueueExercise(since, now)
        if (featureAvailable(HealthConnectFeatures.FEATURE_MINDFULNESS_SESSION)) {
            enqueueMindfulness(since, now)
        }
    }

    private suspend fun enqueueSteps(since: Instant, now: Instant) {
        val response = client.readRecords(
            ReadRecordsRequest(StepsRecord::class, TimeRangeFilter.between(since, now)))
        val byDay = response.records.groupBy { it.startTime.atZone(zone).toLocalDate() }
        for ((date, records) in byDay) {
            val total = records.sumOf { it.count }
            val payload = IngestMeasurement(
                kind = IngestMeasurement.Kind.MEASUREMENT,
                clientId = UUID.nameUUIDFromBytes("steps:${date}".toByteArray()),
                metricType = MetricType.STEPS,
                value = total.toDouble(),
                occurredAt = OffsetDateTime.ofInstant(DayAnchors.anchorInstant(date, zone), zone),
                zoneOffset = DayAnchors.zoneOffsetAt(date, zone).toString()
            )
            enqueue("MEASUREMENT", payload)
        }
    }

    private suspend fun enqueueSleep(since: Instant, now: Instant) {
        val response = client.readRecords(
            ReadRecordsRequest(SleepSessionRecord::class, TimeRangeFilter.between(since, now)))
        val byDay = response.records.groupBy { it.startTime.atZone(zone).toLocalDate() }
        for ((date, records) in byDay) {
            val seconds = records.sumOf { ChronoUnit.SECONDS.between(it.startTime, it.endTime) }
            val firstStart = records.minOf { it.startTime }
            val payload = IngestMeasurement(
                kind = IngestMeasurement.Kind.MEASUREMENT,
                clientId = UUID.nameUUIDFromBytes("sleep:${date}".toByteArray()),
                metricType = MetricType.SLEEP_SECONDS,
                value = seconds.toDouble(),
                occurredAt = OffsetDateTime.ofInstant(firstStart, zone),
                zoneOffset = firstStart.atZone(zone).offset.toString()
            )
            enqueue("MEASUREMENT", payload)
        }
    }

    private suspend fun enqueueHeartRate(since: Instant, now: Instant) {
        val response = client.readRecords(
            ReadRecordsRequest(HeartRateRecord::class, TimeRangeFilter.between(since, now)))
        val byDay = response.records.groupBy { record -> record.samples.minOf { it.time }.atZone(zone).toLocalDate() }
        for ((date, records) in byDay) {
            val samples = records.flatMap { it.samples }
            if (samples.isEmpty()) continue
            val avgBpm = samples.map { it.beatsPerMinute }.average()
            val payload = IngestMeasurement(
                kind = IngestMeasurement.Kind.MEASUREMENT,
                clientId = UUID.nameUUIDFromBytes("hr:${date}".toByteArray()),
                metricType = MetricType.HEART_RATE_BPM,
                value = avgBpm,
                occurredAt = OffsetDateTime.ofInstant(DayAnchors.anchorInstant(date, zone), zone),
                zoneOffset = DayAnchors.zoneOffsetAt(date, zone).toString()
            )
            enqueue("MEASUREMENT", payload)
        }
    }

    private suspend fun enqueueExercise(since: Instant, now: Instant) {
        val response = client.readRecords(
            ReadRecordsRequest(ExerciseSessionRecord::class, TimeRangeFilter.between(since, now)))
        for (record in response.records) {
            val payload = IngestActivity(
                kind = IngestActivity.Kind.ACTIVITY,
                clientId = UUID.nameUUIDFromBytes("exercise:${record.metadata.id}".toByteArray()),
                type = mapExerciseType(record.exerciseType),
                title = record.title,
                startedAt = OffsetDateTime.ofInstant(record.startTime, zone),
                durationS = ChronoUnit.SECONDS.between(record.startTime, record.endTime).toInt()
            )
            enqueue("ACTIVITY", payload)
        }
    }

    private suspend fun enqueueMindfulness(since: Instant, now: Instant) {
        val response = client.readRecords(
            ReadRecordsRequest(MindfulnessSessionRecord::class, TimeRangeFilter.between(since, now)))
        for (record in response.records) {
            val payload = IngestMentalSession(
                kind = IngestMentalSession.Kind.MENTAL_SESSION,
                clientId = UUID.nameUUIDFromBytes("mindfulness:${record.metadata.id}".toByteArray()),
                sessionKind = when (record.mindfulnessSessionType) {
                    MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION -> MentalSessionKind.MEDITATION
                    MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_BREATHING -> MentalSessionKind.BREATHING
                    MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MOVEMENT -> MentalSessionKind.MOVEMENT
                    else -> MentalSessionKind.MEDITATION
                },
                startedAt = OffsetDateTime.ofInstant(record.startTime, zone),
                durationS = ChronoUnit.SECONDS.between(record.startTime, record.endTime).toInt()
            )
            enqueue("MENTAL_SESSION", payload)
        }
    }

    private fun mapExerciseType(vendorType: Int): ActivityType {
        return when (vendorType) {
            ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> ActivityType.RUNNING
            ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> ActivityType.WALKING
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING,
            ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> ActivityType.CYCLING
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
            ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> ActivityType.SWIMMING
            ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> ActivityType.STRENGTH
            ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> ActivityType.YOGA
            else -> ActivityType.OTHER
        }
    }

    private suspend fun enqueue(kind: String, payload: Any) {
        val clientId = when (payload) {
            is IngestMeasurement -> payload.clientId
            is IngestActivity -> payload.clientId
            is IngestMentalSession -> payload.clientId
            else -> UUID.randomUUID()
        }
        dao.insertAll(listOf(PendingUploadEntity(
            clientId = clientId.toString(),
            kind = kind,
            payloadJson = mapper.writeValueAsString(payload),
            createdAtEpochMs = clock.millis()
        )))
    }
}
