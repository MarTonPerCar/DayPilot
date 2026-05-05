package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.model.HomeSectionData
import com.example.daypilot_test_desing.ui.model.ProgressFilter
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

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
            val values = data.data.takeLast(7).map { day ->
                when (data.currentFilter) {
                    ProgressFilter.POINTS -> day.points
                    ProgressFilter.STEPS -> day.steps
                    ProgressFilter.TASKS -> day.tasksCompleted
                }
            }
            val maxValue = values.maxOrNull() ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(
                                (value.toFloat() / maxValue).coerceAtLeast(0.05f)
                            )
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(accentColor)
                    )
                }
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Top 5
                    val topCount = minOf(5, data.totalFriends)
                    repeat(topCount) { index ->
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

                    if (data.position > 5) {
                        // Separador
                        Spacer(Modifier.width(4.dp))

                        // Barra del usuario
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${data.position}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeSectionIndicatorPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeSectionIndicator(
                data = HomeSectionData.Calendar(
                    pendingTasks = 2,
                    completedTasks = 3
                ), accentColor = Color(0xFF4A7C59)
            )
            HomeSectionIndicator(
                data = HomeSectionData.Rivalry(position = 2, totalFriends = 5),
                accentColor = Color(0xFFB85C00)
            )
        }
    }
}