package com.example.daypilot_test_desing.core.ui.components.cards

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotDivider
import com.example.daypilot_test_desing.core.ui.components.basic.StatsBreakdownRow
import com.example.daypilot_test_desing.core.ui.components.basic.StatsTopBlock
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Composable
fun StatsCard(
    rankingPosition: Int,
    pointsToday: Int,
    pointsFromTasks: Int,
    pointsFromSteps: Int,
    pointsFromHabits: Int,
    pointsFromTimers: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Text(
                    text = stringResource(R.string.stats_daily_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsTopBlock(
                        icon = Icons.Default.EmojiEvents,
                        label = stringResource(R.string.stats_ranking),
                        value = "#$rankingPosition",
                        modifier = Modifier.weight(1f)
                    )
                    StatsTopBlock(
                        icon = Icons.Default.Star,
                        label = stringResource(R.string.stats_points_today),
                        value = pointsToday.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                DayPilotDivider()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatsBreakdownRow(
                        icon = Icons.Default.CheckCircle,
                        label = stringResource(R.string.stats_label_tasks),
                        points = pointsFromTasks
                    )
                    StatsBreakdownRow(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = stringResource(R.string.stats_label_steps),
                        points = pointsFromSteps
                    )
                    StatsBreakdownRow(
                        icon = Icons.Default.Favorite,
                        label = stringResource(R.string.stats_label_habits),
                        points = pointsFromHabits
                    )
                    StatsBreakdownRow(
                        icon = Icons.Default.Timer,
                        label = stringResource(R.string.stats_label_timer),
                        points = pointsFromTimers
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            StatsCard(
                rankingPosition = 2,
                pointsToday = 8,
                pointsFromTasks = 4,
                pointsFromSteps = 2,
                pointsFromHabits = 1,
                pointsFromTimers = 1
            )
        }
    }
}