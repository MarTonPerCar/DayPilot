package com.example.daypilot.main.mainZone.habits.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsScreen(
    hasPermission: Boolean,
    ui: StepsUiState,
    onBack: () -> Unit,
    onChangeGoal: (Int) -> Unit,
    onRequestPermission: () -> Unit,
    onConsumeUploadMessage: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    var showGoalSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ui.uploadMessage) {
        ui.uploadMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onConsumeUploadMessage()
        }
    }

    var goalText by remember(ui.goalToday, ui.pendingGoalNextDay) {
        mutableStateOf((ui.pendingGoalNextDay ?: ui.goalToday).toString())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pasos") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Default.Info, "Info") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasPermission) {
                Text("Necesitamos permiso de actividad para leer pasos.")
                Button(onClick = onRequestPermission) { Text("Conceder permiso") }
                return@Column
            }

            if (!ui.hasSensor) {
                Text(ui.error ?: "No se detectó sensor de pasos.")
                return@Column
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { ui.progress },
                    strokeWidth = 12.dp,
                    modifier = Modifier.size(180.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${ui.stepsToday}", style = MaterialTheme.typography.headlineLarge)
                    Text("de ${ui.goalToday} pasos")
                    Text("${(ui.progress * 100f).roundToInt()}%")
                }
            }

            ui.pendingGoalNextDay?.let { next ->
                AssistChip(
                    onClick = { },
                    label = { Text("Meta nueva ($next) aplicada mañana") }
                )
            }

            OutlinedButton(
                onClick = {
                    goalText = (ui.pendingGoalNextDay ?: ui.goalToday).toString()
                    showGoalSheet = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar meta")
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Cómo funciona") },
            text = {
                Text(
                    "• Se cuentan pasos con el sensor del móvil.\n" +
                            "• La meta diaria da puntos al llegar al 50%, 75% y 100%.\n" +
                            "• Si cambias la meta con pasos ya contados, se aplica mañana.\n" +
                            "• Botón TEST: guarda en Firebase para verificar que todo va bien."
                )
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } }
        )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showGoalSheet = false }) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val value = goalText.toIntOrNull() ?: ui.goalToday
                        onChangeGoal(value)
                        showGoalSheet = false
                    }) { Text("Guardar") }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}