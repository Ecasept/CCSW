package com.github.ecasept.ccsw.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * DataStore manager for handling app preferences
 */
class PreferencesDataStoreRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val INSTANCE_ID = stringPreferencesKey("instance_id")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    val prefs: Flow<AppPreferences> = dataStore.data.map { preferences ->
        AppPreferences(
            instanceId = preferences[PreferencesKeys.INSTANCE_ID],
            sessionToken = preferences[PreferencesKeys.SESSION_TOKEN],
            serverUrl = preferences[PreferencesKeys.SERVER_URL]
                ?: GeneratedConfig.DEFAULT_SERVER_URL
        )
    }
    val isLoggedIn = prefs.map { it.instanceId != null && it.sessionToken != null }

    /** Updates a nullable preference key with a value or removes it if the value is null. */
    private suspend fun <T> updateNullable(key: Preferences.Key<T>, value: T?) {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[key] = value
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun logout() {
        updateNullable(PreferencesKeys.INSTANCE_ID, null)
        updateNullable(PreferencesKeys.SESSION_TOKEN, null)
    }

    suspend fun updateInstanceId(userId: String?) {
        return updateNullable(PreferencesKeys.INSTANCE_ID, userId)
    }

    suspend fun updateSessionToken(sessionToken: String?) {
        return updateNullable(PreferencesKeys.SESSION_TOKEN, sessionToken)
    }

    suspend fun updateServerUrl(serverUrl: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = serverUrl
        }
    }
}

typealias PDSRepo = PreferencesDataStoreRepository