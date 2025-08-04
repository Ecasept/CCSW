package com.github.ecasept.ccsw.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.AppPreferences
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val dataStore: PreferencesDataStore = PreferencesDataStore(application)
) : AndroidViewModel(application) {
    val prefs: StateFlow<AppPreferences?> = dataStore.prefs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun logout() {
        viewModelScope.launch {
            dataStore.updateUserId(null)
        }
    }
}