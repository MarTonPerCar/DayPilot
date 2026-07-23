package com.example.daypilot_test_desing.feature.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.connectivity.isConnectivityError
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val taskRepo: TaskRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeSubscribing = false
    private var refreshing = false

    init { refresh() }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        _uiState.update { it.copy(isLoading = true) }
        if (!ConnectivityState.ensureOnline()) {
            _uiState.update { it.copy(isLoading = false) }
            return false
        }
        return try {
            val tasks = taskRepo.getTasks()
            _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            subscribeToRealtimeOnce()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load calendar tasks", e)
            _uiState.update { it.copy(isLoading = false) }
            false
        }
    }

    // Realtime can only subscribe to base tables, but getTasks() reads a joined
    // tasks+task_days view — so both base tables are watched here instead.
    //
    // realtimeSubscribing is set synchronously because load() runs concurrently from both
    // init/refresh() and the startup awaitLoad() join — without it, both calls could see
    // realtimeChannel == null and each try to join a channel with the same topic name, and the
    // second one crashes ("cannot call postgresChangeFlow after joining the channel").
    private fun subscribeToRealtimeOnce() {
        if (realtimeChannel != null || realtimeSubscribing) return
        realtimeSubscribing = true
        val uid = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            val channel = supabase.channel("tasks-$uid")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "tasks"
                filter("user_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "task_days"
                filter("user_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.subscribe()
            realtimeChannel = channel
        }
    }

    private fun refreshFromRealtime() {
        if (refreshing) return // a burst of changes shouldn't queue up overlapping fetches
        refreshing = true
        SessionCache.tasks.value = null // getTasks() short-circuits on cache otherwise
        viewModelScope.launch {
            try {
                load()
            } finally {
                refreshing = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { runCatching { realtimeChannel?.unsubscribe() } }
    }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun addTask(data: NewTaskData) {
        val fakeId = "pending_${System.currentTimeMillis()}"
        val placeholder = CalendarTaskData(
            id          = fakeId,
            day         = data.day,
            month       = data.month,
            year        = data.year,
            title       = data.title,
            category    = data.category,
            difficulty  = data.difficulty,
            duration    = data.duration,
            isDone      = false,
            description = data.description.ifBlank { null },
            isRecurring = data.isRecurring,
            hasReminder = data.hasReminder,
            isPending   = true
        )
        _uiState.update { it.copy(tasks = it.tasks + placeholder) }

        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state -> state.copy(tasks = state.tasks.filter { it.id != fakeId }) }
                return@launch
            }
            try {
                taskRepo.addTask(data)
                load()
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create task '${data.title}' (recurring=${data.isRecurring})", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks.filter { it.id != fakeId },
                        userMessage = R.string.error_task_create
                    )
                }
            }
        }
    }

    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int, description: String = "") {
        val original = _uiState.value.tasks.find { it.id == id }
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { task ->
                if (task.id == id) task.copy(
                    title       = title,
                    category    = category,
                    difficulty  = difficulty,
                    duration    = duration,
                    description = description.ifBlank { null }
                ) else task
            })
        }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                if (original != null) {
                    _uiState.update { state -> state.copy(tasks = state.tasks.map { if (it.id == id) original else it }) }
                }
                return@launch
            }
            try {
                taskRepo.updateTask(id, title, category, difficulty, duration, description)
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task $id", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(
                        tasks = if (original != null)
                            state.tasks.map { if (it.id == id) original else it }
                        else
                            state.tasks,
                        userMessage = R.string.error_task_update
                    )
                }
            }
        }
    }

    fun toggleTask(occurrenceId: String, isDone: Boolean) {
        val original = _uiState.value.tasks.firstOrNull { it.occurrenceId == occurrenceId } ?: return
        // isEarned is sticky (unlike isDone) — points are paid at most once per occurrence.
        val shouldAwardPoints = isDone && !original.isEarned
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map {
                if (it.occurrenceId == occurrenceId)
                    it.copy(isDone = isDone, isEarned = it.isEarned || shouldAwardPoints)
                else it
            })
        }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state -> state.copy(tasks = state.tasks.map { if (it.occurrenceId == occurrenceId) original else it }) }
                return@launch
            }
            try {
                taskRepo.toggleTask(occurrenceId, isDone)
                if (shouldAwardPoints) {
                    progressRepo.logPoints(20, "TASKS")
                    // TASK_COMPLETED notification is now inserted by a Supabase DB trigger.
                }
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle task occurrence $occurrenceId to isDone=$isDone", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks.map { if (it.occurrenceId == occurrenceId) original else it },
                        userMessage = R.string.error_task_toggle
                    )
                }
            }
        }
    }

    fun deleteTask(id: String) {
        val snapshot = _uiState.value.tasks
        // isEarned stays sticky here too — deleting a task never claws back points.
        _uiState.update { state -> state.copy(tasks = state.tasks.filter { it.id != id }) }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { it.copy(tasks = snapshot) }
                return@launch
            }
            try {
                taskRepo.deleteTask(id)
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task $id", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { it.copy(tasks = snapshot, userMessage = R.string.error_task_delete) }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        private const val TAG = "CalendarViewModel"

        fun factory(taskRepo: TaskRepository, progressRepo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CalendarViewModel(taskRepo, progressRepo) as T
            }
    }
}
