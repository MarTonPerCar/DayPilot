package com.example.daypilot.main.mainZone.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty

@Composable
fun DifficultyChip(
    label: String,
    difficulty: TaskDifficulty,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = when (difficulty) {
        TaskDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
        TaskDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        TaskDifficulty.HARD -> MaterialTheme.colorScheme.error
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.25f)
        )
    )
}

@Composable
fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    // 🎨 Colores de fondo para cada estado
    val completedBg =
        MaterialTheme.colorScheme.tertiaryContainer
    val pendingBg =
        MaterialTheme.colorScheme.error.copy(alpha = 0.10f)

    val cardColors = if (task.isCompleted) {
        CardDefaults.cardColors(containerColor = completedBg)
    } else {
        CardDefaults.cardColors(containerColor = pendingBg)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = cardColors,
        // 👇 Quitamos la sombra para que no parezca un borde oscuro
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila título + botón completar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title.ifBlank { "(Sin título)" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onCompleteClick,
                    enabled = !task.isCompleted,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.error
                    )
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Completada",
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        Text("Completar")
                    }
                }
            }

            // Fila inferior: dificultad + categoría + duración + fecha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Dificultad ---
                val difficultyText = when (task.difficulty) {
                    TaskDifficulty.EASY -> "Fácil"
                    TaskDifficulty.MEDIUM -> "Media"
                    TaskDifficulty.HARD -> "Difícil"
                }
                val difficultyColor = when (task.difficulty) {
                    TaskDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
                    TaskDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                    TaskDifficulty.HARD -> MaterialTheme.colorScheme.error
                }

                AssistChip(
                    onClick = { },
                    label = { Text(difficultyText) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = difficultyColor.copy(alpha = 0.2f)
                    )
                )

                // --- Categoría ---
                AssistChip(
                    onClick = {},
                    label = { Text(task.category) },
                    leadingIcon = {
                        Icon(
                            imageVector = categoryIconFor(task.category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                // --- Duración ---
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formatDuration(task.estimatedMinutes),
                        style = MaterialTheme.typography.bodySmall
                    )


                    // --- Día más próximo ---
                    val nextDay = task.days.minOrNull()
                    if (nextDay != null) {
                        val diff = daysBetweenToday(nextDay)
                        val dueText = when {
                            diff == null -> "En: ${formatDisplayDate(nextDay)}"
                            diff < 0 -> "Atrasada (${formatDisplayDate(nextDay)})"
                            diff == 0 -> "Hoy (${formatDisplayDate(nextDay)})"
                            diff == 1 -> "Mañana (${formatDisplayDate(nextDay)})"
                            else -> "En: ${formatDisplayDate(nextDay)}"
                        }
                        val dueColor =
                            if (diff != null && diff < 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                        Text(
                            text = dueText,
                            style = MaterialTheme.typography.bodySmall,
                            color = dueColor
                        )
                    }
                }
            }
        }
    }
}