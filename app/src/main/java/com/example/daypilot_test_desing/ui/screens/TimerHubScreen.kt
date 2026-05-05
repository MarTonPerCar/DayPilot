package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.model.TimerOption
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.TimerHubCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerHubScreen(
    onNavigateToTimer: (id: String, minutes: Int) -> Unit,
    onNavigateToPomodoro: (sessions: Int) -> Unit,
    onBack: () -> Unit
) {
    var showCustomSheet   by remember { mutableStateOf(false) }
    var showPomodoroSheet by remember { mutableStateOf(false) }
    var customMinutes     by remember { mutableFloatStateOf(30f) }
    var pomodoroSessions  by remember { mutableFloatStateOf(4f) }
    val sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val timers = listOf(
        TimerOption(
            id              = "POMODORO",
            labelRes        = R.string.timer_pomodoro,
            descriptionRes  = R.string.timer_pomodoro_desc,
            icon            = Icons.Default.Timer,
            accentColor     = Color(0xFFE53935),
            isPomodoro      = true
        ),
        TimerOption(
            id              = "TRAINING",
            labelRes        = R.string.timer_training,
            descriptionRes  = R.string.timer_training_desc,
            icon            = Icons.Default.FitnessCenter,
            accentColor     = Color(0xFF43A047),
            durationMinutes = 90
        ),
        TimerOption(
            id              = "MEDITATION",
            labelRes        = R.string.timer_meditation,
            descriptionRes  = R.string.timer_meditation_desc,
            icon            = Icons.Default.SelfImprovement,
            accentColor     = Color(0xFF5E35B1),
            durationMinutes = 60
        ),
        TimerOption(
            id              = "COOKING",
            labelRes        = R.string.timer_cooking,
            descriptionRes  = R.string.timer_cooking_desc,
            icon            = Icons.Default.Restaurant,
            accentColor     = Color(0xFFFF8F00),
            durationMinutes = 120
        ),
        TimerOption(
            id              = "CUSTOM",
            labelRes        = R.string.timer_custom,
            descriptionRes  = R.string.timer_custom_desc,
            icon            = Icons.Default.Tune,
            accentColor     = Color(0xFF00ACC1),
            isCustom        = true
        )
    )

    // ── Custom sheet ─────────────────────────────────────────────
    if (showCustomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCustomSheet = false },
            sheetState       = sheetState,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor   = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text       = "Cronómetro personalizado",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = "${customMinutes.toInt()} minutos",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF00ACC1)
                    )
                }
                Slider(
                    value         = customMinutes,
                    onValueChange = { customMinutes = it },
                    valueRange    = 5f..180f,
                    steps         = 34,
                    colors        = SliderDefaults.colors(
                        thumbColor       = Color(0xFF00ACC1),
                        activeTrackColor = Color(0xFF00ACC1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 min",  style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("3 h",    style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Presets rápidos
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h", 90 to "1.5h").forEach { (min, label) ->
                        val isSelected = customMinutes.toInt() == min
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF00ACC1)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { customMinutes = min.toFloat() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = label,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Button(
                    onClick  = {
                        showCustomSheet = false
                        onNavigateToTimer("CUSTOM", customMinutes.toInt())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00ACC1)
                    )
                ) {
                    Text("Iniciar", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Pomodoro sheet ────────────────────────────────────────────
    if (showPomodoroSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPomodoroSheet = false },
            sheetState       = sheetState,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor   = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text       = "Configurar Pomodoro",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )

                // Info sesión
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = Color(0xFFE53935).copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔴", fontSize = 20.sp)
                            Text(
                                text  = "25 min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE53935)
                            )
                            Text(
                                text  = "Trabajo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔵", fontSize = 20.sp)
                            Text(
                                text  = "5 min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E88E5)
                            )
                            Text(
                                text  = "Descanso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⏱", fontSize = 20.sp)
                            Text(
                                text  = "${pomodoroSessions.toInt() * 30} min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text  = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sesiones
                Text(
                    text  = "Número de sesiones",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = "${pomodoroSessions.toInt()} sesiones",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFFE53935)
                    )
                }
                Slider(
                    value         = pomodoroSessions,
                    onValueChange = { pomodoroSessions = it },
                    valueRange    = 1f..8f,
                    steps         = 6,
                    colors        = SliderDefaults.colors(
                        thumbColor       = Color(0xFFE53935),
                        activeTrackColor = Color(0xFFE53935)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1 sesión (30 min)",  style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("8 sesiones (4h)", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick  = {
                        showPomodoroSheet = false
                        onNavigateToPomodoro(pomodoroSessions.toInt())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Iniciar Pomodoro", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = "Cronómetros",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            timers.forEach { timer ->
                TimerHubCard(
                    timer   = timer,
                    onClick = {
                        when {
                            timer.isPomodoro -> showPomodoroSheet = true
                            timer.isCustom   -> showCustomSheet   = true
                            else             -> onNavigateToTimer(timer.id, timer.durationMinutes)
                        }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
}