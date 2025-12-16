package com.example.daypilot.main.mainZone.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty
import com.example.daypilot.firebaseLogic.taskLogic.TaskRepository
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uid: String,
    taskRepo: TaskRepository,
    onBack: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onOpenTasks: () -> Unit
) {

    // ========== State ==========

    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isoFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val today = remember { LocalDate.now() }

    val tasksByDate = remember(tasks) { buildTasksByDate(tasks, isoFormatter) }

    var selectedDate by remember { mutableStateOf(today) }
    val selectedTasks = remember(selectedDate, tasksByDate) { tasksByDate[selectedDate].orEmpty() }

    val scope = rememberCoroutineScope()
    var detailTask by remember { mutableStateOf<Task?>(null) }

    // ========== Data ==========

    fun reloadTasks() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                tasks = taskRepo.getTasks(uid)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error cargando las tareas."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(uid) { reloadTasks() }

    // ========== Calendar Config ==========

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(24) }
    val endMonth = remember { currentMonth.plusMonths(24) }

    val daysOfWeek = remember { daysOfWeek(firstDayOfWeekFromLocale()) }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val visibleMonth = remember { derivedStateOf { calendarState.firstVisibleMonth.yearMonth } }

    // ========== UI ==========

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onOpenTasks) {
                        Text("Tareas")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            MonthHeader(
                yearMonth = visibleMonth.value,
                onPrev = {
                    scope.launch {
                        calendarState.animateScrollToMonth(visibleMonth.value.minusMonths(1))
                    }
                },
                onNext = {
                    scope.launch {
                        calendarState.animateScrollToMonth(visibleMonth.value.plusMonths(1))
                    }
                }
            )

            DaysOfWeekRow(daysOfWeek = daysOfWeek)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            CalendarDayCell(
                                day = day,
                                today = today,
                                selectedDate = selectedDate,
                                onSelect = { selectedDate = it },
                                tasksForDay = tasksByDate[day.date].orEmpty()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

            SummaryHeader(selectedDate = selectedDate, today = today)

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                selectedTasks.isEmpty() -> {
                    Text(
                        text = "No hay tareas para este día.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedTasks, key = { it.id }) { task ->
                            CalendarTaskSummaryRow(
                                task = task,
                                onClick = { detailTask = task }
                            )
                        }
                    }
                }
            }
        }

        detailTask?.let { t ->
            CalendarTaskDetailDialog(
                task = t,
                today = today,
                onDismiss = { detailTask = null },
                onOpenInTasks = {
                    detailTask = null
                    onOpenTask(t.id)
                }
            )
        }
    }
}

private fun buildTasksByDate(
    tasks: List<Task>,
    isoFormatter: DateTimeFormatter
): Map<LocalDate, List<Task>> {
    val map = mutableMapOf<LocalDate, MutableList<Task>>()
    for (t in tasks) {
        for (iso in t.days) {
            val d = runCatching { LocalDate.parse(iso, isoFormatter) }.getOrNull() ?: continue
            map.getOrPut(d) { mutableListOf() }.add(t)
        }
    }
    return map
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = remember(yearMonth) {
        yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
        }

        Text(
            text = "$monthName ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
private fun DaysOfWeekRow(daysOfWeek: List<java.time.DayOfWeek>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEach { dow ->
            Text(
                text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replace(".", ""),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(44.dp),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    today: LocalDate,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    tasksForDay: List<Task>
) {
    val isInMonth = day.position == DayPosition.MonthDate
    val isSelected = isInMonth && day.date == selectedDate
    val isToday = isInMonth && day.date == today

    val pendingOverdue = isInMonth &&
            day.date.isBefore(today) &&
            tasksForDay.any { !it.isCompleted }

    val count = if (isInMonth) tasksForDay.size else 0

    val markColor = when {
        pendingOverdue -> MaterialTheme.colorScheme.error
        count >= 8 -> MaterialTheme.colorScheme.tertiary
        count in 1..7 -> MaterialTheme.colorScheme.secondary
        else -> null
    }

    val textColor = when {
        !isInMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    markColor != null -> markColor.copy(alpha = 0.18f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(enabled = isInMonth) { onSelect(day.date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )

        if (isInMonth && markColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(markColor)
            )
        }

        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            )
        }
    }
}

@Composable
private fun SummaryHeader(selectedDate: LocalDate, today: LocalDate) {
    val label = remember(selectedDate, today) {
        if (selectedDate == today) "Tareas de hoy"
        else "Tareas del ${selectedDate.dayOfMonth}/${selectedDate.monthValue}"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun CalendarTaskSummaryRow(
    task: Task,
    onClick: () -> Unit
) {
    val accent = when (task.difficulty) {
        TaskDifficulty.EASY -> MaterialTheme.colorScheme.tertiary
        TaskDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        TaskDifficulty.HARD -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = task.title.ifBlank { "(Sin título)" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
