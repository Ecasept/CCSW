package com.github.ecasept.ccsw.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.ui.components.MainTopAppBar
import com.github.ecasept.ccsw.ui.screens.home.history.History
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onSettingsNav: () -> Unit, onLogoutNav: () -> Unit, viewModel: HomeViewModel = koinViewModel()
) {

    val state = viewModel.homeUiState.collectAsStateWithLifecycle().value

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val hideDropdownMenu = { dropdownMenuExpanded = false }
    val showDropdownMenu = { dropdownMenuExpanded = true }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    HomeScreenStateless(
        state = state,
        onLogout = { viewModel.logout(onLogoutNav) },
        onSettingsNav = onSettingsNav,
        showDropdownMenu = showDropdownMenu,
        hideDropdownMenu = hideDropdownMenu,
        dropdownMenuExpanded = dropdownMenuExpanded,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun HomeScreenStateless(
    state: HomeUiState,
    onLogout: () -> Unit,
    onSettingsNav: () -> Unit,
    showDropdownMenu: () -> Unit,
    hideDropdownMenu: () -> Unit,
    dropdownMenuExpanded: Boolean,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            MainTopAppBar(title = "CCSW", actions = {
                IconButton(showDropdownMenu) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, contentDescription = "Options"
                    )
                }
                HomeDropdownMenu(
                    hideDropdownMenu,
                    onSettingsNav,
                    onLogout,
                    dropdownMenuExpanded
                )
            })
        },
    ) { innerPadding ->
        HomeScreenContent(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        History(
            historyLoadState = state.historyLoadState,
            recommendedActions = {
                RecommendedActions(
                    loadState = state.actionsLoadState,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun HomeDropdownMenu(
    onDismissRequest: () -> Unit,
    onPrefClick: () -> Unit,
    onLogoutClick: () -> Unit,
    expanded: Boolean
) {
    DropdownMenu(expanded, onDismissRequest) {
        DropdownMenuItem(text = { Text("Settings") }, onClick = {
            onDismissRequest()
            onPrefClick()
        })
        DropdownMenuItem(text = { Text("Logout") }, onClick = {
            onDismissRequest()
            onLogoutClick()
        })
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenStateless(
        state = HomeUiState(historyLoadState = LoadState.Failure("a")),
        onLogout = {},
        onSettingsNav = {},
        showDropdownMenu = {},
        hideDropdownMenu = {},
        dropdownMenuExpanded = false,
        onRefresh = {}
    )
}
