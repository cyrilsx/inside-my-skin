package dev.healthhub.companion.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.healthhub.companion.data.sync.SyncEnqueuer
import dev.healthhub.companion.di.AppContainer
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(container: AppContainer) {
    var pending by remember { mutableStateOf(-1) }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        pending = container.pendingUploadDao.pendingCount()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("HealthHub companion")
        Spacer(Modifier.height(8.dp))
        Text("Pending uploads: ${if (pending < 0) "…" else pending}")
        if (message.isNotEmpty()) Text(message)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val today = LocalDate.now(container.zone)
            val yesterday = today.minusDays(1)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                container.healthConnectRepository.enqueueDeltas(yesterday.atStartOfDay(container.zone).toInstant())
                container.usageStatsRepository.enqueueDailyUsage(today)
                container.usageStatsRepository.enqueueDailyUsage(yesterday)
                SyncEnqueuer.enqueueNow(context)
                pending = container.pendingUploadDao.pendingCount()
                message = "Delta sync queued"
            }
        }) { Text("Sync now") }
    }
}
