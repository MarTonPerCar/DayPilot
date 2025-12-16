package com.example.daypilot.main.mainZone.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot.main.mainZone.habits.steps.StepsUiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsHubScreen(
    onBack: () -> Unit,
    stepsUi: StepsUiState?,
    hasStepsPermission: Boolean,
    onRequestStepsPermission: () -> Unit,
    onChangeStepsGoal: (Int) -> Unit,
    onOpenSteps: () -> Unit, // opcional: pantalla detalle
    onOpenTechHealth: () -> Unit,
    onOpenReminders: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hábitos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Información")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StepsDashboard(
                ui = stepsUi,
                hasPermission = hasStepsPermission,
                onRequestPermission = onRequestStepsPermission,
                onChangeGoal = onChangeStepsGoal,
                onOpenSteps = onOpenSteps
            )

            HabitTile(
                title = "Salud tecnológica",
                subtitle = "Límites por app / grupo + avisos",
                icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                onClick = onOpenTechHealth
            )

            HabitTile(
                title = "Recordatorios",
                subtitle = "Avisos, timers y rutinas",
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                onClick = onOpenReminders
            )
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("¿Qué es Hábitos?") },
            text = {
                Text(
                    "• Pasos: progreso diario y puntos por hitos.\n" +
                            "• Salud tecnológica: límites de uso por apps/grupos.\n" +
                            "• Recordatorios: avisos y timers.\n\n" +
                            "Todo desde un panel."
                )
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StepsDashboard(
    ui: StepsUiState?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onChangeGoal: (Int) -> Unit,
    onOpenSteps: () -> Unit
) {
    var showGoalSheet by remember { mutableStateOf(false) }

    // Draft goal
    var goalText by remember(ui?.goalToday, ui?.pendingGoalNextDay) {
        mutableStateOf(((ui?.pendingGoalNextDay ?: ui?.goalToday) ?: 8000).toString())
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text("Pasos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
            }

            if (!hasPermission) {
                Text("Necesitas el permiso de actividad para contar pasos.")
                Button(onClick = onRequestPermission) { Text("Conceder permiso") }
                return@Column
            }

            val steps = ui?.stepsToday ?: 0
            val goal = (ui?.goalToday ?: 1).coerceAtLeast(1)
            val progress = (ui?.progress ?: 0f).coerceIn(0f, 1f)
            val remainingSteps = (goal - steps).coerceAtLeast(0)

            // 50% = 1, 75% = 1, 100% = 1 + bonus(3) => 4
            val ptsLeft =
                (if (ui?.m50Sent == true) 0 else 1) +
                        (if (ui?.m75Sent == true) 0 else 1) +
                        (if (ui?.m100Sent == true) 0 else 4)

            val nextMilestone = when {
                ui == null -> "—"
                !ui.m50Sent -> "50%"
                !ui.m75Sent -> "75%"
                !ui.m100Sent -> "100%"
                else -> "Completado"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // ✅ no deja que el bloque crezca infinito
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Círculo (izquierda)
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        strokeWidth = 12.dp,
                        modifier = Modifier.size(130.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$steps", style = MaterialTheme.typography.headlineMedium)
                        Text("de $goal", style = MaterialTheme.typography.bodySmall)
                        Text("${(progress * 100f).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Panel derecho
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), // ✅ ahora solo llena la altura del Row
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Resumen", fontWeight = FontWeight.SemiBold)

                        // ✅ chips que se adaptan a la fila (no vertical)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 3
                        ) {
                            AssistChip(
                                modifier = Modifier.widthIn(min = 72.dp),
                                onClick = {},
                                label = { Text(if (ui?.m50Sent == true) "✓ 50%" else "50%") }
                            )
                            AssistChip(
                                modifier = Modifier.widthIn(min = 72.dp),
                                onClick = {},
                                label = { Text(if (ui?.m75Sent == true) "✓ 75%" else "75%") }
                            )
                            AssistChip(
                                modifier = Modifier.widthIn(min = 72.dp),
                                onClick = {},
                                label = { Text(if (ui?.m100Sent == true) "✓ 100%" else "100%") }
                            )
                        }

                        Text("Restan: $remainingSteps pasos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Puntos restantes hoy: $ptsLeft", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Siguiente hito: $nextMilestone", color = MaterialTheme.colorScheme.onSurfaceVariant)

                        ui?.pendingGoalNextDay?.let { next ->
                            AssistChip(onClick = { }, label = { Text("Meta $next (mañana)") })
                        }

                        Spacer(Modifier.weight(1f)) // ✅ empuja el botón abajo SIN estirar toda la pantalla

                        OutlinedButton(
                            onClick = {
                                goalText = ((ui?.pendingGoalNextDay ?: ui?.goalToday) ?: 8000).toString()
                                showGoalSheet = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Configurar meta") }
                    }
                }
            }
        }
    }

    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoalSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Meta diaria", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Pasos objetivo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showGoalSheet = false }) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val value = goalText.toIntOrNull() ?: (ui?.goalToday ?: 8000)
                        onChangeGoal(value)
                        showGoalSheet = false
                    }) { Text("Guardar") }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HabitTile(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 86.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon()
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}