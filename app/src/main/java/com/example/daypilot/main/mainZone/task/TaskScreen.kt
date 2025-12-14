package com.example.daypilot.main.mainZone.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.Task
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty
import com.example.daypilot.firebaseLogic.taskLogic.TaskRepository
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    uid: String,
    taskRepo: TaskRepository,
    authRepo: AuthRepository,
    onBack: () -> Unit
) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var sortOption by remember { mutableStateOf(TaskSortOption.NEXT_DATE) }
    var difficultyFilter by remember { mutableStateOf<TaskDifficulty?>(null) }
    var statusFilter by remember { mutableStateOf(TaskStatusFilter.ALL) }

    var isSheetOpen by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    // Diálogo para confirmar completar
    var taskToConfirm by remember { mutableStateOf<Task?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun reloadTasks() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                tasks = taskRepo.getTasks(uid)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error cargando la tarea."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(uid) {
        reloadTasks()
    }

    val filteredSortedTasks = remember(tasks, sortOption, difficultyFilter, statusFilter) {
        tasks
            .filter { t ->
                val matchesDifficulty = difficultyFilter?.let { t.difficulty == it } ?: true
                val matchesStatus = when (statusFilter) {
                    TaskStatusFilter.ALL -> true
                    TaskStatusFilter.PENDING -> !t.isCompleted
                    TaskStatusFilter.COMPLETED -> t.isCompleted
                }
                matchesDifficulty && matchesStatus
            }
            .sortedWith(
                when (sortOption) {
                    TaskSortOption.DATE ->
                        compareBy<Task> { it.createdAt }

                    TaskSortOption.NAME ->
                        compareBy { it.title.lowercase() }

                    TaskSortOption.DURATION ->
                        compareBy { it.estimatedMinutes }

                    TaskSortOption.DIFFICULTY ->
                        compareBy { it.difficulty.ordinal }

                    TaskSortOption.NEXT_DATE ->
                        compareBy { task ->
                            nextDateMillis(task) ?: Long.MAX_VALUE
                        }
                }
            )
    }

    // Sheet crear/editar
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
                            errorMessage = e.localizedMessage ?: "Error guardando la tarea."
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
                                errorMessage = e.localizedMessage ?: "Error eliminando tarea."
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

    // Diálogo de confirmar completar
    taskToConfirm?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToConfirm = null },
            title = { Text("Completar tarea") },
            text = { Text("¿Quieres marcar \"${task.title.ifBlank { "esta tarea" }}\" como completada?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val t = task
                        taskToConfirm = null
                        scope.launch {
                            try {
                                taskRepo.completeTask(uid, t)
                                authRepo.addPoints(
                                    uid = uid,
                                    points = 2L,
                                    source = PointSource.TASKS,
                                    metadata = mapOf(
                                        "taskId" to t.id,
                                        "title" to t.title
                                    )
                                )
                                reloadTasks()
                            } catch (e: Exception) {
                                errorMessage = e.localizedMessage ?: "Error completando tarea."
                            }
                        }
                    }
                ) {
                    Text("Sí, completar")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🔹 AQUÍ el Scaffold con el content lambda final
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
    ) { innerPadding ->   // 👈 ESTE es el content del Scaffold
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

            TaskFiltersRow(
                sortOption = sortOption,
                onSortChange = { sortOption = it },
                difficultyFilter = difficultyFilter,
                onDifficultyFilterChange = { difficultyFilter = it },
                statusFilter = statusFilter,
                onStatusFilterChange = { statusFilter = it }
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
                                    taskToConfirm = task
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
