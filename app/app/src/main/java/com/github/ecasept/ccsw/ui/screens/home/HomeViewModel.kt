package com.github.ecasept.ccsw.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.Snapshot
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
            dataStore.logout()
            navigate()
        }
    }

    fun refresh() {
        getHomeData(isRefresh = true)
    }

    private fun startRefresh() {
        _homeUiState.update { old ->
            when (old.loadState) {
                is LoadState.Loading -> old.copy(
                    loadState = LoadState.Failure("Can't refresh while loading, how did you even manage to do this???")
                )

                is LoadState.Failure -> old.copy(
                    loadState = old.loadState.copy(
                        isRefreshing = true
                    )
                )

                is LoadState.Loaded -> old.copy(
                    loadState = old.loadState.copy(
                        isRefreshing = true
                    )
                )
            }
        }
    }

    fun getHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                startRefresh()

            } else {
                _homeUiState.update {
                    it.copy(loadState = LoadState.Loading)
                }
            }

            apiClient.getGoodHistory(limit = 5, offset = 0).on(
                success = { data ->
                    Log.d("HomeViewModel", data.toString())
                    _homeUiState.update {
                        it.copy(loadState = LoadState.Loaded(data))
                    }
                },
                error = { error ->
                    _homeUiState.update {
                        it.copy(loadState = LoadState.Failure(error))
                    }
                }
            )
        }
    }
}

sealed class LoadState() {
    data object Loading : LoadState()
    data class Failure(val message: String, val isRefreshing: Boolean = false) : LoadState()
    data class Loaded(val data: List<Snapshot>, val isRefreshing: Boolean = false) : LoadState()
}

data class HomeUiState(
    val loadState: LoadState = LoadState.Loading,
)
