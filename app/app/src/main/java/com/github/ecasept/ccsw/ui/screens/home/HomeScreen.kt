package com.github.ecasept.ccsw.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMapIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionType
import com.github.ecasept.ccsw.data.Good
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.data.GoodHistoryEntry
import com.github.ecasept.ccsw.data.Snapshot
import com.github.ecasept.ccsw.data.SnapshotEntry
import com.github.ecasept.ccsw.data.getAllGoods
import com.github.ecasept.ccsw.data.getGood
import com.github.ecasept.ccsw.fcm.showActionNotification
import com.github.ecasept.ccsw.ui.components.MainTopAppBar
import com.github.ecasept.ccsw.ui.theme.CCSWTheme
import java.time.OffsetDateTime

@Composable
fun HomeScreen(
    onSettingsNav: () -> Unit, onLogoutNav: () -> Unit, viewModel: LoginViewModel = viewModel()
) {

    val state = viewModel.homeUiState.collectAsStateWithLifecycle().value

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val hideDropdownMenu = { dropdownMenuExpanded = false }
    val showDropdownMenu = { dropdownMenuExpanded = true }

    LaunchedEffect(Unit) {
        viewModel.getHomeData()
    }

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
                    { viewModel.logout(onLogoutNav) },
                    dropdownMenuExpanded
                )
            })
        },
    ) { innerPadding ->
        when (state.loadState) {
            is LoadState.Loading -> HomeScreenLoading(Modifier.padding(innerPadding))
            is LoadState.Failure -> HomeScreenError(
                state.loadState.message,
                Modifier.padding(innerPadding),
                state.loadState.isRefreshing,
                viewModel::refresh
            )

            is LoadState.Loaded -> HomeScreenContent(
                state.loadState.data,
                Modifier.padding(innerPadding),
                state.loadState.isRefreshing,
                viewModel::refresh,
            )
        }
    }
}

@Composable
fun HomeScreenLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp), color = Color.Unspecified
        )
        Text("Loading goods...")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenError(
    message: String,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(2) { i ->
                when (i) {
                    0 -> Text("Error loading goods:")
                    1 -> Text(message)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    snapshots: List<Snapshot>,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getAllGoods().fastMapIndexed { i, g -> Pair(i, g) }) { (index, good) ->
                // Get the snapshot entry for this good from every snapshot
                val history = snapshots.mapNotNull { snapshot ->
                    snapshot.goods.getOrNull(index)?.let { entry ->
                        GoodHistoryEntry(
                            timestamp = snapshot.timestamp,
                            value = entry.value,
                            bought = entry.bought
                        )
                    }
                }
                GoodCard(
                    modifier = Modifier.fillMaxWidth(),
                    good = good,
                    history = history
                )

            }
        }
    }
}

@Composable
fun GoodCard(
    modifier: Modifier = Modifier,
    good: Good,
    history: GoodHistory,
) {
    Card(modifier) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = good.res),
                contentDescription = "Good Icon",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified,
            )
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(good.symbol)
                Text("Price: ${history[0].value}$")
                Text("Bought: ${history[0].bought}%")
            }
        }
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

@Composable
fun DebugNotification(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var goodId by remember { mutableIntStateOf(0) }
    var value by remember { mutableDoubleStateOf(100.0) }
    var threshold by remember { mutableDoubleStateOf(50.0) }
    var type by remember { mutableStateOf(ActionType.SELL) }

    val good = getGood(goodId)

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = good.res),
                contentDescription = "Good Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp),
                tint = Color.Unspecified,
            )
            Text(
                text = good.symbol, modifier = Modifier.padding(8.dp)
            )
        }
        Text(buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                when (type) {
                    ActionType.SELL -> append("Sell")
                    ActionType.BUY -> append("Buy")
                }
            }
            append(" for ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$value$")
            }
            append(" after passing threshold of ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$threshold$")
            }
        })
        Row {

            Button(onClick = {
                goodId = (0..17).random()
                value = (50..150).random().toDouble()
                type = if ((0..1).random() < 0.5) ActionType.SELL else ActionType.BUY
                threshold = when (type) {
                    ActionType.SELL -> (value - (0..20).random())
                    ActionType.BUY -> (value + (0..20).random())
                }

            }) {
                Text("Generate Random values")
            }
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = {
                    showActionNotification(
                        Action(
                            goodId, value, threshold, type
                        ), (0..1000000).random().toString(), context
                    )
                },
            ) {
                Text("Show notification")
            }

        }
    }
}


@Preview(apiLevel = 34, showBackground = true)
@Composable
fun HomeScreenContentPreview() {
    CCSWTheme {
        HomeScreenContent(
            snapshots = listOf(Snapshot(
                timestamp = OffsetDateTime.now(),
                goods = getAllGoods().map {
                    SnapshotEntry(value = 200.0, bought = true)
                }
            )
            ),
            modifier = Modifier.fillMaxSize(),
            isRefreshing = false,
            onRefresh = {}
        )
    }
}

@Preview(apiLevel = 34)
@Composable
fun Test() {
    Text("abc")
}