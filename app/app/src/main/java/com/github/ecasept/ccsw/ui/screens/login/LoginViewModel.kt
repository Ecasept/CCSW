package com.github.ecasept.ccsw.ui.screens.login

import android.app.Application
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.preferences.PDSRepo
import com.github.ecasept.ccsw.network.ApiClient
import com.github.ecasept.ccsw.network.createAPI
import com.github.ecasept.ccsw.ui.components.toTextFieldValue
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class LoginViewModel (
    private val dataStore: PDSRepo,
    private val apiClient: ApiClient,
) : ViewModel() {

    fun login(navigate: () -> Unit) {
        _loginState.update { it.copy(loadState = LoadState.Loading) }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                val instanceId = _loginState.value.instanceId
                val accessCode = _loginState.value.accessCode
                val serverUrl = _loginState.value.serverUrl.text
                viewModelScope.launch {
                    // Update server URL in DataStore
                    dataStore.updateServerUrl(serverUrl)

                    val result = apiClient.createSession(
                        instanceId,
                        accessCode
                    )
                    result.onSuspend(
                        success = { sessionToken ->
                            addFCMToken(
                                fcmToken = fcmToken,
                                instanceId = instanceId,
                                sessionToken = sessionToken,
                                onSuccess = navigate
                            )
                        },
                        error = { error ->
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

    private suspend fun addFCMToken(
        fcmToken: String,
        instanceId: String,
        sessionToken: String,
        onSuccess: () -> Unit
    ) {
        val result = apiClient.addDeviceToken(fcmToken, instanceId, sessionToken)
        result.onSuspend(
            success = {
                dataStore.updateInstanceId(instanceId)
                dataStore.updateSessionToken(sessionToken)
                _loginState.update { it.copy(loadState = LoadState.None) }
                onSuccess()
            },
            error = { error ->
                _loginState.update {
                    it.copy(loadState = LoadState.Failure("Login failed: $error"))
                }
            }
        )
    }

    fun updateInstanceId(instanceId: String) {
        _loginState.update { it.copy(instanceId = instanceId) }
    }

    fun updateAccessCode(accessCode: String) {
        _loginState.update { it.copy(accessCode = accessCode) }
    }

    fun updateServerUrl(serverUrl: TextFieldValue) {
        _loginState.update { it.copy(serverUrl = serverUrl) }
    }

    fun resetServerUrl() {
        _loginState.update { it.copy(serverUrl = defaultServerUrl.toTextFieldValue()) }
    }

    private val tmp = viewModelScope.launch {
        dataStore.prefs.collect { prefs ->
            defaultServerUrl = prefs.serverUrl
            if (!hasFilledDefaultServerUrl) {
                // Put the default server URL into the text field at the first time
                hasFilledDefaultServerUrl = true
                _loginState.update { it.copy(serverUrl = defaultServerUrl.toTextFieldValue()) }
            }
        }
    }

    private var defaultServerUrl = ""
    private var hasFilledDefaultServerUrl = false

    private val _loginState = MutableStateFlow(LoginState(serverUrl = TextFieldValue("")))
    val loginState = _loginState.asStateFlow()
    val isServerUrlDirty = loginState.map { it.serverUrl.text != defaultServerUrl }
}

sealed class LoadState() {
    data object Loading : LoadState()
    data class Failure(val message: String) : LoadState()
    data object None : LoadState()
}


data class LoginState(
    val instanceId: String = "",
    val accessCode: String = "",
    val loadState: LoadState = LoadState.None,
    val serverUrl: TextFieldValue,
)