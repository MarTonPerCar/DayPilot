package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.TaskFormCard
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.model.TaskCategory
import com.example.daypilot_test_desing.ui.model.TaskDifficulty
import com.example.daypilot_test_desing.ui.model.CalendarTaskData
import java.util.Calendar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<CalendarTaskData>,
    onBack: () -> Unit,
    onAddTask: (day: Int) -> Unit,
    onTapTask: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit = {},
    onEditTask: (String) -> Unit = {}
) {
    val today = Calendar.getInstance()
    var currentMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH) + 1) }
    var currentYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf<Int?>(today.get(Calendar.DAY_OF_MONTH)) }

    // Filtros
    var selectedDifficulty by remember { mutableStateOf<TaskDifficulty?>(null) }
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }

    // BottomSheet
    var showAddSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var dayForNewTask by remember { mutableStateOf(1) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Puntos de color para el calendario
    val taskDots = tasks.map { task ->
        CalendarTaskDot(day = task.day, color = task.category.color)
    }

    // Tareas filtradas del día seleccionado
    val tasksForSelectedDay = selectedDay?.let { day ->
        tasks.filter { it.day == day }
            .filter { selectedDifficulty == null || it.difficulty == selectedDifficulty }
            .filter { selectedCategory == null || it.category == selectedCategory }
    } ?: emptyList()

    // BottomSheet para añadir/editar
    if (showAddSheet || editingTaskId != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
                editingTaskId = null
            },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            TaskFormCard(
                isEditing = editingTaskId != null,
                onSave = {
                    showAddSheet = false
                    editingTaskId = null
                },
                onCancel = {
                    showAddSheet = false
                    editingTaskId = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title = stringResource(R.string.calendar_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Calendario ───────────────────────────────────────
            DayPilotCalendar(
                month = currentMonth,
                year = currentYear,
                taskDots = taskDots,
                selectedDay = selectedDay,
                onDaySelected = { selectedDay = it },
                onPreviousMonth = {
                    if (currentMonth == 1) {
                        currentMonth = 12; currentYear--
                    } else currentMonth--
                    selectedDay = null
                },
                onNextMonth = {
                    if (currentMonth == 12) {
                        currentMonth = 1; currentYear++
                    } else currentMonth++
                    selectedDay = null
                },
                onAddTask = { day ->
                    dayForNewTask = day
                    showAddSheet = true
                }
            )

            // ── Filtros ──────────────────────────────────────────
            selectedDay?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filtro dificultad
                    var showDifficultyMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showDifficultyMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDifficulty != null)
                                    selectedDifficulty!!.color.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedDifficulty?.label ?: "Dificultad",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = selectedDifficulty?.color
                                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = selectedDifficulty?.color
                                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showDifficultyMenu,
                            onDismissRequest = { showDifficultyMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    selectedDifficulty = null
                                    showDifficultyMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            TaskDifficulty.entries.forEach { diff ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = diff.label,
                                            color = diff.color,
                                            fontWeight = if (selectedDifficulty == diff)
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedDifficulty =
                                            if (selectedDifficulty == diff) null else diff
                                        showDifficultyMenu = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(diff.color)
                                        )
                                    },
                                    trailingIcon = {
                                        if (selectedDifficulty == diff) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = diff.color,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Filtro categoría
                    var showCategoryMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedCategory != null)
                                    selectedCategory!!.color.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectedCategory != null) {
                                        Icon(
                                            imageVector = selectedCategory!!.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = selectedCategory!!.color
                                        )
                                    }
                                    Text(
                                        text = selectedCategory?.label ?: "Categoría",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = selectedCategory?.color
                                            ?: MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = selectedCategory?.color
                                        ?: MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    selectedCategory = null
                                    showCategoryMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                            TaskCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cat.label,
                                            color = cat.color,
                                            fontWeight = if (selectedCategory == cat)
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCategory =
                                            if (selectedCategory == cat) null else cat
                                        showCategoryMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = cat.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (selectedCategory == cat) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = cat.color,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                // ── Tareas del día ───────────────────────────────
                DayPilotSectionHeader(
                    title      = "Tareas del día $selectedDay",
                    actionText = "+ Añadir",
                    onAction   = {
                        dayForNewTask = selectedDay ?: 1
                        showAddSheet  = true
                    }
                )

                if (tasksForSelectedDay.isEmpty()) {
                    DayPilotEmptyState(
                        message  = stringResource(R.string.calendar_no_tasks),
                        modifier = Modifier.height(100.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tasksForSelectedDay.forEach { task ->
                            TaskDayCard(
                                title            = task.title,
                                category         = task.category,
                                difficulty       = task.difficulty,
                                durationMinutes  = task.duration,
                                isCompleted      = task.isDone,
                                onToggleComplete = { onToggleTask(task.id, it) },
                                onTap            = { onTapTask(task.id) },
                                onEdit           = { editingTaskId = task.id },
                                onDelete         = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}