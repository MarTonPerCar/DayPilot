package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.ChartSummaryItem
import com.example.daypilot_test_desing.core.data.model.DayProgress
import com.example.daypilot_test_desing.core.data.model.ProgressFilter
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

private fun chartLineColor(filter: ProgressFilter): Color = when (filter) {
    ProgressFilter.POINTS -> Color(0xFF4CAF50)
    ProgressFilter.STEPS -> Color(0xFF2196F3)
    ProgressFilter.TASKS -> Color(0xFFFF9800)
}

private fun chartValues(data: List<DayProgress>, filter: ProgressFilter): List<Float> = data.map { day ->
    when (filter) {
        ProgressFilter.POINTS -> day.points.toFloat()
        ProgressFilter.STEPS -> day.steps.toFloat()
        ProgressFilter.TASKS -> day.tasksCompleted.toFloat()
    }
}

private fun compactLabel(value: Float): String =
    if (value >= 1000) "${(value / 1000).toInt()}k" else value.toInt().toString()

private fun chartPoints(values: List<Float>, chartMax: Float, chartHeight: Float, yAxisWidth: Float, barWidth: Float): List<Offset> =
    values.mapIndexed { index, value ->
        Offset(
            x = yAxisWidth + index * barWidth + barWidth / 2,
            y = if (chartMax > 0) chartHeight - (value / chartMax) * chartHeight else chartHeight
        )
    }

private fun DrawScope.drawYAxisGridLines(
    chartMax: Float,
    chartHeight: Float,
    yAxisWidth: Float,
    xPadding: Float,
    textMeasurer: TextMeasurer
) {
    val ySteps = 4
    repeat(ySteps + 1) { i ->
        val yValue = chartMax * i / ySteps
        val yPos = chartHeight - (chartHeight * i / ySteps)

        drawLine(
            color = Color.Gray.copy(alpha = 0.15f),
            start = Offset(yAxisWidth, yPos),
            end = Offset(size.width - xPadding, yPos),
            strokeWidth = 1.dp.toPx()
        )

        val textLayout = textMeasurer.measure(
            text = AnnotatedString(compactLabel(yValue)),
            style = TextStyle(fontSize = 8.sp, color = Color.Gray)
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = yAxisWidth - textLayout.size.width - 4.dp.toPx(),
                y = yPos - textLayout.size.height / 2
            )
        )
    }
}

private fun DrawScope.drawValueBars(
    values: List<Float>,
    chartMax: Float,
    chartHeight: Float,
    yAxisWidth: Float,
    barWidth: Float,
    lineColor: Color
) {
    values.forEachIndexed { index, value ->
        val barHeight = if (chartMax > 0) (value / chartMax) * chartHeight else 0f
        val x = yAxisWidth + index * barWidth
        drawRect(
            color = lineColor.copy(alpha = 0.12f),
            topLeft = Offset(x + 1.dp.toPx(), chartHeight - barHeight),
            size = Size(barWidth - 2.dp.toPx(), barHeight)
        )
    }
}

private fun DrawScope.drawTodayMarker(
    todayIndex: Int,
    values: List<Float>,
    chartMax: Float,
    chartHeight: Float,
    yAxisWidth: Float,
    barWidth: Float,
    lineColor: Color
) {
    if (todayIndex < 0 || todayIndex >= values.size) return
    val todayX   = yAxisWidth + todayIndex * barWidth + barWidth / 2
    val todayVal = values[todayIndex]
    val topY     = if (chartMax > 0) chartHeight - (todayVal / chartMax) * chartHeight else chartHeight
    drawLine(
        color       = lineColor.copy(alpha = 0.75f),
        start       = Offset(todayX, chartHeight),
        end         = Offset(todayX, topY),
        strokeWidth = 2.5.dp.toPx()
    )
}

private fun DrawScope.drawTrendFill(points: List<Offset>, chartHeight: Float, lineColor: Color) {
    val fillPath = Path().apply {
        moveTo(points.first().x, chartHeight)
        lineTo(points.first().x, points.first().y)
        points.zipWithNext().forEach { (p1, p2) ->
            val cx = (p1.x + p2.x) / 2
            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
        }
        lineTo(points.last().x, chartHeight)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.35f),
                lineColor.copy(alpha = 0.02f)
            ),
            startY = 0f,
            endY = chartHeight
        )
    )
}

private fun DrawScope.drawTrendLine(points: List<Offset>, lineColor: Color) {
    val linePath = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.zipWithNext().forEach { (p1, p2) ->
            val cx = (p1.x + p2.x) / 2
            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
        }
    }
    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawTrendPoints(points: List<Offset>, values: List<Float>, lineColor: Color, textMeasurer: TextMeasurer) {
    points.forEachIndexed { index, point ->
        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = point)
        drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = point)

        if (index % 5 != 0 && index != points.lastIndex) return@forEachIndexed
        val textLayout = textMeasurer.measure(
            text = AnnotatedString(compactLabel(values[index])),
            style = TextStyle(fontSize = 8.sp, color = lineColor, fontWeight = FontWeight.Bold)
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = point.x - textLayout.size.width / 2,
                y = point.y - textLayout.size.height - 4.dp.toPx()
            )
        )
    }
}

private fun DrawScope.drawTrend(points: List<Offset>, values: List<Float>, chartHeight: Float, lineColor: Color, textMeasurer: TextMeasurer) {
    if (points.size < 2) return
    drawTrendFill(points, chartHeight, lineColor)
    drawTrendLine(points, lineColor)
    drawTrendPoints(points, values, lineColor, textMeasurer)
}

private data class ChartGeometry(val chartHeight: Float, val yAxisWidth: Float, val barWidth: Float)

// Position-based, not dayOfMonth-based — dayOfMonth rolls over a month boundary.
private fun DrawScope.drawXAxisLabels(
    values: List<Float>,
    todayIndex: Int,
    dayLabels: List<Int>,
    geometry: ChartGeometry,
    lineColor: Color,
    textMeasurer: TextMeasurer
) {
    val (chartHeight, yAxisWidth, barWidth) = geometry
    values.forEachIndexed { index, _ ->
        val showLabel = index % 5 == 4 || index == todayIndex
        if (!showLabel) return@forEachIndexed

        val x = yAxisWidth + index * barWidth + barWidth / 2
        val label = if (index < dayLabels.size) dayLabels[index].toString() else (index + 1).toString()
        val isToday = index == todayIndex
        val labelColor = if (isToday) lineColor else Color.Gray
        val textLayout = textMeasurer.measure(
            text = AnnotatedString(label),
            style = TextStyle(
                fontSize = 8.sp,
                color = labelColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        )
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = x - textLayout.size.width / 2,
                y = chartHeight + 4.dp.toPx()
            )
        )
    }
}

@Composable
private fun ProgressChart(
    values: List<Float>,
    todayIndex: Int,
    dayLabels: List<Int>,
    lineColor: Color,
    textMeasurer: TextMeasurer
) {
    AnimatedContent(
        targetState = values,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        label = "chart"
    ) { animValues ->
        val chartMax = maxOf((animValues.maxOrNull() ?: 1f) * 1.2f, 10f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val yAxisWidth = 36.dp.toPx()
            val xPadding = 8.dp.toPx()
            val chartWidth = size.width - yAxisWidth - xPadding
            val chartHeight = size.height - 16.dp.toPx()
            val barWidth = chartWidth / animValues.size

            drawYAxisGridLines(chartMax, chartHeight, yAxisWidth, xPadding, textMeasurer)
            drawValueBars(animValues, chartMax, chartHeight, yAxisWidth, barWidth, lineColor)
            drawTodayMarker(todayIndex, animValues, chartMax, chartHeight, yAxisWidth, barWidth, lineColor)

            val points = chartPoints(animValues, chartMax, chartHeight, yAxisWidth, barWidth)
            drawTrend(points, animValues, chartHeight, lineColor, textMeasurer)

            drawXAxisLabels(
                animValues, todayIndex, dayLabels,
                ChartGeometry(chartHeight, yAxisWidth, barWidth), lineColor, textMeasurer
            )
        }
    }
}

@Composable
private fun ChartSummaryRow(total: Int, average: Int, best: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ChartSummaryItem(
            label = stringResource(R.string.progress_chart_total),
            value = total.toString()
        )
        ChartSummaryItem(
            label = stringResource(R.string.progress_chart_average),
            value = average.toString()
        )
        ChartSummaryItem(
            label = stringResource(R.string.progress_chart_best),
            value = best.toString()
        )
    }
}

@Composable
fun ProgressChartCard(
    data: List<DayProgress>,
    filter: ProgressFilter,
    modifier: Modifier = Modifier
) {
    val lineColor = chartLineColor(filter)

    val title = when (filter) {
        ProgressFilter.POINTS -> stringResource(R.string.progress_chart_points)
        ProgressFilter.STEPS -> stringResource(R.string.progress_chart_steps)
        ProgressFilter.TASKS -> stringResource(R.string.progress_chart_tasks)
    }

    val values = chartValues(data, filter)

    val total = values.sumOf { it.toDouble() }.toInt()
    val average = if (values.isNotEmpty()) total / values.size else 0
    val best = values.maxOrNull()?.toInt() ?: 0

    val textMeasurer = rememberTextMeasurer()
    val todayIndex   = data.indexOfFirst { it.isToday }
    val dayLabels    = data.map { it.dayOfMonth }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            ProgressChart(
                values = values,
                todayIndex = todayIndex,
                dayLabels = dayLabels,
                lineColor = lineColor,
                textMeasurer = textMeasurer
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            ChartSummaryRow(total = total, average = average, best = best)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressChartCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ProgressChartCard(
                data = List(30) { index ->
                    DayProgress(
                        dayOfMonth = (index % 31) + 1,
                        points = (5..25).random(),
                        steps = (500..3000).random(),
                        tasksCompleted = (0..8).random()
                    )
                },
                filter = ProgressFilter.POINTS
            )
        }
    }
}
