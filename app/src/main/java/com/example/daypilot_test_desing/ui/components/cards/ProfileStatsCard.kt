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
import com.example.daypilot_test_desing.ui.components.basic.DayPilotAvatar
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

@Composable
fun ProfileStatsCard(
    name: String,
    username: String,
    level: Int,
    totalPoints: Int,
    currentStreak: Int,
    longestStreak: Int,
    avatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // ── Cabecera: avatar + info ───────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DayPilotAvatar(name = name, avatarUrl = avatarUrl, size = 64)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Badge de nivel
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ Nivel $level",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── Barra de progreso de nivel ────────────────────
                val pointsForCurrentLevel = (level - 1) * 50
                val pointsForNextLevel    = level * 50
                val levelProgress = ((totalPoints - pointsForCurrentLevel).toFloat() /
                        (pointsForNextLevel - pointsForCurrentLevel)).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progreso nivel ${level + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$totalPoints / $pointsForNextLevel pts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // ── Stats ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileStatBlock(
                        icon = Icons.Default.Star,
                        label = "Puntos totales",
                        value = totalPoints.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatBlock(
                        icon = Icons.Default.Whatshot,
                        label = "Racha actual",
                        value = "${currentStreak}🔥",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatBlock(
                        icon = Icons.Default.EmojiEvents,
                        label = "Mejor racha",
                        value = "${longestStreak}⚡",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStatBlock(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
fun ProfileStatsCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ProfileStatsCard(
                name = "Mario García",
                username = "mariogarcia",
                level = 7,
                totalPoints = 340,
                currentStreak = 7,
                longestStreak = 14
            )
        }
    }
}