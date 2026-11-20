package dev.healthhub.companion

import android.app.Application
import dev.healthhub.companion.data.sync.SyncEnqueuer
import dev.healthhub.companion.di.AppContainer

class CompanionApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SyncEnqueuer.enqueuePeriodic(this)
    }
}
