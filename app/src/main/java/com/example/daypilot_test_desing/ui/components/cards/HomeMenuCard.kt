package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Tipos de sección ─────────────────────────────────────────────
enum class HomeSection(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    CALENDAR(
        title = "Calendario",
        icon = Icons.Default.CalendarMonth,
        accentColor = Color(0xFF4A7C59)
    ),
    PROGRESS(
        title = "Progreso",
        icon = Icons.Default.BarChart,
        accentColor = Color(0xFF1A6B8A)
    ),
    HABITS(
        title = "Hábitos",
        icon = Icons.Default.FitnessCenter,
        accentColor = Color(0xFF6B4FA8)
    ),
    RIVALRY(
        title = "Rivalidad",
        icon = Icons.Default.EmojiEvents,
        accentColor = Color(0xFFB85C00)
    )
}

// ── Datos de preview por sección ─────────────────────────────────
sealed class HomeSectionData {
    data class Calendar(val pendingTasks: Int, val completedTasks: Int) : HomeSectionData()
    data class Progress(val currentPoints: Int, val goalPoints: Int) : HomeSectionData()
    data class Habits(val stepsProgress: Float, val timerDone: Boolean) : HomeSectionData()
    data class Rivalry(val position: Int, val totalFriends: Int) : HomeSectionData()
}

// ── HomeMenuCard ─────────────────────────────────────────────────
@Composable
fun HomeMenuCard(
    section: HomeSection,
    data: HomeSectionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Gradiente de fondo ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                section.accentColor.copy(alpha = 0.12f),
                                section.accentColor.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            // ── Icono decorativo de fondo ────────────────────────
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = section.accentColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .rotate(-15f)
            )

            // ── Contenido ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono pequeño + título
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(section.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = section.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Indicador visual por sección
                HomeSectionIndicator(data = data, accentColor = section.accentColor)
            }
        }
    }
}

// ── Indicador visual por sección ─────────────────────────────────
@Composable
fun HomeSectionIndicator(data: HomeSectionData, accentColor: Color) {
    when (data) {

        is HomeSectionData.Calendar -> {
            val total = data.pendingTasks + data.completedTasks
            val progress = if (total > 0) data.completedTasks.toFloat() / total else 0f
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${data.completedTasks}/${total} tareas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f)
                )
            }
        }

        is HomeSectionData.Progress -> {
            val progress = (data.currentPoints.toFloat() / data.goalPoints).coerceIn(0f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${data.currentPoints} pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = "meta ${data.goalPoints}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f)
                )
            }
        }

        is HomeSectionData.Habits -> {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Pasos
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "👣 ${(data.stepsProgress * 100).toInt()}% pasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { data.stepsProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = accentColor.copy(alpha = 0.2f)
                    )
                }
                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (data.timerDone) Icons.Default.CheckCircle
                        else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (data.timerDone) accentColor
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (data.timerDone) "Cronómetro completado"
                        else "Cronómetro pendiente",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (data.timerDone) accentColor
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is HomeSectionData.Rivalry -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🏆 #${data.position} de ${data.totalFriends}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                // Mini ranking visual
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(data.totalFriends.coerceAtMost(5)) { index ->
                        val isCurrentUser = index == data.position - 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isCurrentUser) 20.dp else 12.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (isCurrentUser) accentColor
                                    else accentColor.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun HomeMenuCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMenuCard(
                    section = HomeSection.CALENDAR,
                    data = HomeSectionData.Calendar(pendingTasks = 2, completedTasks = 3),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section = HomeSection.PROGRESS,
                    data = HomeSectionData.Progress(currentPoints = 340, goalPoints = 500),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMenuCard(
                    section = HomeSection.HABITS,
                    data = HomeSectionData.Habits(stepsProgress = 0.6f, timerDone = true),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section = HomeSection.RIVALRY,
                    data = HomeSectionData.Rivalry(position = 2, totalFriends = 5),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}