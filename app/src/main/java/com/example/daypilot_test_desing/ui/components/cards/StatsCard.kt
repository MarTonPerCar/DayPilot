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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotDivider
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

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
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Cabecera ─────────────────────────────────────
                Text(
                    text       = "Resumen del día",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                // ── Ranking + Total ──────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsTopBlock(
                        icon    = Icons.Default.EmojiEvents,
                        label   = "Ranking",
                        value   = "#$rankingPosition",
                        modifier = Modifier.weight(1f)
                    )
                    StatsTopBlock(
                        icon    = Icons.Default.Star,
                        label   = "Puntos hoy",
                        value   = pointsToday.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                DayPilotDivider()

                // ── Desglose de puntos ───────────────────────────
                Text(
                    text  = "Desglose",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatsBreakdownRow(
                        icon   = Icons.Default.CheckCircle,
                        label  = "Tareas",
                        points = pointsFromTasks
                    )
                    StatsBreakdownRow(
                        icon   = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label  = "Pasos",
                        points = pointsFromSteps
                    )
                    StatsBreakdownRow(
                        icon   = Icons.Default.Favorite,
                        label  = "Hábitos",
                        points = pointsFromHabits
                    )
                    StatsBreakdownRow(
                        icon   = Icons.Default.Timer,
                        label  = "Cronómetro",
                        points = pointsFromTimers
                    )
                }
            }
        }
    }
}

// ── Bloque superior (ranking / total) ────────────────────────────
@Composable
fun StatsTopBlock(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Fila de desglose ─────────────────────────────────────────────
@Composable
fun StatsBreakdownRow(
    icon: ImageVector,
    label: String,
    points: Int
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier           = Modifier.size(16.dp)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text       = "$points pts",
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Preview ──────────────────────────────────────────────────────
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
                rankingPosition  = 2,
                pointsToday      = 8,
                pointsFromTasks  = 4,
                pointsFromSteps  = 2,
                pointsFromHabits = 1,
                pointsFromTimers = 1
            )
        }
    }
}