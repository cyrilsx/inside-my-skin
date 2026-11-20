package dev.healthhub.companion.ui.mood

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.healthhub.companion.api.models.IngestMoodCheckin
import dev.healthhub.companion.data.local.PendingUploadEntity
import dev.healthhub.companion.di.AppContainer
import dev.healthhub.companion.domain.MoodDraft
import dev.healthhub.companion.domain.MoodValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Composable
fun MoodScreen(container: AppContainer) {
    var valence by remember { mutableStateOf(0f) }
    var labels by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val mapper = remember { jacksonObjectMapper() }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Mood check-in")
        Text("Valence: ${"%.2f".format(valence)}")
        Slider(
            value = valence,
            onValueChange = { valence = it },
            valueRange = -1f..1f
        )
        OutlinedTextField(
            value = labels,
            onValueChange = { labels = it },
            label = { Text("Labels (comma separated)") })
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            try {
                val draft = MoodValidator.validate(MoodDraft(
                    valence = valence.toDouble(),
                    labels = labels.split(","),
                    note = note.ifBlank { null }))
                val now = OffsetDateTime.ofInstant(Instant.now(container.clock), container.zone)
                val payload = IngestMoodCheckin(
                    kind = IngestMoodCheckin.Kind.MOOD_CHECKIN,
                    clientId = UUID.randomUUID(),
                    occurredAt = now,
                    valence = draft.valence,
                    labels = draft.labels
                )
                CoroutineScope(Dispatchers.IO).launch {
                    container.pendingUploadDao.insertAll(listOf(PendingUploadEntity(
                        clientId = payload.clientId.toString(),
                        kind = "MOOD_CHECKIN",
                        payloadJson = mapper.writeValueAsString(payload),
                        createdAtEpochMs = container.clock.millis()
                    )))
                    message = "Check-in saved, will upload on next sync"
                }
            } catch (e: IllegalArgumentException) {
                message = e.message ?: "invalid check-in"
            }
        }) { Text("Save check-in") }
        if (message.isNotEmpty()) Text(message)
    }
}
