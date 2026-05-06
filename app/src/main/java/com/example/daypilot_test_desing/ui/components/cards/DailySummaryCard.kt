package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DailySummaryStat
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

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

                // ── Cabecera ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.daily_summary_hello, userName),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.daily_summary_subtitle),
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
                            text = stringResource(R.string.daily_summary_streak_days, streak),
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
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = stringResource(R.string.daily_summary_label_steps),
                        value = stepsToday.toString(),
                        subValue = stringResource(R.string.daily_summary_steps_goal, stepsGoal),
                        modifier = Modifier.weight(1f)
                    )
                    DailySummaryStat(
                        icon = Icons.Default.CheckCircle,
                        label = stringResource(R.string.daily_summary_label_tasks),
                        value = "$tasksCompleted/$tasksTotal",
                        subValue = stringResource(R.string.daily_summary_tasks_completed),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailySummaryStat(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.daily_summary_label_points),
                        value = pointsToday.toString(),
                        subValue = stringResource(R.string.daily_summary_points_today),
                        modifier = Modifier.weight(1f)
                    )
                    DailySummaryStat(
                        icon = Icons.Default.EmojiEvents,
                        label = stringResource(R.string.daily_summary_label_ranking),
                        value = "#$rankingPosition",
                        subValue = stringResource(R.string.daily_summary_ranking_among),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
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