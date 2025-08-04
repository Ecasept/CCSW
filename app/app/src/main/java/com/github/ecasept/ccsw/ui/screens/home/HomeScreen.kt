package com.github.ecasept.ccsw.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionType
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.data.getGood
import com.github.ecasept.ccsw.fcm.showActionNotification
import com.github.ecasept.ccsw.ui.components.MainTopAppBar

@Composable
fun HomeScreen(
    onSettingsNav: () -> Unit,
    onLogoutNav: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {

    val state = viewModel.homeUiState.collectAsStateWithLifecycle().value

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val hideDropdownMenu = { dropdownMenuExpanded = false }
    val showDropdownMenu = { dropdownMenuExpanded = true }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "CCSW",
                actions = {
                    IconButton(showDropdownMenu) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                    HomeDropdownMenu(
                        hideDropdownMenu,
                        onSettingsNav,
                        { viewModel.logout(onLogoutNav) },
                        dropdownMenuExpanded
                    )
                }
            )
        },
    ) { innerPadding ->
        when (state.loadState) {
            is LoadState.Loading -> HomeScreenLoading(Modifier.padding(innerPadding))
            is LoadState.Failure -> HomeScreenError(
                state.loadState.message,
                Modifier.padding(innerPadding)
            )

            is LoadState.Loaded -> HomeScreenContent(
                state.loadState.data,
                Modifier.padding(innerPadding),
                viewModel::getHomeData
            )
        }
    }
}

@Composable
fun HomeScreenLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.Unspecified
        )
        Text("Loading goods...")
    }
}

@Composable
fun HomeScreenError(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error loading goods:")
        Text(message)
    }
}

@Composable
fun HomeScreenContent(
    data: List<GoodHistory>,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Recent Good History", fontWeight = FontWeight.Bold)
        if (data.isEmpty()) {
            Text("No recent good history available.")
        } else {
            data.forEach { history ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        painter = painterResource(id = history.good.res),
                        contentDescription = "Good Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Text("${history.good.symbol}: ${history.value}$")
                }
            }
        }
        Button(onClick = onRefresh) {
            Text("Refresh")
        }
        DebugNotification()
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
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onDismissRequest()
                onPrefClick()
            }
        )
        DropdownMenuItem(
            text = { Text("Logout") },
            onClick = {
                onDismissRequest()
                onLogoutClick()
            }
        )
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
                text = good.symbol,
                modifier = Modifier.padding(8.dp)
            )
        }
        Text(
            buildAnnotatedString {
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
            }
        )
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
                            goodId,
                            value,
                            threshold,
                            type
                        ),
                        (0..1000000).random().toString(),
                        context
                    )
                },
            ) {
                Text("Show notification")
            }

        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen({}, {})
}