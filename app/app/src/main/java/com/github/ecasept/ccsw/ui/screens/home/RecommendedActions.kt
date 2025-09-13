package com.github.ecasept.ccsw.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ecasept.ccsw.data.Action
import com.github.ecasept.ccsw.data.ActionType
import com.github.ecasept.ccsw.data.ActionsSnapshot
import com.github.ecasept.ccsw.data.getGood
import com.github.ecasept.ccsw.data.isMissed
import com.github.ecasept.ccsw.ui.theme.Loss
import com.github.ecasept.ccsw.ui.theme.Profit
import com.github.ecasept.ccsw.utils.formatPrice
import com.github.ecasept.ccsw.utils.formatRelativeAgo
import java.time.OffsetDateTime

@Composable
fun RecommendedActions(
    modifier: Modifier = Modifier,
    loadState: LoadState<ActionsSnapshot>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (loadState) {
            is LoadState.Loading -> {
                Title()
                LoadingState(Modifier.padding(16.dp))
            }

            is LoadState.Failure -> {
                Title()
                ErrorState(loadState.message, Modifier.padding(16.dp))
            }

            is LoadState.Loaded -> {
                val first = loadState.data.firstOrNull()
                if (first == null) {
                    Title()
                    EmptyState(Modifier.padding(16.dp))
                } else {
                    Title(date = first.createdAt)
                    LoadedState(first)
                }
            }
        }
    }
}

@Composable
fun Title(
    date: OffsetDateTime? = null,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Recommended Actions",
            style = MaterialTheme.typography.headlineSmall,
        )
        date?.let {
            val now = OffsetDateTime.now()
            val diff = now.toEpochSecond() - it.toEpochSecond()
            Text(
                text = formatRelativeAgo(diff),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            "Loading..."
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(28.dp)
        )
        Text(
            "Couldn't load recommendations",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadedState(
    actions: ActionsSnapshot,
    modifier: Modifier = Modifier,
) {
    if (actions.actions.isEmpty()) {
        EmptyState(modifier)
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Don't show missed actions in the list
            actions.actions.filter { !it.type.isMissed() }.forEach { action ->
                val good = getGood(action.symbol) ?: return@forEach

                val isBuy = action.type == ActionType.BUY || action.type == ActionType.STILL_BUY

                val valueColor = if (isBuy) Loss else Profit
                val valueStr = formatPrice(action.value)
                val thresholdStr = formatPrice(action.threshold)
                val actionStr = if (isBuy) "Buy" else "Sell"
                val actionIcon =
                    if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

                item(key = good.symbol) {
                    Card(
                        border = BorderStroke(1.dp, valueColor.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            Modifier
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(good.res),
                                contentDescription = "Good Icon",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = action.symbol,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = actionIcon,
                                        contentDescription = null,
                                        tint = valueColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = actionStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = valueColor
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = valueStr,
                                    color = valueColor,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = "past $thresholdStr",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            "You're all set",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            "Wait for the stock market prices to change for new recommendations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(apiLevel = 34, showBackground = true)
@Composable
fun RecommendedActionsPreview() {
    Column {
        RecommendedActions(
            loadState = LoadState.Loaded(
                listOf(
                    ActionsSnapshot(
                        listOf(
                            Action(
                                symbol = "CRL",
                                value = 41.24,
                                threshold = 32.01,
                                type = ActionType.STILL_SELL
                            )
                        ), OffsetDateTime.now().minusMinutes(10)
                    )
                )
            )
        )
        RecommendedActions(
            loadState = LoadState.Loading()
        )
        RecommendedActions(
            loadState = LoadState.Failure("Network error")
        )
        RecommendedActions(
            loadState = LoadState.Loaded(emptyList())
        )
    }
}
