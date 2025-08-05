package com.github.ecasept.ccsw.ui.screens.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import com.github.ecasept.ccsw.network.createAPI
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val dataStore: PreferencesDataStore = PreferencesDataStore(application)
) : AndroidViewModel(application) {

    private val apiClient = createAPI(dataStore)

    fun login(navigate: () -> Unit) {
        _loginState.update { it.copy(loadState = LoadState.Loading) }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val userId = _loginState.value.userId
                viewModelScope.launch {
                    apiClient.registerToken(token, userId).onSuspend(
                        success = {
                            dataStore.updateUserId(_loginState.value.userId)
                            _loginState.update {
                                it.copy(loadState = LoadState.None)
                            }
                            navigate()
                        },
                        error = { error ->
                            Log.e("LoginViewModel", "Login failed: $error")
                            _loginState.update {
                                it.copy(loadState = LoadState.Failure("Login failed: $error"))
                            }
                        }
                    )
                }
            } else {
                _loginState.update {
                    it.copy(loadState = LoadState.Failure("Login failed: Failed to retrieve device FCM token."))
                }
            }
        }
    }

    fun updateUserId(userId: String) {
        _loginState.update { it.copy(userId = userId) }
    }

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

}

sealed class LoadState() {
    data object Loading : LoadState()
    class Failure(val message: String) : LoadState()
    data object None : LoadState()
}

data class LoginState(
    val userId: String = "",
    val loadState: LoadState = LoadState.None,
)