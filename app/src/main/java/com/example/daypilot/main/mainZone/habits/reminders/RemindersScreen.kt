@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.daypilot.main.mainZone.habits.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RemindersScreen(
    vm: RemindersViewModel,
    hasNotifPermission: Boolean,
    onRequestNotif: () -> Unit,
    onBack: () -> Unit
) {

    // ========== State ==========

    val context = LocalContext.current
    val ui by vm.ui.collectAsState()

    var showInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    val sdf = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val exactOk = ReminderScheduler.canScheduleExact(context)

    // ========== UI ==========

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recordatorios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás") }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Default.Info, contentDescription = "Info") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editing = null
                    showSheet = true
                }
            ) { Text("+") }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val needsExactAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactOk

            if (!hasNotifPermission || needsExactAccess) {
                PermissionCenteredCard(
                    hasNotifPermission = hasNotifPermission,
                    needsExactAccess = needsExactAccess,
                    onRequestNotif = onRequestNotif,
                    onOpenExactSettings = {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                )
            } else {
                RemindersList(
                    ui = ui,
                    sdf = sdf,
                    onEdit = {
                        editing = it
                        showSheet = true
                    },
                    onDelete = { vm.delete(it) },
                    onToggleDaily = { r, enabled -> vm.setEnabled(r, enabled) }
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Cómo funciona") },
            text = {
                Text(
                    "• Una vez: eliges día y hora. Cuando suena, desaparece.\n" +
                            "• Diario: funciona como alarma diaria (tiene interruptor Activo).\n" +
                            "• Aviso previo: notifica 10 min antes y a la hora exacta.\n" +
                            "• Si activas el aviso y quedan <10 min, te avisa inmediatamente (solo una vez)."
                )
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } }
        )
    }

    if (showSheet) {
        CreateOrEditReminderSheet(
            vm = vm,
            initial = editing,
            onDismiss = {
                showSheet = false
                editing = null
            }
        )
    }
}

@Composable
private fun RemindersList(
    ui: RemindersUiState,
    sdf: SimpleDateFormat,
    onEdit: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onToggleDaily: (Reminder, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Activos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (ui.reminders.isEmpty()) {
            EmptyStateCentered(
                title = "No hay recordatorios",
                subtitle = "Pulsa + para crear uno nuevo."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ui.reminders) { r ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(r) },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        r.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (r.repeat == RepeatType.DAILY) "Diario" else "Una vez",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (r.repeat == RepeatType.DAILY) {
                                    Switch(
                                        checked = r.enabled,
                                        onCheckedChange = { onToggleDaily(r, it) }
                                    )
                                }
                            }

                            Text(
                                "Suena: ${sdf.format(Date(r.triggerAtMillis))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (r.preAlertMin > 0) {
                                Text(
                                    "Aviso previo: ${r.preAlertMin} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onDelete(r) }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCenteredCard(
    hasNotifPermission: Boolean,
    needsExactAccess: Boolean,
    onRequestNotif: () -> Unit,
    onOpenExactSettings: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Permisos necesarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (!hasNotifPermission) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Text("Activa notificaciones para ver los avisos.")
                    }
                    Button(onClick = onRequestNotif, modifier = Modifier.fillMaxWidth()) {
                        Text("Permitir notificaciones")
                    }
                }

                if (needsExactAccess) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null)
                        Text("Activa alarmas exactas para que suene a la hora exacta.")
                    }
                    Button(onClick = onOpenExactSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("Activar alarmas exactas")
                    }
                }

                Text(
                    "Cuando termines, vuelve aquí y crea tu recordatorio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCentered(title: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CreateOrEditReminderSheet(
    vm: RemindersViewModel,
    initial: Reminder?,
    onDismiss: () -> Unit
) {

    // ========== State ==========

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEdit = initial != null

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var mode by remember { mutableStateOf(initial?.repeat ?: RepeatType.ONCE) }
    var preAlert by remember { mutableStateOf((initial?.preAlertMin ?: 0) > 0) }
    var enabledDaily by remember { mutableStateOf(initial?.enabled ?: true) }

    var chosenOnceAt by remember {
        mutableStateOf(initial?.takeIf { it.repeat == RepeatType.ONCE }?.triggerAtMillis)
    }
    var chosenDailyHour by remember { mutableStateOf(initial?.hour) }
    var chosenDailyMin by remember { mutableStateOf(initial?.minute) }

    val preMin = if (preAlert) 10 else 0
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    // ========== UI ==========

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (isEdit) "Editar recordatorio" else "Nuevo recordatorio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Texto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { mins ->
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = mode == RepeatType.ONCE,
                        onClick = { chosenOnceAt = System.currentTimeMillis() + mins * 60_000L }
                    ) { Text("${mins}m") }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = mode == RepeatType.ONCE,
                    onClick = { mode = RepeatType.ONCE },
                    label = { Text("Una vez") }
                )
                FilterChip(
                    selected = mode == RepeatType.DAILY,
                    onClick = { mode = RepeatType.DAILY },
                    label = { Text("Diario") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Aviso previo", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Notifica 10 min antes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = preAlert, onCheckedChange = { preAlert = it })
            }

            if (mode == RepeatType.DAILY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activo", fontWeight = FontWeight.SemiBold)
                    Switch(checked = enabledDaily, onCheckedChange = { enabledDaily = it })
                }
            }

            if (mode == RepeatType.ONCE) {
                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal2 = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m)
                                    set(Calendar.DAY_OF_MONTH, d)
                                }

                                TimePickerDialog(
                                    context,
                                    { _, hh, mm ->
                                        cal2.set(Calendar.HOUR_OF_DAY, hh)
                                        cal2.set(Calendar.MINUTE, mm)
                                        cal2.set(Calendar.SECOND, 0)
                                        cal2.set(Calendar.MILLISECOND, 0)
                                        chosenOnceAt = cal2.timeInMillis
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Elegir día y hora") }

                Text(
                    chosenOnceAt?.let { "Suena: ${fmt.format(Date(it))}" } ?: "Sin hora seleccionada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (mode == RepeatType.DAILY) {
                Button(
                    onClick = {
                        val cal = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hh, mm ->
                                chosenDailyHour = hh
                                chosenDailyMin = mm
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Elegir hora") }

                Text(
                    if (chosenDailyHour != null && chosenDailyMin != null)
                        "Suena todos los días a: %02d:%02d".format(chosenDailyHour, chosenDailyMin)
                    else "Sin hora seleccionada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val canSave = when (mode) {
                RepeatType.ONCE -> chosenOnceAt != null
                RepeatType.DAILY -> chosenDailyHour != null && chosenDailyMin != null
            }

            Button(
                onClick = {
                    val finalTitle = title
                    val updated: Reminder = if (!isEdit) {
                        if (mode == RepeatType.ONCE) {
                            vm.buildOnce(finalTitle, chosenOnceAt!!, preMin)
                        } else {
                            vm.buildDaily(finalTitle, chosenDailyHour!!, chosenDailyMin!!, preMin)
                                .copy(enabled = enabledDaily)
                        }
                    } else {
                        val id = initial.id
                        if (mode == RepeatType.ONCE) {
                            Reminder(
                                id = id,
                                title = finalTitle.trim().ifBlank { "Recordatorio" },
                                repeat = RepeatType.ONCE,
                                enabled = true,
                                triggerAtMillis = chosenOnceAt!!,
                                preAlertMin = preMin,
                                lastPreSentForTriggerAt = 0L
                            )
                        } else {
                            val next = vm.computeNextDaily(chosenDailyHour!!, chosenDailyMin!!)
                            Reminder(
                                id = id,
                                title = finalTitle.trim().ifBlank { "Recordatorio" },
                                repeat = RepeatType.DAILY,
                                enabled = enabledDaily,
                                triggerAtMillis = next,
                                hour = chosenDailyHour,
                                minute = chosenDailyMin,
                                preAlertMin = preMin,
                                lastPreSentForTriggerAt = 0L
                            )
                        }
                    }

                    if (isEdit) vm.updateExisting(updated) else vm.saveNew(updated)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(if (isEdit) "Guardar cambios" else "Crear recordatorio")
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}