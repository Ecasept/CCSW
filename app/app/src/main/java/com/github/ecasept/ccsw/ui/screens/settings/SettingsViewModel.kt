package com.github.ecasept.ccsw.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.PDSRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: PDSRepo
) : ViewModel() {
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
