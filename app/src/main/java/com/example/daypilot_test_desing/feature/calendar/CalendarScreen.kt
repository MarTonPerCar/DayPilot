package com.example.daypilot_test_desing.feature.calendar

import androidx.annotation.StringRes
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
import com.example.daypilot_test_desing.core.ui.components.basic.*
import com.example.daypilot_test_desing.core.ui.components.cards.*
import com.example.daypilot_test_desing.core.ui.components.DayPilotCalendar
import com.example.daypilot_test_desing.core.ui.components.DayPilotCalendarActions
import com.example.daypilot_test_desing.core.ui.components.DayPilotCalendarState
import com.example.daypilot_test_desing.core.ui.components.forms.TaskFormCard
import com.example.daypilot_test_desing.core.ui.components.forms.TaskFormInitialData
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.CalendarTaskDot
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import java.util.Calendar

data class CalendarActions(
    val onMessageShown: () -> Unit = {},
    val onBack: () -> Unit,
    val onCreateTask: (NewTaskData) -> Unit,
    val onTapTask: (String) -> Unit,
    val onToggleTask: (String, Boolean) -> Unit,
    val onDeleteTask: (String) -> Unit = {},
    val onUpdateTask: (id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int, description: String) -> Unit = { _, _, _, _, _, _ -> }
)

private fun autoSelectDayFor(newMonth: Int, newYear: Int, todayDay: Int, todayMonth: Int, todayYear: Int): Int = when {
    newYear > todayYear || (newYear == todayYear && newMonth > todayMonth) -> 1
    newYear < todayYear || (newYear == todayYear && newMonth < todayMonth) -> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, newYear)
        cal.set(Calendar.MONTH, newMonth - 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    else -> todayDay
}

@Composable
private fun RowScope.TaskDetailHeader(task: CalendarTaskData) {
    Text(
        text = task.title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.weight(1f)
    )
    if (task.isDone) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TaskDetailBadgesRow(task: CalendarTaskData) {
    if (!task.isRecurring && !task.hasReminder) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (task.isRecurring) {
            SuggestionChip(
                onClick = {},
                label = { Text(stringResource(R.string.task_detail_recurring), style = MaterialTheme.typography.labelSmall) },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
        }
        if (task.hasReminder) {
            SuggestionChip(
                onClick = {},
                label = { Text(stringResource(R.string.task_detail_reminder), style = MaterialTheme.typography.labelSmall) },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp)) }
            )
        }
    }
}

@Composable
private fun RowScope.TaskDetailActionsRow(task: CalendarTaskData, onToggle: () -> Unit, onEdit: () -> Unit) {
    OutlinedButton(
        onClick = onToggle,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (task.isDone) stringResource(R.string.task_mark_pending)
                   else stringResource(R.string.task_mark_done)
        )
    }
    Button(
        onClick = onEdit,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.task_edit))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDetailSheet(
    task: CalendarTaskData,
    onDismiss: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TaskDetailHeader(task = task)
            }

            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DifficultyChip(difficulty = task.difficulty)
                CategoryChip(category = task.category)
                DurationChip(minutes = task.duration)
            }

            TaskDetailBadgesRow(task = task)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaskDetailActionsRow(
                    task = task,
                    onToggle = { onToggle(task.occurrenceId, !task.isDone) },
                    onEdit = { onEdit(task.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormSheet(
    show: Boolean,
    editingTaskId: String?,
    editingTask: CalendarTaskData?,
    dayForNewTask: Int,
    currentMonth: Int,
    currentYear: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCreateTask: (NewTaskData) -> Unit,
    onUpdateTask: (id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int, description: String) -> Unit
) {
    if (!show) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        TaskFormCard(
            isEditing = editingTaskId != null,
            initialData = TaskFormInitialData(
                title      = editingTask?.title       ?: "",
                description= editingTask?.description ?: "",
                category   = editingTask?.category    ?: TaskCategory.PERSONAL,
                difficulty = editingTask?.difficulty  ?: TaskDifficulty.EASY,
                duration   = editingTask?.duration    ?: 30
            ),
            onSave = { title, category, difficulty, duration, description, isRecurring, hasReminder, recurrenceDays ->
                if (editingTaskId == null) {
                    onCreateTask(
                        NewTaskData(
                            day           = dayForNewTask,
                            month         = currentMonth,
                            year          = currentYear,
                            title         = title,
                            category      = category,
                            difficulty    = difficulty,
                            duration      = duration,
                            description   = description,
                            isRecurring   = isRecurring,
                            hasReminder   = hasReminder,
                            recurrenceDays= recurrenceDays
                        )
                    )
                } else {
                    onUpdateTask(editingTaskId, title, category, difficulty, duration, description)
                }
                onDismiss()
            },
            onCancel = onDismiss
        )
    }
}

@Composable
private fun DifficultyFilterDropdown(selectedDifficulty: TaskDifficulty?, onSelect: (TaskDifficulty?) -> Unit, modifier: Modifier = Modifier) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { showMenu = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedDifficulty != null)
                    selectedDifficulty.color.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
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
                    color = selectedDifficulty?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = selectedDifficulty?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.calendar_filter_all)) },
                onClick = { onSelect(null); showMenu = false },
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            TaskDifficulty.entries.forEach { diff ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(diff.labelRes),
                            color = diff.color,
                            fontWeight = if (selectedDifficulty == diff) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(if (selectedDifficulty == diff) null else diff)
                        showMenu = false
                    },
                    leadingIcon = {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(diff.color))
                    },
                    trailingIcon = {
                        if (selectedDifficulty == diff) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = diff.color, modifier = Modifier.size(14.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterDropdown(selectedCategory: TaskCategory?, onSelect: (TaskCategory?) -> Unit, modifier: Modifier = Modifier) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { showMenu = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedCategory != null)
                    selectedCategory.color.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
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
                            imageVector = selectedCategory.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = selectedCategory.color
                        )
                    }
                    Text(
                        text = selectedCategory?.let { stringResource(it.labelRes) }
                            ?: stringResource(R.string.task_category_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = selectedCategory?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = selectedCategory?.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.calendar_filter_all)) },
                onClick = { onSelect(null); showMenu = false },
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            TaskCategory.entries.forEach { cat ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(cat.labelRes),
                            color = cat.color,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(if (selectedCategory == cat) null else cat)
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (selectedCategory == cat) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = cat.color, modifier = Modifier.size(14.dp))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskFiltersRow(
    selectedDifficulty: TaskDifficulty?,
    onDifficultySelect: (TaskDifficulty?) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelect: (TaskCategory?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DifficultyFilterDropdown(selectedDifficulty, onDifficultySelect, Modifier.weight(1f))
        CategoryFilterDropdown(selectedCategory, onCategorySelect, Modifier.weight(1f))
    }
}

@Composable
private fun DayTasksList(
    tasks: List<CalendarTaskData>,
    onToggle: (String, Boolean) -> Unit,
    onTap: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (tasks.isEmpty()) {
        DayPilotEmptyState(
            message = stringResource(R.string.calendar_no_tasks),
            modifier = Modifier.height(100.dp)
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tasks.forEach { task ->
            TaskDayCard(
                state = TaskDayCardUiState(
                    title          = task.title,
                    category       = task.category,
                    difficulty     = task.difficulty,
                    durationMinutes= task.duration,
                    isCompleted    = task.isDone,
                    hasReminder    = task.hasReminder,
                    isRecurring    = task.isRecurring,
                    isPending      = task.isPending
                ),
                actions = TaskDayCardActions(
                    onToggleComplete = { onToggle(task.occurrenceId, it) },
                    onTap          = { onTap(task.occurrenceId) },
                    onEdit         = { onEdit(task.id) },
                    onDelete       = { onDelete(task.id) }
                )
            )
        }
    }
}

@Composable
private fun SelectedDaySection(
    selectedDay: Int,
    tasksForSelectedDay: List<CalendarTaskData>,
    selectedDifficulty: TaskDifficulty?,
    onDifficultySelect: (TaskDifficulty?) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelect: (TaskCategory?) -> Unit,
    onAddTask: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onTap: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    TaskFiltersRow(selectedDifficulty, onDifficultySelect, selectedCategory, onCategorySelect)

    DayPilotSectionHeader(
        title = stringResource(R.string.calendar_day_tasks_title, selectedDay),
        actionText = stringResource(R.string.calendar_add_task_action),
        onAction = onAddTask
    )

    DayTasksList(tasksForSelectedDay, onToggle, onTap, onEdit, onDelete)

    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    actions: CalendarActions
) {
    val tasks       = state.tasks
    val userMessage = state.userMessage
    val onMessageShown = actions.onMessageShown
    val onBack          = actions.onBack
    val onCreateTask     = actions.onCreateTask
    val onToggleTask     = actions.onToggleTask
    val onDeleteTask     = actions.onDeleteTask
    val onUpdateTask     = actions.onUpdateTask
    val now        = remember { Calendar.getInstance() }
    val todayDay   = remember { now.get(Calendar.DAY_OF_MONTH) }
    val todayMonth = remember { now.get(Calendar.MONTH) + 1 }
    val todayYear  = remember { now.get(Calendar.YEAR) }

    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var currentYear  by remember { mutableIntStateOf(todayYear) }
    var selectedDay  by remember { mutableStateOf<Int?>(todayDay) }

    var selectedDifficulty by remember { mutableStateOf<TaskDifficulty?>(null) }
    var selectedCategory   by remember { mutableStateOf<TaskCategory?>(null) }

    var showAddSheet  by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var detailTaskId  by remember { mutableStateOf<String?>(null) }
    var dayForNewTask by remember { mutableStateOf(1) }

    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHost = remember { SnackbarHostState() }
    val messageText  = userMessage?.let { stringResource(it) }
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbarHost.showSnackbar(messageText)
            onMessageShown()
        }
    }

    fun autoSelectDay(newMonth: Int, newYear: Int): Int =
        autoSelectDayFor(newMonth, newYear, todayDay, todayMonth, todayYear)

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

    val detailTask = detailTaskId?.let { id -> tasks.find { it.occurrenceId == id } }

    detailTask?.let { task ->
        TaskDetailSheet(
            task = task,
            onDismiss = { detailTaskId = null },
            onToggle = onToggleTask,
            onEdit = { id ->
                detailTaskId = null
                editingTaskId = id
            }
        )
    }

    TaskFormSheet(
        show = showAddSheet || editingTaskId != null,
        editingTaskId = editingTaskId,
        editingTask = editingTask,
        dayForNewTask = dayForNewTask,
        currentMonth = currentMonth,
        currentYear = currentYear,
        sheetState = sheetState,
        onDismiss = {
            showAddSheet = false
            editingTaskId = null
        },
        onCreateTask = onCreateTask,
        onUpdateTask = onUpdateTask
    )

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title = stringResource(R.string.calendar_title),
                onBack = onBack
            )
        },
        snackbarHost  = { SnackbarHost(hostState = snackbarHost) },
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
            DayPilotCalendar(
                state = DayPilotCalendarState(
                    month = currentMonth,
                    year = currentYear,
                    taskDots = taskDots,
                    selectedDay = selectedDay
                ),
                actions = DayPilotCalendarActions(
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
            )

            selectedDay?.let { day ->
                SelectedDaySection(
                    selectedDay = day,
                    tasksForSelectedDay = tasksForSelectedDay,
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelect = { selectedDifficulty = it },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    onAddTask = {
                        dayForNewTask = selectedDay ?: 1
                        showAddSheet = true
                    },
                    onToggle = onToggleTask,
                    onTap = { detailTaskId = it },
                    onEdit = { editingTaskId = it },
                    onDelete = onDeleteTask
                )
            }
        }
    }
}
