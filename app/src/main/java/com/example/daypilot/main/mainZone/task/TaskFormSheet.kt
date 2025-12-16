package com.example.daypilot.main.mainZone.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormSheet(
    initialTask: Task?,
    onSave: (Task) -> Unit,
    onDelete: (Task?) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var difficulty by remember { mutableStateOf(initialTask?.difficulty ?: TaskDifficulty.MEDIUM) }
    var estimatedMinutes by remember { mutableStateOf(initialTask?.estimatedMinutes ?: 30) }

    val daysState = remember {
        mutableStateListOf<String>().apply {
            initialTask?.days?.let { addAll(it) }
        }
    }

    val categoryOptions = listOf(
        "General", "Estudios", "Trabajo", "Salud", "Personal", "Deporte", "Casa"
    )
    var category by remember { mutableStateOf(initialTask?.category ?: "General") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var reminderEnabled by remember { mutableStateOf(initialTask?.reminderEnabled ?: false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Date(millis)
                        val iso = dateFormatter.format(date)
                        if (!daysState.contains(iso)) {
                            daysState.add(iso)
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initialTask == null) "Nueva tarea" else "Editar tarea",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Nombre de la tarea") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Dificultad", style = MaterialTheme.typography.bodyMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyChip(
                    label = "Fácil",
                    difficulty = TaskDifficulty.EASY,
                    selected = difficulty == TaskDifficulty.EASY,
                    onClick = { difficulty = TaskDifficulty.EASY }
                )
                DifficultyChip(
                    label = "Media",
                    difficulty = TaskDifficulty.MEDIUM,
                    selected = difficulty == TaskDifficulty.MEDIUM,
                    onClick = { difficulty = TaskDifficulty.MEDIUM }
                )
                DifficultyChip(
                    label = "Difícil",
                    difficulty = TaskDifficulty.HARD,
                    selected = difficulty == TaskDifficulty.HARD,
                    onClick = { difficulty = TaskDifficulty.HARD }
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Duración aproximada", style = MaterialTheme.typography.bodyMedium)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        estimatedMinutes = when {
                            estimatedMinutes <= 5 -> 5
                            estimatedMinutes <= 30 -> estimatedMinutes - 5
                            else -> estimatedMinutes - 15
                        }.coerceAtLeast(5)
                    },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("-")
                }

                Text(
                    text = formatDuration(estimatedMinutes),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = {
                        estimatedMinutes = when {
                            estimatedMinutes < 30 -> estimatedMinutes + 5
                            else -> estimatedMinutes + 15
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+")
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Días", style = MaterialTheme.typography.bodyMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir día")
                }
            }

            if (daysState.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    daysState.sorted().forEach { iso ->
                        AssistChip(
                            onClick = { daysState.remove(iso) },
                            label = { Text(formatDisplayDate(iso)) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No hay días seleccionados.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Categoría", style = MaterialTheme.typography.bodyMedium)

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = categoryIconFor(option),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(option)
                                }
                            },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Recordatorio", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Activar recordatorio para esta tarea",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialTask != null) {
                    TextButton(
                        onClick = { onDelete(initialTask) }
                    ) {
                        Text("Eliminar")
                    }
                }

                Button(
                    onClick = {
                        val task = Task(
                            id = initialTask?.id ?: "",
                            title = title,
                            description = description,
                            difficulty = difficulty,
                            estimatedMinutes = estimatedMinutes,
                            days = daysState.toList(),
                            category = category,
                            reminderEnabled = reminderEnabled,
                            createdAt = initialTask?.createdAt ?: System.currentTimeMillis(),
                            isCompleted = initialTask?.isCompleted ?: false,
                            completedAt = initialTask?.completedAt
                        )
                        onSave(task)
                    }
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
