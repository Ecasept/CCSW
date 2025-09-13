package com.github.ecasept.ccsw.ui.screens.home.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ecasept.ccsw.R
import com.github.ecasept.ccsw.data.Good
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.data.GoodHistoryEntry
import com.github.ecasept.ccsw.ui.theme.CCSWTheme
import com.github.ecasept.ccsw.ui.theme.Loss
import com.github.ecasept.ccsw.ui.theme.Profit
import com.github.ecasept.ccsw.utils.d
import com.github.ecasept.ccsw.utils.formatPrice
import com.github.ecasept.ccsw.utils.h
import com.github.ecasept.ccsw.utils.m
import com.github.ecasept.ccsw.utils.mo
import com.github.ecasept.ccsw.utils.w
import com.github.ecasept.ccsw.utils.y
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import java.time.OffsetDateTime

@Composable
fun GoodCard(
    modifier: Modifier = Modifier,
    good: Good,
    history: GoodHistory,
    chartDataProvider: ChartDataProvider = defaultDataProvider
) {
    val isEmpty = history.isEmpty()
    val lastValue = history.lastOrNull()?.value ?: 0.0
    val priceText = if (isEmpty) "--" else formatPrice(lastValue)
    val firstValue = history.firstOrNull()?.value ?: 0.0
    val percent = if (isEmpty) 0.0 else ((lastValue / firstValue - 1) * 100)
    val isLoss = percent < 0
    val percentPrefix = if (isLoss) "" else "+" // Add a plus sign for gains
    val percentText = if (isEmpty) "N/A" else percentPrefix + "%.1f%%".format(percent)
    val color = if (isLoss) Loss else Profit

    val modelProducer = remember { CartesianChartModelProducer() }
    chartDataProvider(history, modelProducer)

    val showChart = history.size >= 2

    Card(modifier) {
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = good.res),
                    contentDescription = "Good Icon",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified,
                )
                Column(Modifier.padding(start = 8.dp)) {
                    Text(good.name, style = MaterialTheme.typography.titleMedium)
                    Text(good.symbol, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    if (isEmpty) {
                        Text(
                            text = "No history",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        return@Column
                    }
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (isLoss) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = color
                        )
                        Text(
                            text = percentText,
                            color = color,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (!showChart) {
                Text(
                    text = "No history available",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Gray
                )
            } else {
                GoodChart(Modifier.fillMaxWidth(), modelProducer, color, history)
            }
        }
    }
}

@Composable
fun PreviewGoodCard(
    good: Good,
    history: List<GoodHistoryEntry>,
    chartDataProvider: ChartDataProvider = previewDataProvider
) {
    GoodCard(
        good = good,
        history = history.sortedBy { it.timestamp },
        chartDataProvider = chartDataProvider
    )
}

private data class ExampleCard(val name: String, val points: List<Pair<Long, Int>>)

private val exampleCards = listOf(
    ExampleCard(
        name = "Short intra-minute progression",
        points = listOf(
            5.m to 95,
            4.m to 100,
            3.m to 102,
            2.m to 101,
            1.m to 105,
            0.m to 110,
        )
    ),
    ExampleCard(
        name = "30 minutes span",
        points = listOf(
            30.m to 80,
            25.m to 90,
            20.m to 120,
            15.m to 115,
            10.m to 130,
            5.m to 140,
            0.m to 150,
        )
    ),
    ExampleCard(
        name = "Several hours",
        points = listOf(
            6.h to 200,
            5.h to 210,
            4.h to 190,
            3.h to 220,
            2.h to 240,
            1.h to 230,
            30.m to 260,
            0.m to 250,
        )
    ),
    ExampleCard(
        name = "One day granularity",
        points = listOf(
            1.d to 300,
            18.h to 310,
            12.h to 305,
            8.h to 320,
            4.h to 340,
            2.h to 360,
        )
    ),
    ExampleCard(
        name = "Multiple days",
        points = listOf(
            7.d to 500,
            6.d to 480,
            5.d to 490,
            4.d to 530,
            3.d to 520,
            2.d to 560,
            1.d to 600,
            12.h to 590,
            0.m to 610,
        )
    ),
    ExampleCard(
        name = "Weeks to months/cut off",
        points = listOf(
            2.mo to 800,
            7.w to 780,
            6.w to 760,
            5.w to 820,
            4.w to 850,
//            3.w to 830,
//            2.w to 870,
//            1.w to 900,
//            0.m to 920,
        )
    ),
    ExampleCard(
        name = "Years scale",
        points = listOf(
            5.y to 1200,
            4.y to 1400,
            3.y to 1600,
            2.y to 1800,
            1.y to 1700,
            6.mo to 1900,
            3.mo to 2000,
            1.mo to 2100,
            0.m to 2050,
        )
    ),
)

@Preview(apiLevel = 34, heightDp = 2000)
@Composable
fun GoodCardPreview() {
    CCSWTheme {
        Column {
            exampleCards.forEach { card ->
                val good = Good(
                    building = "Test Building",
                    name = card.name,
                    symbol = card.name.take(3).uppercase(),
                    res = R.drawable.good_00
                )
                PreviewGoodCard(
                    good = good,
                    history = card.points.map {
                        GoodHistoryEntry(
                            OffsetDateTime.now().minusSeconds(it.first),
                            it.second.toDouble(),
                            true
                        )
                    },
                    chartDataProvider = { a, b -> PreviewGoodChartDataProvider(a, b) }
                )
            }
        }
    }
}