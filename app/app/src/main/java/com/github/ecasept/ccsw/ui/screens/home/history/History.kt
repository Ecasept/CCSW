package com.github.ecasept.ccsw.ui.screens.home.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.ecasept.ccsw.data.GoodHistoryEntry
import com.github.ecasept.ccsw.data.Snapshot
import com.github.ecasept.ccsw.data.getAllGoods
import com.github.ecasept.ccsw.ui.screens.home.LoadState

@Composable
private fun HistoryTitle() {
    Text(
        text = "History",
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
fun History(
    historyLoadState: LoadState<Snapshot>,
    recommendedActions: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    when (historyLoadState) {
        is LoadState.Loading -> HistoryLoading(recommendedActions, modifier)
        is LoadState.Failure -> HistoryError(historyLoadState.message, recommendedActions, modifier)
        is LoadState.Loaded -> HistoryContent(historyLoadState.data, recommendedActions, modifier)
    }
}

@Composable
fun HistoryLoading(
    recommendedActions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scrollable container for PullToRefreshBox
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        recommendedActions()
        HistoryTitle()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text("Loading goods...")
        }
    }
}

@Composable
fun HistoryError(
    message: String,
    recommendedActions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scrollable container for PullToRefreshBox
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        recommendedActions()
        HistoryTitle()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                "Error loading history",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoryContent(
    snapshots: List<Snapshot>,
    recommendedActions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item(key = "top-spacer") { Spacer(Modifier.height(16.dp)) }
        item(key = "recommended-actions") { recommendedActions() }
        item(key = "history-title") { HistoryTitle() }
        items(getAllGoods(), key = { it.symbol }) { good ->
            val history = snapshots.mapNotNull { snapshot ->
                snapshot.goods[good.symbol]?.let { entry ->
                    GoodHistoryEntry(
                        timestamp = snapshot.timestamp,
                        value = entry.value,
                        bought = entry.bought
                    )
                }
            }.sortedBy { it.timestamp }
            GoodCard(
                modifier = Modifier.fillMaxWidth(),
                good = good,
                history = history
            )
        }
    }
}
