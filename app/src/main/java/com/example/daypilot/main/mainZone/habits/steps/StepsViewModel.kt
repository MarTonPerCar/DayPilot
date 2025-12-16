package com.example.daypilot.main.mainZone.habits.steps

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import com.example.daypilot.firebaseLogic.pointsLogic.PointsRepository
import com.example.daypilot.firebaseLogic.pointsLogic.PointsTime
import com.example.daypilot.mainDatabase.StepsLocalState
import com.example.daypilot.mainDatabase.StepsLocalStore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId

data class StepsUiState(
    val hasSensor: Boolean = true,
    val error: String? = null,
    val stepsToday: Int = 0,
    val goalToday: Int = 2000,
    val progress: Float = 0f,
    val dayKey: String = "",
    val pendingGoalNextDay: Int? = null,
    val uploadMessage: String? = null,

    val m50Sent: Boolean = false,
    val m75Sent: Boolean = false,
    val m100Sent: Boolean = false,
)


@RequiresApi(Build.VERSION_CODES.O)
class StepsViewModel(
    private val appContext: Context,
    private val uid: String,
    private val zoneId: ZoneId,
    private val pointsRepo: PointsRepository = PointsRepository(),
) : ViewModel() {

    private val local = StepsLocalStore(appContext)
    private val sensor = StepsSensor(appContext)
    private val firestore = FirebaseFirestore.getInstance()

    private val _ui = MutableStateFlow(StepsUiState(dayKey = PointsTime.todayKey(zoneId)))
    val ui: StateFlow<StepsUiState> = _ui.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true

        // 1) Local store stream
        viewModelScope.launch {
            local.flow.collect { ls ->
                val todayKey = PointsTime.todayKey(zoneId)

                _ui.update {
                    it.copy(
                        goalToday = ls.goalToday,
                        dayKey = todayKey,
                        pendingGoalNextDay = if (ls.goalNextDay != ls.goalToday) ls.goalNextDay else null,

                        m50Sent = ls.m50Sent,
                        m75Sent = ls.m75Sent,
                        m100Sent = ls.m100Sent
                    )
                }
            }
        }

        // 2) Sensor stream
        viewModelScope.launch {
            sensor.counterFlow()
                .catch { e ->
                    _ui.update { it.copy(hasSensor = false, error = e.message) }
                }
                .collect { counter ->
                    onCounter(counter)
                }
        }
    }

    private suspend fun ensureDayInitialized(counter: Long): StepsLocalState {
        val todayKey = PointsTime.todayKey(zoneId)
        val ls = local.flow.first()

        if (ls.dayKey != todayKey || ls.dayKey.isBlank()) {
            val goalToday = ls.goalNextDay.takeIf { it > 0 } ?: ls.goalToday
            local.setDay(
                dayKey = todayKey,
                baseline = counter,
                goalToday = goalToday,
                goalNext = goalToday
            )
            return local.flow.first()
        }
        return ls
    }

    private suspend fun onCounter(counter: Long) {
        val ls = ensureDayInitialized(counter)

        val stepsToday = (counter - ls.baselineCounter).coerceAtLeast(0L).toInt()
        val goal = ls.goalToday.coerceAtLeast(1)
        val p = (stepsToday.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

        _ui.update { it.copy(stepsToday = stepsToday, progress = p, error = null, hasSensor = true) }
        checkMilestones(stepsToday, goal, ls)
    }

    private suspend fun checkMilestones(stepsToday: Int, goal: Int, ls: StepsLocalState) {
        val pct = stepsToday.toFloat() / goal.toFloat()

        val pts50 = 1L
        val pts75 = 1L
        val pts100 = 1L
        val bonusOn100 = 3L

        if (pct >= 0.5f && !ls.m50Sent) {
            award(pts50, "50")
            local.markMilestone50()
        }
        if (pct >= 0.75f && !ls.m75Sent) {
            award(pts75, "75")
            local.markMilestone75()
        }
        if (pct >= 1.0f && !ls.m100Sent) {
            award(pts100 + bonusOn100, "100")
            local.markMilestone100()
        }
    }

    private suspend fun award(points: Long, milestone: String) {
        pointsRepo.addPoints(
            uid = uid,
            points = points,
            source = PointSource.STEPS,
            metadata = mapOf("milestone" to milestone)
        )
    }

    fun setGoal(newGoal: Int) {
        val value = newGoal.coerceIn(10, 1_000_000)
        viewModelScope.launch {
            val currentSteps = ui.value.stepsToday
            if (currentSteps > 0) {
                local.setGoalNextDay(value)
                _ui.update { it.copy(pendingGoalNextDay = value) }
            } else {
                val ls = local.flow.first()
                local.setDay(
                    dayKey = PointsTime.todayKey(zoneId),
                    baseline = ls.baselineCounter,
                    goalToday = value,
                    goalNext = value
                )
            }
        }
    }

    fun uploadStepsToFirebaseNow() {
        viewModelScope.launch {
            try {
                val dayKey = ui.value.dayKey.ifBlank { PointsTime.todayKey(zoneId) }
                val steps = ui.value.stepsToday
                val goal = ui.value.goalToday

                val data = mapOf(
                    "dayKey" to dayKey,
                    "steps" to steps,
                    "goal" to goal,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                firestore
                    .collection("users")
                    .document(uid)
                    .collection("daily_steps")
                    .document(dayKey)
                    .set(data, SetOptions.merge())

                _ui.update { it.copy(uploadMessage = "Subido a Firebase: $steps pasos ($dayKey)") }
            } catch (e: Exception) {
                _ui.update { it.copy(uploadMessage = "Error subiendo: ${e.message ?: e::class.java.simpleName}") }
            }
        }
    }

    fun consumeUploadMessage() {
        _ui.update { it.copy(uploadMessage = null) }
    }
}