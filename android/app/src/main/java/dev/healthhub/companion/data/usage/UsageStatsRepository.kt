package dev.healthhub.companion.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.healthhub.companion.api.models.IngestUsageDaily
import dev.healthhub.companion.api.models.IngestUsageDailyPerAppInner
import dev.healthhub.companion.data.local.PendingUploadDao
import dev.healthhub.companion.data.local.PendingUploadEntity
import dev.healthhub.companion.domain.UsageAggregator
import dev.healthhub.companion.domain.UsageEvent
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class UsageStatsRepository(
    private val context: Context,
    private val dao: PendingUploadDao,
    private val clock: Clock,
    private val zone: ZoneId
) {

    private val mapper = jacksonObjectMapper()

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun enqueueDailyUsage(date: LocalDate) {
        if (!hasUsageAccess()) return
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val events = usageStatsManager.queryEvents(dayStart, dayEnd)
        val list = mutableListOf<UsageEvent>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                list.add(UsageEvent(event.packageName, event.timeStamp, event.timeStamp))
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                val last = list.lastOrNull()
                if (last != null && last.packageName == event.packageName && last.endMs == last.startMs) {
                    list[list.lastIndex] = last.copy(endMs = event.timeStamp)
                }
            }
        }
        val day = UsageAggregator.aggregate(list, date, zone)
        val payload = IngestUsageDaily(
            kind = IngestUsageDaily.Kind.USAGE_DAILY,
            clientId = UUID.nameUUIDFromBytes("usage:${date}".toByteArray()),
            date = date,
            totalScreenTimeS = day.totalForegroundS.toInt(),
            perApp = day.perAppSeconds.map { (app, s) ->
                IngestUsageDailyPerAppInner(app = app, foregroundS = s.toInt())
            }
        )
        dao.insertAll(listOf(PendingUploadEntity(
            clientId = payload.clientId.toString(),
            kind = "USAGE_DAILY",
            payloadJson = mapper.writeValueAsString(payload),
            createdAtEpochMs = clock.millis()
        )))
    }
}
