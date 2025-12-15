package com.example.daypilot.main.mainZone.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CalendarTaskDetailDialog(
    task: Task,
    today: LocalDate,
    onDismiss: () -> Unit,
    onOpenInTasks: () -> Unit
) {
    val scroll = rememberScrollState()
    val iso = DateTimeFormatter.ISO_LOCAL_DATE

    // Próxima fecha de la tarea (mínima)
    val nextDay: LocalDate? = task.days
        .mapNotNull { runCatching { LocalDate.parse(it, iso) }.getOrNull() }
        .minOrNull()

    val isOverdue = nextDay != null && nextDay.isBefore(today) && !task.isCompleted

    val headerTitle = when {
        task.isCompleted -> "Completada"
        isOverdue -> "Atrasada"
        nextDay == null -> "Sin fecha"
        nextDay == today -> "Hoy"
        nextDay == today.plusDays(1) -> "Mañana"
        else -> "Próxima"
    }

    val headerColor = when {
        task.isCompleted -> MaterialTheme.colorScheme.tertiary
        isOverdue -> MaterialTheme.colorScheme.error
        nextDay == today -> MaterialTheme.colorScheme.primary
        nextDay == today.plusDays(1) -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val diffColor = when (task.difficulty) {
        TaskDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
        TaskDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        TaskDifficulty.HARD -> MaterialTheme.colorScheme.error
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header potente
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = headerColor
                )

                if (nextDay != null) {
                    Text(
                        text = "Fecha: ${formatFullDate(nextDay)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (task.isCompleted) "Estado: Completada ✅" else "Estado: Pendiente ⏳",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )

                // Título + descripción
                Text(
                    text = task.title.ifBlank { "(Sin título)" },
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Sin descripción.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Info (chips)
                Text("Información", style = MaterialTheme.typography.titleSmall)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                when (task.difficulty) {
                                    TaskDifficulty.EASY -> "Dificultad: Fácil"
                                    TaskDifficulty.MEDIUM -> "Dificultad: Media"
                                    TaskDifficulty.HARD -> "Dificultad: Difícil"
                                }
                            )
                        },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = diffColor.copy(alpha = 0.18f)
                        )
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text("Categoría: ${task.category.ifBlank { "General" }}") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text("Duración: ${formatDuration(task.estimatedMinutes)}") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text("Recordatorio: ${if (task.reminderEnabled) "Sí" else "No"}") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    AssistChip(
                        onClick = {},
                        label = { Text("Creada: ${formatMillisDateTime(task.createdAt)}") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    task.completedAt?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text("Completada: ${formatTimestampDateTime(it)}") },
                            colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            )
                        )
                    }
                }

                // Días programados
                Text("Días programados", style = MaterialTheme.typography.titleSmall)
                if (task.days.isEmpty()) {
                    Text(
                        text = "No hay días asignados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        task.days.sorted().forEach { isoStr ->
                            AssistChip(onClick = {}, label = { Text(isoStr) })
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(top = 4.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cerrar") }

                    Button(
                        onClick = onOpenInTasks,
                        modifier = Modifier.weight(1f)
                    ) { Text("Abrir en Tareas") }
                }
            }
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "${h} h"
        else -> "${h} h ${m} min"
    }
}

private fun formatFullDate(date: LocalDate): String =
    "${date.dayOfMonth}/${date.monthValue}/${date.year}"

private fun formatMillisDateTime(millis: Long): String {
    if (millis <= 0L) return "—"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    return sdf.format(Date(millis))
}

private fun formatTimestampDateTime(ts: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    return sdf.format(ts.toDate())
}