package com.github.ecasept.ccsw.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val dataStore: PreferencesDataStore = PreferencesDataStore(application)
) : AndroidViewModel(application) {

    fun login() {
        viewModelScope.launch {
            dataStore.updateUserId(_loginState.value.userId)
        }
    }

    fun updateUserId(userId: String) {
        _loginState.update { it.copy(userId = userId) }
    }

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()
}

data class LoginState(
    val userId: String = ""
)