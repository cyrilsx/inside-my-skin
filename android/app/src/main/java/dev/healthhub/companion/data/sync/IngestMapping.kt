package dev.healthhub.companion.data.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.healthhub.companion.api.models.IngestActivity
import dev.healthhub.companion.api.models.IngestItem
import dev.healthhub.companion.api.models.IngestMeasurement
import dev.healthhub.companion.api.models.IngestMentalSession
import dev.healthhub.companion.api.models.IngestMoodCheckin
import dev.healthhub.companion.api.models.IngestUsageDaily
import dev.healthhub.companion.data.local.PendingUploadEntity

object IngestMapping {

    private val mapper: ObjectMapper = jacksonObjectMapper()

    fun toIngestItem(entity: PendingUploadEntity): IngestItem {
        val json = entity.payloadJson
        return when (entity.kind) {
            "MEASUREMENT" -> mapper.readValue(json, IngestMeasurement::class.java) as IngestItem
            "ACTIVITY" -> mapper.readValue(json, IngestActivity::class.java) as IngestItem
            "MENTAL_SESSION" -> mapper.readValue(json, IngestMentalSession::class.java) as IngestItem
            "USAGE_DAILY" -> mapper.readValue(json, IngestUsageDaily::class.java) as IngestItem
            "MOOD_CHECKIN" -> mapper.readValue(json, IngestMoodCheckin::class.java) as IngestItem
            else -> throw IllegalArgumentException("unknown upload kind: ${entity.kind}")
        }
    }
}
