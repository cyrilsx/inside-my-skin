package dev.healthhub.companion.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.healthhub.companion.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(container: AppContainer) {
    var baseUrl by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        baseUrl = container.settings.apiBaseUrl.first()
        token = container.settings.deviceToken
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings")
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("API base URL") })
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Device token") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                container.settings.setApiBaseUrl(baseUrl.trim())
                container.settings.deviceToken = token.trim()
                message = "Saved"
            }
        }) { Text("Save") }
        if (message.isNotEmpty()) Text(message)
    }
}
