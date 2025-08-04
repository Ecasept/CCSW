package com.github.ecasept.ccsw.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.ecasept.ccsw.network.ServerUrlStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * DataStore manager for handling app preferences
 */
class PreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    val prefs: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        AppPreferences(
            userId = preferences[PreferencesKeys.USER_ID],
            serverUrl = preferences[PreferencesKeys.SERVER_URL]
                ?: GeneratedConfig.DEFAULT_SERVER_URL
        )
    }
    val isLoggedIn = prefs.map { it.userId != null }

    suspend fun updateUserId(userId: String?) {
        context.dataStore.edit { preferences ->
            if (userId != null) {
                preferences[PreferencesKeys.USER_ID] = userId
            } else {
                preferences.remove(PreferencesKeys.USER_ID)
            }
        }
    }

    suspend fun updateServerUrl(serverUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = serverUrl
        }
        // Update server url for api client
        ServerUrlStorage.serverUrl = serverUrl
    }
}
