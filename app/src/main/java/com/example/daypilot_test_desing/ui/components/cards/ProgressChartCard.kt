package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.model.DayProgress
import com.example.daypilot_test_desing.ui.model.ProgressFilter
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import com.example.daypilot_test_desing.ui.components.basic.ChartSummaryItem

@Composable
fun ProgressChartCard(
    data: List<DayProgress>,
    filter: ProgressFilter,
    modifier: Modifier = Modifier
) {
    val lineColor = when (filter) {
        ProgressFilter.POINTS -> Color(0xFF4CAF50)
        ProgressFilter.STEPS  -> Color(0xFF2196F3)
        ProgressFilter.TASKS  -> Color(0xFFFF9800)
    }

    val title = when (filter) {
        ProgressFilter.POINTS -> "⭐ Puntos"
        ProgressFilter.STEPS  -> "👣 Pasos"
        ProgressFilter.TASKS  -> "✅ Tareas completadas"
    }

    val values = data.map { day ->
        when (filter) {
            ProgressFilter.POINTS -> day.points.toFloat()
            ProgressFilter.STEPS  -> day.steps.toFloat()
            ProgressFilter.TASKS  -> day.tasksCompleted.toFloat()
        }
    }

    val maxValue  = maxOf((values.maxOrNull() ?: 1f) * 1.2f, 10f)
    val total     = values.sumOf { it.toDouble() }.toInt()
    val average   = if (values.isNotEmpty()) total / values.size else 0
    val best      = values.maxOrNull()?.toInt() ?: 0

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Título ───────────────────────────────────────────
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            // ── Canvas ───────────────────────────────────────────
            AnimatedContent(
                targetState  = values,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "chart_${filter.name}"
            ) { animValues ->
                val chartMax = maxOf((animValues.maxOrNull() ?: 1f) * 1.2f, 10f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val yAxisWidth  = 36.dp.toPx()
                    val xPadding    = 8.dp.toPx()
                    val chartWidth  = size.width - yAxisWidth - xPadding
                    val chartHeight = size.height - 16.dp.toPx()
                    val barWidth    = chartWidth / animValues.size

                    // ── Eje Y ─────────────────────────────────────
                    val ySteps = 4
                    repeat(ySteps + 1) { i ->
                        val yValue = chartMax * i / ySteps
                        val yPos   = chartHeight - (chartHeight * i / ySteps)

                        drawLine(
                            color       = Color.Gray.copy(alpha = 0.15f),
                            start       = Offset(yAxisWidth, yPos),
                            end         = Offset(size.width - xPadding, yPos),
                            strokeWidth = 1.dp.toPx()
                        )

                        val yLabel = when {
                            yValue >= 1000 -> "${(yValue / 1000).toInt()}k"
                            else           -> yValue.toInt().toString()
                        }
                        val textLayout = textMeasurer.measure(
                            text  = AnnotatedString(yLabel),
                            style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft          = Offset(
                                x = yAxisWidth - textLayout.size.width - 4.dp.toPx(),
                                y = yPos - textLayout.size.height / 2
                            )
                        )
                    }

                    // ── Barras fondo ──────────────────────────────
                    animValues.forEachIndexed { index, value ->
                        val barHeight = if (chartMax > 0) (value / chartMax) * chartHeight else 0f
                        val x = yAxisWidth + index * barWidth
                        drawRect(
                            color   = lineColor.copy(alpha = 0.12f),
                            topLeft = Offset(x + 1.dp.toPx(), chartHeight - barHeight),
                            size    = Size(barWidth - 2.dp.toPx(), barHeight)
                        )
                    }

                    // ── Puntos de la línea ────────────────────────
                    val points = animValues.mapIndexed { index, value ->
                        Offset(
                            x = yAxisWidth + index * barWidth + barWidth / 2,
                            y = if (chartMax > 0)
                                chartHeight - (value / chartMax) * chartHeight
                            else chartHeight
                        )
                    }

                    if (points.size >= 2) {
                        // Área rellena
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
                            path  = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.35f),
                                    lineColor.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY   = chartHeight
                            )
                        )

                        // Línea
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.zipWithNext().forEach { (p1, p2) ->
                                val cx = (p1.x + p2.x) / 2
                                cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(
                            path  = linePath,
                            color = lineColor,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap   = StrokeCap.Round,
                                join  = StrokeJoin.Round
                            )
                        )

                        // Puntos + valores cada 5
                        points.forEachIndexed { index, point ->
                            drawCircle(
                                color  = lineColor,
                                radius = 3.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color  = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = point
                            )

                            if (index % 5 == 0 || index == points.lastIndex) {
                                val label = when {
                                    animValues[index] >= 1000 ->
                                        "${(animValues[index] / 1000).toInt()}k"
                                    else -> animValues[index].toInt().toString()
                                }
                                val textLayout = textMeasurer.measure(
                                    text  = AnnotatedString(label),
                                    style = TextStyle(
                                        fontSize   = 8.sp,
                                        color      = lineColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = textLayout,
                                    topLeft          = Offset(
                                        x = point.x - textLayout.size.width / 2,
                                        y = point.y - textLayout.size.height - 4.dp.toPx()
                                    )
                                )
                            }
                        }
                    }

                    // ── Eje X — etiquetas días ────────────────────
                    animValues.forEachIndexed { index, _ ->
                        if (index % 5 == 0 || index == animValues.lastIndex) {
                            val x = yAxisWidth + index * barWidth + barWidth / 2
                            val textLayout = textMeasurer.measure(
                                text  = AnnotatedString((index + 1).toString()),
                                style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft          = Offset(
                                    x = x - textLayout.size.width / 2,
                                    y = chartHeight + 4.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // ── Stats resumen ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartSummaryItem(label = "Total", value = total.toString())
                ChartSummaryItem(label = "Media", value = average.toString())
                ChartSummaryItem(label = "Mejor", value = best.toString())
            }
        }
    }
}

@Composable
fun ProgressChartCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgressChartCard(
                    data   = List(30) { index ->
                        DayProgress(index + 1, (5..25).random(), (500..3000).random(), (0..8).random())
                    },
                    filter = ProgressFilter.POINTS
                )
            }
        }
    }
}