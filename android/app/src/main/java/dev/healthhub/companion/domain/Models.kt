package dev.healthhub.companion.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class PendingUpload(
    val clientId: String,
    val kind: String,
    val payloadJson: String,
    val createdAtEpochMs: Long
)

data class UsageEvent(
    val packageName: String,
    val startMs: Long,
    val endMs: Long
)

data class UsageDay(
    val date: LocalDate,
    val totalForegroundS: Long,
    val perAppSeconds: Map<String, Long>,
    val isEstimate: Boolean = true
)

object UsageAggregator {

    fun aggregate(events: List<UsageEvent>, date: LocalDate, zone: ZoneId): UsageDay {
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val perApp = mutableMapOf<String, Long>()
        for (event in events) {
            if (event.endMs <= dayStart || event.startMs >= dayEnd) continue
            val clippedStart = maxOf(event.startMs, dayStart)
            val clippedEnd = minOf(event.endMs, dayEnd)
            if (clippedEnd <= clippedStart) continue
            val seconds = (clippedEnd - clippedStart) / 1000
            perApp[event.packageName] = (perApp[event.packageName] ?: 0L) + seconds
        }
        return UsageDay(
            date = date,
            totalForegroundS = perApp.values.sum(),
            perAppSeconds = perApp.toMap(),
            isEstimate = true
        )
    }
}

data class MoodDraft(
    val valence: Double,
    val labels: List<String>,
    val note: String?
)

object MoodValidator {

    fun validate(draft: MoodDraft): MoodDraft {
        require(draft.valence in -1.0..1.0) { "valence must be in [-1, 1]" }
        val labels = draft.labels.map { it.trim() }.filter { it.isNotEmpty() }
        return draft.copy(labels = labels)
    }
}

object DayAnchors {

    fun anchorInstant(date: LocalDate, zone: ZoneId): Instant {
        return date.atTime(12, 0).atZone(zone).toInstant()
    }

    fun zoneOffsetAt(date: LocalDate, zone: ZoneId): ZoneOffset {
        return zone.rules.getOffset(date.atTime(12, 0).atZone(zone).toInstant())
    }
}
