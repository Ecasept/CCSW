package com.github.ecasept.ccsw.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ecasept.ccsw.data.ActionsSnapshot
import com.github.ecasept.ccsw.data.Snapshot
import com.github.ecasept.ccsw.data.preferences.PDSRepo
import com.github.ecasept.ccsw.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class HomeViewModel(
    private val dataStore: PDSRepo,
    private val apiClient: ApiClient
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState = _homeUiState.asStateFlow()

    fun logout(navigate: () -> Unit) {
        viewModelScope.launch {
            dataStore.logout()
            navigate()
        }
    }

    fun refresh() {
        loadAll(isRefresh = true)
    }

    /** Loads the history and action data from the API.
     * If [isRefresh] is true, it will update the UI state to indicate a refresh is in progress.
     * Otherwise, it will set the load state to Loading.
     */
    fun loadAll(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _homeUiState.update { old ->
                    old.copy(isRefreshing = true)
                }
            } else {
                _homeUiState.update {
                    it.copy(
                        historyLoadState = LoadState.Loading(),
                        actionsLoadState = LoadState.Loading(),
                    )
                }
            }

            val history = async {
                apiClient.getGoodHistory(limit = 5, offset = 0).on(
                    success = { data ->
                        _homeUiState.update {
                            it.copy(historyLoadState = LoadState.Loaded(data))
                        }
                    },
                    error = { error ->
                        _homeUiState.update {
                            it.copy(historyLoadState = LoadState.Failure(error))
                        }
                    }
                )
            }
            val actions = async {
                apiClient.getActions(limit = 1, offset = 0).on(
                    success = { data ->
                        Log.d("HomeViewModel", data.toString())
                        _homeUiState.update {
                            it.copy(actionsLoadState = LoadState.Loaded(data))
                        }
                    },
                    error = { error ->
                        _homeUiState.update {
                            it.copy(actionsLoadState = LoadState.Failure(error))
                        }
                    }
                )
            }
            history.await()
            actions.await()
            if (isRefresh) {
                _homeUiState.update { old ->
                    old.copy(isRefreshing = false)
                }
            }
        }
    }
}

sealed class LoadState<T> {
    data class Loading<T>(val doNotUse: Int = 0) : LoadState<T>()
    data class Failure<T>(val message: String) : LoadState<T>()
    data class Loaded<T>(val data: List<T>) : LoadState<T>()
}

data class HomeUiState(
    val historyLoadState: LoadState<Snapshot> = LoadState.Loading(),
    val actionsLoadState: LoadState<ActionsSnapshot> = LoadState.Loading(),
    val isRefreshing: Boolean = false,
)
