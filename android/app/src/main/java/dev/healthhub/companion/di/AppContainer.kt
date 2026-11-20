package dev.healthhub.companion.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.room.Room
import dev.healthhub.companion.data.SettingsRepository
import dev.healthhub.companion.data.healthconnect.HealthConnectRepository
import dev.healthhub.companion.data.local.AppDatabase
import dev.healthhub.companion.data.usage.UsageStatsRepository
import java.time.Clock
import java.time.ZoneId

class AppContainer(context: Context) {

    val clock: Clock = Clock.systemDefaultZone()
    val zone: ZoneId = ZoneId.systemDefault()

    val settings = SettingsRepository(context)

    val database: AppDatabase = Room.databaseBuilder(
        context, AppDatabase::class.java, "healthhub-companion.db").build()

    val pendingUploadDao = database.pendingUploadDao()

    val healthConnectClient: HealthConnectClient = HealthConnectClient.getOrCreate(context)

    val healthConnectRepository = HealthConnectRepository(
        healthConnectClient, pendingUploadDao, clock, zone)

    val usageStatsRepository = UsageStatsRepository(context, pendingUploadDao, clock, zone)
}
