package dev.healthhub.companion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val API_BASE_URL = stringPreferencesKey("api_base_url")
}

class SettingsRepository(context: Context) {

    private val dataStore = context.settingsDataStore
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val apiBaseUrl: Flow<String> = dataStore.data.map { it[SettingsKeys.API_BASE_URL] ?: "" }

    suspend fun setApiBaseUrl(url: String) {
        dataStore.edit { it[SettingsKeys.API_BASE_URL] = url }
    }

    var deviceToken: String
        get() = encryptedPrefs.getString("device_token", "") ?: ""
        set(value) {
            encryptedPrefs.edit().putString("device_token", value).apply()
        }
}
