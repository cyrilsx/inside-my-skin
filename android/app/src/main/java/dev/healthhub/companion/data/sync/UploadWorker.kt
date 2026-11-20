package dev.healthhub.companion.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.healthhub.companion.api.models.IngestBatch
import dev.healthhub.companion.data.SettingsRepository
import dev.healthhub.companion.data.api.ApiFactory
import dev.healthhub.companion.data.local.PendingUploadDao
import kotlinx.coroutines.flow.first

class UploadWorker(
    context: Context,
    params: WorkerParameters,
    private val dao: PendingUploadDao,
    private val settings: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = dao.pending(limit = 500)
        if (pending.isEmpty()) return Result.success()

        val baseUrl = settings.apiBaseUrl.first()
        if (baseUrl.isBlank() || settings.deviceToken.isBlank()) {
            return Result.retry()
        }

        return try {
            val api = ApiFactory.companionApi(baseUrl, settings.deviceToken)
            val batch = IngestBatch(items = pending.map { IngestMapping.toIngestItem(it) })
            val result = api.ingestBatch(batch)
            val acceptedOrDuplicate = result.results
                .filter { it.status == dev.healthhub.companion.api.models.IngestItemResult.Status.ACCEPTED
                        || it.status == dev.healthhub.companion.api.models.IngestItemResult.Status.DUPLICATE }
                .map { it.clientId.toString() }
            val rejected = result.results
                .filter { it.status == dev.healthhub.companion.api.models.IngestItemResult.Status.REJECTED }
                .map { it.clientId.toString() }
            if (acceptedOrDuplicate.isNotEmpty()) dao.deleteByClientIds(acceptedOrDuplicate)
            if (rejected.isNotEmpty()) dao.markFailed(rejected)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

}
