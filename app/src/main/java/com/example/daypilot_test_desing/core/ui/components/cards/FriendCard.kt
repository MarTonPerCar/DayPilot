package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.*
import com.example.daypilot_test_desing.core.data.model.FriendWeeklySummary
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Immutable
data class FriendCardInfo(
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null,
    val weeklySummary: FriendWeeklySummary? = null
)

@Composable
fun FriendCard(
    info: FriendCardInfo,
    modifier: Modifier = Modifier,
    onReact: (ReactionType) -> Unit = {},
    onRemove: (() -> Unit)? = null
) {
    val name          = info.name
    val email         = info.email
    val points        = info.points
    val streak        = info.streak
    val avatarUrl     = info.avatarUrl
    val weeklySummary = info.weeklySummary
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DayPilotAvatar(name = name, avatarUrl = avatarUrl, size = 52)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DayPilotStatsRow(points = points, streak = streak)
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.PersonRemove,
                        contentDescription = stringResource(R.string.friends_remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        DayPilotDivider()
        if (weeklySummary != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayPilotWeeklyStat(
                            emoji = "⭐",
                            value = weeklySummary.totalPoints.toString(),
                            label = stringResource(R.string.weekly_summary_points),
                            modifier = Modifier.weight(1f)
                        )
                        DayPilotWeeklyStat(
                            emoji = "✅",
                            value = weeklySummary.tasksCompleted.toString(),
                            label = stringResource(R.string.weekly_summary_tasks),
                            modifier = Modifier.weight(1f)
                        )
                        DayPilotWeeklyStat(
                            emoji = "👣",
                            value = weeklySummary.totalSteps.toString(),
                            label = stringResource(R.string.weekly_summary_steps),
                            modifier = Modifier.weight(1f)
                        )
                        DayPilotWeeklyStat(
                            emoji = "🔥",
                            value = "${weeklySummary.bestStreak}d",
                            label = stringResource(R.string.weekly_summary_streak),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    DayPilotReactionButton(
                        selectedReaction = weeklySummary.myReaction,
                        onReact = onReact
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.weekly_summary_no_data),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FriendCard(
                info = FriendCardInfo(
                    name = "Carlos Ruiz",
                    email = "carlos@example.com",
                    points = 480,
                    streak = 9
                )
            )
            FriendCard(
                info = FriendCardInfo(
                    name = "Ana López",
                    email = "ana@example.com",
                    points = 520,
                    streak = 14,
                    weeklySummary = FriendWeeklySummary(
                        totalPoints = 45,
                        tasksCompleted = 12,
                        totalSteps = 42000,
                        bestStreak = 7,
                        myReaction = ReactionType.CLAP
                    )
                )
            )
        }
    }
}