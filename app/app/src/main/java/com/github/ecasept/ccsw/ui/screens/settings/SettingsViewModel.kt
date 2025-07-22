package com.github.ecasept.ccsw.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel
    @JvmOverloads constructor(
    private val application: Application,
    private val dataStore: PreferencesDataStore = PreferencesDataStore(application)) : AndroidViewModel(application) {
    val prefs = dataStore.prefs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun updateServerUrl(serverUrl: String) {
        viewModelScope.launch {
            dataStore.updateServerUrl(serverUrl)
        }
    }
}
