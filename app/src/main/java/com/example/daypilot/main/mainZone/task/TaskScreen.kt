package com.example.daypilot.main.mainZone.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty
import com.example.daypilot.firebaseLogic.taskLogic.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class TaskSortOption {
    DATE,
    NAME,
    DURATION,
    DIFFICULTY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    uid: String,
    taskRepo: TaskRepository,
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var sortOption by remember { mutableStateOf(TaskSortOption.DATE) }
    var difficultyFilter by remember { mutableStateOf<TaskDifficulty?>(null) }

    var isSheetOpen by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun reloadTasks() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                tasks = taskRepo.getTasks(uid)
            } catch (e: Exception) {
                errorMessage = "Error cargando tareas."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(uid) {
        reloadTasks()
    }

    val filteredSortedTasks = remember(tasks, sortOption, difficultyFilter) {
        tasks
            .filter { t ->
                difficultyFilter?.let { t.difficulty == it } ?: true
            }
            .sortedWith(
                when (sortOption) {
                    TaskSortOption.DATE -> compareBy { it.days.minOrNull() ?: "" }
                    TaskSortOption.NAME -> compareBy { it.title.lowercase() }
                    TaskSortOption.DURATION -> compareBy { it.estimatedMinutes }
                    TaskSortOption.DIFFICULTY -> compareBy { it.difficulty.ordinal }
                }
            )
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    isSheetOpen = false
                    editingTask = null
                }
            },
            sheetState = sheetState
        ) {
            TaskFormSheet(
                initialTask = editingTask,
                onSave = { taskToSave ->
                    scope.launch {
                        try {
                            if (taskToSave.id.isBlank()) {
                                taskRepo.createTask(uid, taskToSave)
                            } else {
                                taskRepo.updateTask(uid, taskToSave)
                            }
                            reloadTasks()
                        } catch (e: Exception) {
                            errorMessage = "Error guardando tarea."
                        } finally {
                            sheetState.hide()
                            isSheetOpen = false
                            editingTask = null
                        }
                    }
                },
                onDelete = { taskToDelete ->
                    if (taskToDelete != null && taskToDelete.id.isNotBlank()) {
                        scope.launch {
                            try {
                                taskRepo.deleteTask(uid, taskToDelete.id)
                                reloadTasks()
                            } catch (e: Exception) {
                                errorMessage = "Error eliminando tarea."
                            } finally {
                                sheetState.hide()
                                isSheetOpen = false
                                editingTask = null
                            }
                        }
                    }
                },
                onComplete = { taskToComplete ->
                    if (taskToComplete != null && !taskToComplete.isCompleted) {
                        scope.launch {
                            try {
                                taskRepo.completeTask(uid, taskToComplete)
                                reloadTasks()
                            } catch (e: Exception) {
                                errorMessage = "Error completando tarea."
                            } finally {
                                sheetState.hide()
                                isSheetOpen = false
                                editingTask = null
                            }
                        }
                    }
                },
                onCancel = {
                    scope.launch {
                        sheetState.hide()
                        isSheetOpen = false
                        editingTask = null
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    isSheetOpen = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Filtros y ordenación
            TaskFiltersRow(
                sortOption = sortOption,
                onSortChange = { sortOption = it },
                difficultyFilter = difficultyFilter,
                onDifficultyFilterChange = { difficultyFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredSortedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes tareas. ¡Pulsa + para crear una!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSortedTasks) { task ->
                        TaskRow(
                            task = task,
                            onClick = {
                                editingTask = task
                                isSheetOpen = true
                            },
                            onCompleteClick = {
                                if (!task.isCompleted) {
                                    scope.launch {
                                        try {
                                            taskRepo.completeTask(uid, task)
                                            reloadTasks()
                                        } catch (e: Exception) {
                                            errorMessage = "Error completando tarea."
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormSheet(
    initialTask: Task?,
    onSave: (Task) -> Unit,
    onDelete: (Task?) -> Unit,
    onComplete: (Task?) -> Unit,
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

    val categoryOptions = listOf("General", "Estudios", "Trabajo", "Salud", "Personal")
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
            fontWeight = FontWeight.Bold
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

        // Dificultad
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

        // Duración
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
                    text = "$estimatedMinutes min",
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

        // Días
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

        // Categoría
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
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Recordatorio (solo se guarda, las notis las haremos más adelante)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Recordatorio", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Se usará más adelante para notificaciones",
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

        // Botones inferiores
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

                    if (!initialTask.isCompleted) {
                        TextButton(onClick = { onComplete(initialTask) }) {
                            Text("Completar")
                        }
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

@Composable
private fun TaskFiltersRow(
    sortOption: TaskSortOption,
    onSortChange: (TaskSortOption) -> Unit,
    difficultyFilter: TaskDifficulty?,
    onDifficultyFilterChange: (TaskDifficulty?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ordenar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Ordenar por:", style = MaterialTheme.typography.bodyMedium)

            FilterChip(
                selected = sortOption == TaskSortOption.DATE,
                onClick = { onSortChange(TaskSortOption.DATE) },
                label = { Text("Fecha") }
            )
            FilterChip(
                selected = sortOption == TaskSortOption.NAME,
                onClick = { onSortChange(TaskSortOption.NAME) },
                label = { Text("Nombre") }
            )
            FilterChip(
                selected = sortOption == TaskSortOption.DURATION,
                onClick = { onSortChange(TaskSortOption.DURATION) },
                label = { Text("Duración") }
            )
            FilterChip(
                selected = sortOption == TaskSortOption.DIFFICULTY,
                onClick = { onSortChange(TaskSortOption.DIFFICULTY) },
                label = { Text("Dificultad") }
            )
        }

        // Filtro por dificultad
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Dificultad:", style = MaterialTheme.typography.bodyMedium)

            AssistChip(
                label = { Text("Todas") },
                onClick = { onDifficultyFilterChange(null) },
                leadingIcon = null,
                enabled = true
            )

            DifficultyChip(
                label = "Fácil",
                difficulty = TaskDifficulty.EASY,
                selected = difficultyFilter == TaskDifficulty.EASY,
                onClick = { onDifficultyFilterChange(TaskDifficulty.EASY) }
            )
            DifficultyChip(
                label = "Media",
                difficulty = TaskDifficulty.MEDIUM,
                selected = difficultyFilter == TaskDifficulty.MEDIUM,
                onClick = { onDifficultyFilterChange(TaskDifficulty.MEDIUM) }
            )
            DifficultyChip(
                label = "Difícil",
                difficulty = TaskDifficulty.HARD,
                selected = difficultyFilter == TaskDifficulty.HARD,
                onClick = { onDifficultyFilterChange(TaskDifficulty.HARD) }
            )
        }
    }
}

@Composable
private fun DifficultyChip(
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
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        fontWeight = FontWeight.SemiBold
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

                // Botón de completar
                OutlinedButton(
                    onClick = onCompleteClick,
                    enabled = !task.isCompleted
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hecho")
                    } else {
                        Text("Completar")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dificultad
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
                    leadingIcon = null,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = difficultyColor.copy(alpha = 0.2f)
                    )
                )

                // Duración
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${task.estimatedMinutes} min", style = MaterialTheme.typography.bodySmall)
                }

                // Día más próximo
                val nextDay = task.days.minOrNull()

                if (nextDay != null) {
                    Text(
                        text = "Próx: ${formatDisplayDate(nextDay)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (task.isCompleted) {
                    Text(
                        text = "Completada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatDisplayDate(isoDate: String): String {
    // isoDate = "yyyy-MM-dd"
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = parts[1]
            "$day/$month"
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}