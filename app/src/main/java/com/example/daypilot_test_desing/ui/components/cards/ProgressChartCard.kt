package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

@Composable
fun ProgressChartCard(
    data: List<Pair<String, Int>>,
    title: String = "Progreso 30 días",
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Cabecera ─────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Gráfica de barras ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (label, value) ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = if (maxValue > 0) value.toFloat() / maxValue else 0f,
                        animationSpec = tween(
                            durationMillis = 800,
                            easing = EaseOutCubic
                        ),
                        label = "bar_$label"
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Barra
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // ── Etiquetas eje X ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                data.forEach { (label, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // ── Stats resumen ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val total   = data.sumOf { it.second }
                val average = if (data.isNotEmpty()) total / data.size else 0
                val best    = data.maxOfOrNull { it.second } ?: 0

                ChartSummaryItem(label = "Total",  value = total.toString())
                ChartSummaryItem(label = "Media",  value = average.toString())
                ChartSummaryItem(label = "Mejor",  value = best.toString())
            }
        }
    }
}

@Composable
fun ChartSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                data = listOf(
                    "L" to 12, "M" to 8,  "X" to 15, "J" to 6,
                    "V" to 20, "S" to 18, "D" to 10, "L" to 14,
                    "M" to 9,  "X" to 17, "J" to 11, "V" to 22,
                    "S" to 16, "D" to 7
                )
            )
        }
    }
}