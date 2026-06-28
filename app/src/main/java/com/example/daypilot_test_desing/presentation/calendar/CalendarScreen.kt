package com.example.daypilot_test_desing.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.components.DayPilotCalendar
import com.example.daypilot_test_desing.ui.components.forms.TaskFormCard
import com.example.daypilot_test_desing.backend.model.CalendarTaskData
import com.example.daypilot_test_desing.backend.model.CalendarTaskDot
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<CalendarTaskData>,
    onBack: () -> Unit,
    onCreateTask: (NewTaskData) -> Unit,
    onTapTask: (String) -> Unit,
    onToggleTask: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit = {},
    onEditTask: (String) -> Unit = {},
    onUpdateTask: (id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int) -> Unit = { _, _, _, _, _ -> }
) {
    val now         = remember { Calendar.getInstance() }
    val todayDay    = remember { now.get(Calendar.DAY_OF_MONTH) }
    val todayMonth  = remember { now.get(Calendar.MONTH) + 1 }
    val todayYear   = remember { now.get(Calendar.YEAR) }

    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var currentYear  by remember { mutableIntStateOf(todayYear) }
    var selectedDay  by remember { mutableStateOf<Int?>(todayDay) }

    // Filtros
    var selectedDifficulty by remember { mutableStateOf<TaskDifficulty?>(null) }
    var selectedCategory   by remember { mutableStateOf<TaskCategory?>(null) }

    // BottomSheet
    var showAddSheet  by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var dayForNewTask by remember { mutableStateOf(1) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Helper: given a new month+year, pick the day to auto-select
    fun autoSelectDay(newMonth: Int, newYear: Int): Int = when {
        newYear > todayYear || (newYear == todayYear && newMonth > todayMonth) -> 1
        newYear < todayYear || (newYear == todayYear && newMonth < todayMonth) -> {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, newYear)
            cal.set(Calendar.MONTH, newMonth - 1)
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        else -> todayDay
    }

    // Memoised derived values — recompute only when their actual dependencies change
    val taskDots by remember(tasks) {
        derivedStateOf {
            tasks.map { CalendarTaskDot(day = it.day, month = it.month, year = it.year, color = it.category.color) }
        }
    }

    val tasksForSelectedDay by remember(tasks, selectedDay, currentMonth, currentYear, selectedDifficulty, selectedCategory) {
        derivedStateOf {
            val day = selectedDay ?: return@derivedStateOf emptyList<CalendarTaskData>()
            tasks.filter {
                it.day == day && it.month == currentMonth && it.year == currentYear &&
                (selectedDifficulty == null || it.difficulty == selectedDifficulty) &&
                (selectedCategory   == null || it.category   == selectedCategory)
            }
        }
    }

    val editingTask by remember(editingTaskId, tasks) {
        derivedStateOf { editingTaskId?.let { id -> tasks.find { it.id == id } } }
    }

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
                isEditing         = editingTaskId != null,
                initialTitle      = editingTask?.title      ?: "",
                initialCategory   = editingTask?.category   ?: TaskCategory.PERSONAL,
                initialDifficulty = editingTask?.difficulty ?: TaskDifficulty.EASY,
                initialDuration   = editingTask?.duration   ?: 30,
                onSave = { title, category, difficulty, duration ->
                    val currentEditId = editingTaskId
                    if (currentEditId == null) {
                        onCreateTask(
                            NewTaskData(
                                day        = dayForNewTask,
                                month      = currentMonth,
                                year       = currentYear,
                                title      = title,
                                category   = category,
                                difficulty = difficulty,
                                duration   = duration
                            )
                        )
                    } else {
                        onUpdateTask(currentEditId, title, category, difficulty, duration)
                    }
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
                    val newMonth = if (currentMonth == 1) 12 else currentMonth - 1
                    val newYear  = if (currentMonth == 1) currentYear - 1 else currentYear
                    currentMonth = newMonth
                    currentYear  = newYear
                    selectedDay  = autoSelectDay(newMonth, newYear)
                },
                onNextMonth = {
                    val newMonth = if (currentMonth == 12) 1 else currentMonth + 1
                    val newYear  = if (currentMonth == 12) currentYear + 1 else currentYear
                    currentMonth = newMonth
                    currentYear  = newYear
                    selectedDay  = autoSelectDay(newMonth, newYear)
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
                                    text = selectedDifficulty?.let { stringResource(it.labelRes) }
                                        ?: stringResource(R.string.task_difficulty_label),
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
                                text = { Text(stringResource(R.string.calendar_filter_all)) },
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
                                            text = stringResource(diff.labelRes),
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
                                        text = selectedCategory?.let { stringResource(it.labelRes) }
                                            ?: stringResource(R.string.task_category_label),
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
                                text = { Text(stringResource(R.string.calendar_filter_all)) },
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
                                            text = stringResource(cat.labelRes),
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
                    title = stringResource(R.string.calendar_day_tasks_title, selectedDay ?: 0),
                    actionText = stringResource(R.string.calendar_add_task_action),
                    onAction = {
                        dayForNewTask = selectedDay ?: 1
                        showAddSheet = true
                    }
                )

                if (tasksForSelectedDay.isEmpty()) {
                    DayPilotEmptyState(
                        message = stringResource(R.string.calendar_no_tasks),
                        modifier = Modifier.height(100.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tasksForSelectedDay.forEach { task ->
                            TaskDayCard(
                                title = task.title,
                                category = task.category,
                                difficulty = task.difficulty,
                                durationMinutes = task.duration,
                                isCompleted = task.isDone,
                                onToggleComplete = { onToggleTask(task.id, it) },
                                onTap = { onTapTask(task.id) },
                                onEdit = { editingTaskId = task.id },
                                onDelete = { onDeleteTask(task.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}