package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotAvatar
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Medalla ──────────────────────────────────────────────────────
private fun rankMedal(position: Int): String = when (position) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "#$position"
}

// ── Base compartida ──────────────────────────────────────────────
@Composable
private fun RankingCardBase(
    name: String,
    position: Int,
    points: Int,
    streak: Int,
    level: Int = 1,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    isCurrentUser: Boolean = false
) {
    val shape = RoundedCornerShape(20.dp)

    val borderModifier = if (isCurrentUser) {
        Modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                )
            ),
            shape = shape
        )
    } else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentUser) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Posición / medalla ────────────────────────────────
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (position <= 3) {
                    Text(
                        text = rankMedal(position),
                        fontSize = 22.sp
                    )
                } else {
                    Text(
                        text = "#$position",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Avatar ────────────────────────────────────────────
            DayPilotAvatar(name = name, avatarUrl = avatarUrl, size = 44)

            // ── Nombre + nivel + racha ────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.profile_level_badge, level),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.ranking_streak, streak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Puntos ────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.ranking_points),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── RankingCard ───────────────────────────────────────────────────
@Composable
fun RankingCard(
    name: String,
    position: Int,
    points: Int,
    streak: Int,
    level: Int = 1,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    isCurrentUser: Boolean = false
) {
    RankingCardBase(
        name          = name,
        avatarUrl     = avatarUrl,
        position      = position,
        points        = points,
        streak        = streak,
        level         = level,
        modifier      = modifier,
        isCurrentUser = isCurrentUser
    )
}

// ── CurrentUserRankingCard ────────────────────────────────────────
@Composable
fun CurrentUserRankingCard(
    name: String,
    position: Int,
    points: Int,
    streak: Int,
    level: Int = 1,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null
) {
    RankingCardBase(
        name          = name,
        avatarUrl     = avatarUrl,
        position      = position,
        points        = points,
        streak        = streak,
        level         = level,
        modifier      = modifier,
        isCurrentUser = true
    )
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun RankingCardsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RankingCard(name = "Ana López", position = 1, points = 520, streak = 14)
            RankingCard(name = "Carlos Ruiz", position = 2, points = 480, streak = 9)
            RankingCard(name = "Laura Sánchez", position = 3, points = 430, streak = 6)
            CurrentUserRankingCard(name = "Mario García", position = 4, points = 340, streak = 7)
            RankingCard(name = "Pedro Martín", position = 5, points = 290, streak = 3)
        }
    }
}