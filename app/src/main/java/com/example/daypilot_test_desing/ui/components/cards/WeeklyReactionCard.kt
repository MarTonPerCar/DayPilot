package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotDivider
import com.example.daypilot_test_desing.ui.components.basic.DayPilotReactionBadgeRow
import com.example.daypilot_test_desing.ui.components.basic.DayPilotWeeklyStat
import com.example.daypilot_test_desing.ui.components.basic.ReactionType
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

data class ReceivedReaction(
    val fromName: String,
    val reaction: ReactionType,
    val avatarUrl: String? = null
)

data class WeeklySummaryData(
    val totalPoints: Int,
    val tasksCompleted: Int,
    val totalSteps: Int,
    val bestStreak: Int,
    val reactions: List<ReceivedReaction> = emptyList()
)

@Composable
fun WeeklyReactionCard(
    summary: WeeklySummaryData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── Cabecera ─────────────────────────────────────
                Text(
                    text       = stringResource(R.string.weekly_summary_title),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                // ── Stats en una sola fila ────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    DayPilotWeeklyStat(
                        emoji = "⭐",
                        value = summary.totalPoints.toString(),
                        label = stringResource(R.string.weekly_summary_points)
                    )
                    WeeklyStatDivider()
                    DayPilotWeeklyStat(
                        emoji = "✅",
                        value = summary.tasksCompleted.toString(),
                        label = stringResource(R.string.weekly_summary_tasks)
                    )
                    WeeklyStatDivider()
                    DayPilotWeeklyStat(
                        emoji = "👣",
                        value = summary.totalSteps.toString(),
                        label = stringResource(R.string.weekly_summary_steps)
                    )
                    WeeklyStatDivider()
                    DayPilotWeeklyStat(
                        emoji = "🔥",
                        value = "${summary.bestStreak}d",
                        label = stringResource(R.string.weekly_summary_streak)
                    )
                }

                // ── Reacciones ───────────────────────────────────
                if (summary.reactions.isNotEmpty()) {
                    DayPilotDivider()

                    Text(
                        text       = stringResource(R.string.weekly_summary_reactions),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DayPilotReactionBadgeRow(
                        reactions = summary.reactions.map {
                            Pair(it.fromName, it.reaction)
                        }
                    )
                }
            }
        }
    }
}

// ── Stat compacto ────────────────────────────────────────────────
@Composable
private fun WeeklyStatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun WeeklyReactionCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            WeeklyReactionCard(
                summary = WeeklySummaryData(
                    totalPoints    = 45,
                    tasksCompleted = 12,
                    totalSteps     = 42000,
                    bestStreak     = 7,
                    reactions      = listOf(
                        ReceivedReaction("Ana López",    ReactionType.CLAP),
                        ReceivedReaction("Carlos Ruiz",  ReactionType.FIRE),
                        ReceivedReaction("Laura Sánchez", ReactionType.STAR)
                    )
                )
            )
        }
    }
}