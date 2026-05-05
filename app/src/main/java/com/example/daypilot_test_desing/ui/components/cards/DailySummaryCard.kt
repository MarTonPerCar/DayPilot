package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import com.example.daypilot_test_desing.ui.components.basic.DailySummaryStat

@Composable
fun DailySummaryCard(
    userName: String,
    streak: Int,
    stepsToday: Int,
    stepsGoal: Int,
    tasksCompleted: Int,
    tasksTotal: Int,
    pointsToday: Int,
    rankingPosition: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // ── Cabecera ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "¡Hola, $userName!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Resumen de hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Racha
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🔥",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$streak días",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Grid de stats ─────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailySummaryStat(
                        icon = Icons.Default.DirectionsWalk,
                        label = "Pasos",
                        value = stepsToday.toString(),
                        subValue = "meta $stepsGoal",
                        modifier = Modifier.weight(1f)
                    )
                    DailySummaryStat(
                        icon = Icons.Default.CheckCircle,
                        label = "Tareas",
                        value = "$tasksCompleted/$tasksTotal",
                        subValue = "completadas",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailySummaryStat(
                        icon = Icons.Default.Star,
                        label = "Puntos",
                        value = pointsToday.toString(),
                        subValue = "hoy",
                        modifier = Modifier.weight(1f)
                    )
                    DailySummaryStat(
                        icon = Icons.Default.EmojiEvents,
                        label = "Ranking",
                        value = "#$rankingPosition",
                        subValue = "entre amigos",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DailySummaryCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            DailySummaryCard(
                userName = "Mario",
                streak = 7,
                stepsToday = 1200,
                stepsGoal = 2000,
                tasksCompleted = 3,
                tasksTotal = 5,
                pointsToday = 8,
                rankingPosition = 2
            )
        }
    }
}