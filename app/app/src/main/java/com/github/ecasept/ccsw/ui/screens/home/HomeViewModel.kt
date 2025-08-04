package com.github.ecasept.ccsw.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import com.github.ecasept.ccsw.network.createAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val dataStore: PreferencesDataStore = PreferencesDataStore(application)
) : AndroidViewModel(application) {

    private val apiClient = createAPI(dataStore)

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    fun logout(navigate: () -> Unit) {
        viewModelScope.launch {
            dataStore.updateUserId(null)
            navigate()
        }
    }

    private fun getHomeData() {
        viewModelScope.launch {
            _homeUiState.update { it.copy(loadState = LoadState.Loading) }

            val userId = runBlocking {
                dataStore.prefs.first().userId
            }
            if (userId.isNullOrEmpty()) {
                _homeUiState.update { it.copy(loadState = LoadState.Failure("You are not logged in.")) }
                return@launch
            }
            val response = apiClient.getGoodHistory(userId, limit = 5, offset = 0)
            if (!response.isSuccessful) {
                _homeUiState.update {
                    it.copy(
                        loadState = LoadState.Failure(
                            "Failed to load home data: ${
                                response.errorBody()?.string()
                            }"
                        )
                    )
                }
            } else {
                val data = response.body() ?: emptyList()
                _homeUiState.update { it.copy(loadState = LoadState.Loaded(data)) }
            }
        }
    }


}

sealed class LoadState() {
    data object Loading : LoadState()
    class Failure(val message: String) : LoadState()
    class Loaded(val data: List<GoodHistory>) : LoadState()
}

data class HomeUiState(
    val loadState: LoadState = LoadState.Loading,
)