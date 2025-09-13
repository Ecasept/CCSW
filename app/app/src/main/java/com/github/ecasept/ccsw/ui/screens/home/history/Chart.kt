package com.github.ecasept.ccsw.ui.screens.home.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.ecasept.ccsw.data.GoodHistory
import com.github.ecasept.ccsw.utils.formatRelative
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import kotlinx.coroutines.runBlocking
import java.time.OffsetDateTime
import kotlin.math.max
import kotlin.math.min


private const val CHART_HEIGHT_DP = 120
private const val CHART_SPAN = 100

@Composable
fun GoodChart(
    modifier: Modifier,
    modelProducer: CartesianChartModelProducer,
    color: Color,
    history: GoodHistory,
) {
    if (history.size < 2) {
        // No data to display, return early
        return
    }
    val minValue = history.minByOrNull { it.value }?.value!!
    val maxValue = history.maxByOrNull { it.value }?.value!!

    val (startTs, endTs) = remember(history) {
        val start = history.first().timestamp.toEpochSecond()
        val end = history.last().timestamp.toEpochSecond()
        start to end
    }

    val now = remember { OffsetDateTime.now().toEpochSecond() }
    val startLabel = remember(startTs, now) { formatRelative(now - startTs) }
    val endLabel = remember(endTs, now) { formatRelative(now - endTs) }

    val rangeProvider = remember(minValue, maxValue) {
        val avg = (minValue + maxValue) / 2
        // A chart needs to span at least CHART_SPAN units vertically
        var minY = avg - CHART_SPAN / 2
        var maxY = avg + CHART_SPAN / 2
        // If that is not enough to cover the min and max values, adjust accordingly
        minY = min(minY, minValue)
        maxY = max(maxY, maxValue)

        // Move range above 0 if it is below
        if (minY < 0) {
            maxY += -minY
            minY = 0.0
        }
        CartesianLayerRangeProvider.fixed(minY = minY, maxY = maxY)
    }

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        CartesianChartHost(
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill(color)),
                            areaFill = LineCartesianLayer.AreaFill.single(
                                fill(
                                    ShaderProvider.verticalGradient(
                                        arrayOf(
                                            color.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            ),
                        )
                    ),
                    rangeProvider = rangeProvider
                ),
                startAxis = null, //VerticalAxis.rememberStart(),
                bottomAxis = null,
                marker = null,
            ),
            modelProducer,
            Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT_DP.dp),
            rememberVicoScrollState(scrollEnabled = false)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startLabel,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = endLabel.ifBlank { "now" },
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun GoodChartDataProvider(
    goodHistory: GoodHistory,
    modelProducer: CartesianChartModelProducer
) {
    LaunchedEffect(goodHistory) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    goodHistory.map { it.timestamp.toEpochSecond() },
                    goodHistory.map { it.value }
                )
            }
        }
    }
}

@Composable
fun PreviewGoodChartDataProvider(
    goodHistory: GoodHistory,
    modelProducer: CartesianChartModelProducer
) {
    runBlocking {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    goodHistory.map { it.timestamp.toEpochSecond() },
                    goodHistory.map { it.value }
                )
            }
        }
    }
}

typealias ChartDataProvider = @Composable (GoodHistory, CartesianChartModelProducer) -> Unit

val defaultDataProvider: ChartDataProvider =
    @Composable { goodHistory, modelProducer ->
        GoodChartDataProvider(goodHistory, modelProducer)
    }

val previewDataProvider: ChartDataProvider =
    @Composable { goodHistory, modelProducer ->
        PreviewGoodChartDataProvider(goodHistory, modelProducer)
    }
