package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotEmptyState
import com.example.daypilot_test_desing.ui.components.basic.TaskCategory
import com.example.daypilot_test_desing.ui.components.basic.TaskDifficulty
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<CalendarTaskData>,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onTapTask: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit
) {
    var selectedDay by remember { mutableStateOf(17) }
    val daysInMonth = 30
    val weekDays = listOf("L", "M", "X", "J", "V", "S", "D")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Cabecera mes ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Abril 2026",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // ── Días de la semana ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Grid de días ─────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val firstDayOffset = 1
                val totalCells = daysInMonth + firstDayOffset
                val rows = (totalCells + 6) / 7

                repeat(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val day = cellIndex - firstDayOffset + 1
                            val isValidDay = day in 1..daysInMonth
                            val dayTasks = tasks.filter { it.day == day }

                            if (isValidDay) {
                                CalendarDayCard(
                                    day            = day,
                                    isToday        = day == 17,
                                    isSelected     = day == selectedDay,
                                    hasEasyTask    = dayTasks.any { it.difficulty == TaskDifficulty.EASY },
                                    hasMediumTask  = dayTasks.any { it.difficulty == TaskDifficulty.MEDIUM },
                                    hasHardTask    = dayTasks.any { it.difficulty == TaskDifficulty.HARD },
                                    isCurrentMonth = true,
                                    onClick        = { selectedDay = day },
                                    modifier       = Modifier.weight(1f)
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // ── Tareas del día seleccionado ───────────────────────
            val selectedTasks = tasks.filter { it.day == selectedDay }

            Text(
                text = "Tareas del día $selectedDay",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            if (selectedTasks.isEmpty()) {
                DayPilotEmptyState(message = "No hay tareas para este día")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedTasks) { task ->
                        TaskDayCard(
                            title            = task.title,
                            category         = task.category,
                            durationMinutes  = task.durationMinutes,
                            isCompleted      = task.isCompleted,
                            onToggleComplete = { onToggleTask(task.id, it) },
                            onTap            = { onTapTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

data class CalendarTaskData(
    val id: String,
    val day: Int,
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val durationMinutes: Int,
    val isCompleted: Boolean
)