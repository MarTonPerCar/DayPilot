@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.daypilot.main.mainZone.habits.tech

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun TechHealthScreen(
    vm: TechHealthViewModel,
    hasNotifPermission: Boolean,
    onRequestNotif: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val s by vm.state.collectAsState()

    var showInfo by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Restriction?>(null) }

    LaunchedEffect(Unit) {
        vm.bootstrap()
        vm.rolloverIfNeeded()
        vm.refreshAccess()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Salud tecnológica") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        },
        floatingActionButton = {
            if (s.hasUsageAccess == true) {
                FloatingActionButton(onClick = { showCreateSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (s.hasUsageAccess) {
                null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                false -> {
                    PermissionCenteredCard(
                        hasNotifPermission = hasNotifPermission,
                        onRequestNotif = onRequestNotif,
                        onOpenUsageSettings = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        onRefresh = { vm.refreshAccess() }
                    )
                }
                true -> {
                    TechHealthMainContent(
                        state = s,
                        vm = vm,
                        onEdit = { editing = it }
                    )
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Cómo funciona") },
            text = {
                Text(
                    "• Cuenta SOLO la app que está en primer plano (top app).\n" +
                            "• Creas restricciones por APP o por GRUPO.\n" +
                            "• Cambios: hoy editas, y entra en vigor mañana.\n" +
                            "• Puedes elegir “solo contar” (sin notificaciones).\n" +
                            "• Las notificaciones se repiten entre 5s y 60s."
                )
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("OK") } }
        )
    }

    if (showCreateSheet) {
        RestrictionEditorSheet(vm = vm, initial = null, onDismiss = { showCreateSheet = false })
    }

    editing?.let {
        RestrictionEditorSheet(vm = vm, initial = it, onDismiss = { editing = null })
    }
}

@Composable
private fun PermissionCenteredCard(
    hasNotifPermission: Boolean,
    onRequestNotif: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                Text("Permisos necesarios", fontWeight = FontWeight.SemiBold)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Text("Activa “Acceso al uso” para poder medir el tiempo.")
                }

                Button(onClick = onOpenUsageSettings, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir ajustes de uso")
                }

                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Ya lo activé, refrescar")
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Text("Recomendado: permite notificaciones para avisos.")
                }

                Button(
                    onClick = onRequestNotif,
                    enabled = !hasNotifPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (hasNotifPermission) "Notificaciones activadas" else "Permitir notificaciones")
                }
            }
        }
    }
}

@Composable
private fun TechHealthMainContent(
    state: TechHealthState,
    vm: TechHealthViewModel,
    onEdit: (Restriction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Restricciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${state.restrictions.size} en total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.restrictions.isEmpty()) {
            EmptyState(title = "No hay restricciones", subtitle = "Pulsa + para crear una.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.restrictions) { r ->
                    RestrictionCard(r = r, state = state, vm = vm, onEdit = { onEdit(r) })
                }
            }
        }
    }
}

@Composable
private fun RestrictionCard(
    r: Restriction,
    state: TechHealthState,
    vm: TechHealthViewModel,
    onEdit: () -> Unit
) {
    val usedMin = remember(state.usageToday, r) {
        when (r.type) {
            RestrictionType.APP -> ((state.usageToday[r.targetId] ?: 0L) / 60000L).toInt()
            RestrictionType.GROUP -> {
                val g = state.groups.firstOrNull { it.id == r.targetId }
                g?.appPkgs?.sumOf { pkg -> ((state.usageToday[pkg] ?: 0L) / 60000L).toInt() } ?: 0
            }
        }
    }

    val over = usedMin >= r.activeLimitMin && r.activeEnabled

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (r.type == RestrictionType.APP) {
                    val icon = vm.appIcon(r.targetId)
                    if (icon != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(icon).build(),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Box(Modifier.size(36.dp))
                    }
                } else {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(28.dp))
                }

                Column(Modifier.weight(1f)) {
                    Text(r.displayName, fontWeight = FontWeight.SemiBold)
                    AssistChip(
                        onClick = {},
                        label = { Text(if (r.type == RestrictionType.APP) "App" else "Grupo") }
                    )
                }

                val effectiveEnabledTomorrow = r.pendingEnabled ?: r.activeEnabled
                Switch(
                    checked = effectiveEnabledTomorrow,
                    onCheckedChange = { checked ->
                        vm.setRestrictionPending(r, newEnabled = checked)
                    }
                )
            }

            val progress = if (r.activeLimitMin <= 0) 0f
            else (usedMin.toFloat() / r.activeLimitMin.toFloat()).coerceIn(0f, 1f)

            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

            Text(
                "Hoy: $usedMin / ${r.activeLimitMin} min" + if (over) "  •  ⚠️" else "",
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                if (r.activeNotifyEnabled) "Notif: cada ${r.activeNotifyEverySec}s" else "Solo contar (sin notificaciones)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (
                r.pendingSinceDayKey != null &&
                (r.pendingLimitMin != null ||
                        r.pendingEnabled != null ||
                        r.pendingNotifyEnabled != null ||
                        r.pendingNotifyEverySec != null)
            ) {
                val pendParts = buildList {
                    r.pendingLimitMin?.let { add("límite ${it} min") }
                    r.pendingEnabled?.let { add(if (it) "activar" else "desactivar") }
                    r.pendingNotifyEnabled?.let { add(if (it) "notif ON" else "solo contar") }
                    r.pendingNotifyEverySec?.let { add("cada ${it}s") }
                }.joinToString(", ")

                Text(
                    "Cambio mañana: $pendParts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Editar") }
                TextButton(
                    onClick = { vm.scheduleDisableTomorrow(r) },
                    modifier = Modifier.weight(1f)
                ) { Text("Eliminar (mañana)") }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RestrictionEditorSheet(
    vm: TechHealthViewModel,
    initial: Restriction?,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    val s by vm.state.collectAsState()

    val isEdit = initial != null

    var type by remember { mutableStateOf(initial?.type ?: RestrictionType.APP) }

    var selectedPkg by remember {
        mutableStateOf(if (initial?.type == RestrictionType.APP) initial.targetId else "")
    }
    var selectedAppName by remember {
        mutableStateOf(if (initial?.type == RestrictionType.APP) initial.displayName else "")
    }

    var selectedGroupId by remember {
        mutableStateOf(if (initial?.type == RestrictionType.GROUP) initial.targetId else "")
    }
    var groupName by remember { mutableStateOf("") }
    var groupApps by remember { mutableStateOf(setOf<String>()) }

    val appMin = 30
    val appMax = 360
    val groupMin = 60
    val groupMax = 600

    var limitMin by remember {
        mutableStateOf(initial?.activeLimitMin ?: if (type == RestrictionType.APP) 60 else 120)
    }

    LaunchedEffect(type) {
        if (!isEdit) limitMin = if (type == RestrictionType.APP) 60 else 120
    }

    var notifyEnabled by remember {
        mutableStateOf(initial?.activeNotifyEnabled ?: true)
    }
    var notifyEverySec by remember {
        mutableStateOf((initial?.activeNotifyEverySec ?: 60).coerceIn(5, 60))
    }

    var showAppPicker by remember { mutableStateOf(false) }
    var showGroupPickerApps by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (isEdit) "Editar restricción" else "Nueva restricción", fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = type == RestrictionType.APP,
                    onClick = { if (!isEdit) type = RestrictionType.APP },
                    enabled = !isEdit,
                    label = { Text("App") }
                )
                FilterChip(
                    selected = type == RestrictionType.GROUP,
                    onClick = { if (!isEdit) type = RestrictionType.GROUP },
                    enabled = !isEdit,
                    label = { Text("Grupo") }
                )
            }

            if (isEdit) {
                Text(
                    "Los cambios se aplican mañana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Notificaciones", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (notifyEnabled) "Avisar cuando supere el límite" else "Solo contar, sin notificar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = notifyEnabled, onCheckedChange = { notifyEnabled = it })
                    }

                    if (notifyEnabled) {
                        IntervalSliderSeconds(
                            label = "Repetir notificación (5s → 60s)",
                            value = notifyEverySec.coerceIn(5, 60),
                            onValue = { notifyEverySec = it.coerceIn(5, 60) }
                        )
                    }
                }
            }

            if (type == RestrictionType.APP) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Aplicación", fontWeight = FontWeight.SemiBold)

                        if (selectedPkg.isBlank()) {
                            OutlinedButton(onClick = { showAppPicker = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Elegir app")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val icon = vm.appIcon(selectedPkg)
                                if (icon != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(ctx).data(icon).build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        selectedAppName.ifBlank { vm.appLabel(selectedPkg) },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        selectedPkg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { showAppPicker = true }) { Text("Cambiar") }
                            }
                        }
                    }
                }

                LimitSlider(
                    label = "Límite diario (30 min → 6 h)",
                    min = appMin,
                    max = appMax,
                    step = 15,
                    value = limitMin.coerceIn(appMin, appMax),
                    onValue = { limitMin = it }
                )
            }

            if (type == RestrictionType.GROUP) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Grupo", fontWeight = FontWeight.SemiBold)

                        if (!isEdit && selectedGroupId.isBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { showCreateGroup = true },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Crear grupo") }

                                OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier.weight(1f),
                                    enabled = s.groups.isNotEmpty()
                                ) { Text("Usar existente") }
                            }

                            if (s.groups.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    s.groups.forEach { g ->
                                        val selected = selectedGroupId == g.id
                                        AssistChip(
                                            onClick = {
                                                selectedGroupId = g.id
                                                groupName = g.name
                                                groupApps = g.appPkgs
                                            },
                                            label = { Text(if (selected) "✓ ${g.name}" else g.name) }
                                        )
                                    }
                                }
                            }
                        } else {
                            if (selectedGroupId.isBlank() && initial?.type == RestrictionType.GROUP) {
                                selectedGroupId = initial.targetId
                                groupName = vm.groupName(selectedGroupId)
                                groupApps = vm.groupApps(selectedGroupId)
                            }

                            Text(groupName.ifBlank { vm.groupName(selectedGroupId) })
                            Text(
                                "${groupApps.size} apps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { showGroupPickerApps = true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Editar apps") }
                        }
                    }
                }

                LimitSlider(
                    label = "Límite diario (1 h → 10 h)",
                    min = groupMin,
                    max = groupMax,
                    step = 15,
                    value = limitMin.coerceIn(groupMin, groupMax),
                    onValue = { limitMin = it }
                )
            }

            val canSave = when (type) {
                RestrictionType.APP -> selectedPkg.isNotBlank()
                RestrictionType.GROUP -> selectedGroupId.isNotBlank() && groupApps.isNotEmpty()
            }

            Button(
                onClick = {
                    val safeRepeat = notifyEverySec.coerceIn(5, 60)

                    if (!isEdit) {
                        val id = UUID.randomUUID().toString()
                        val name = when (type) {
                            RestrictionType.APP -> selectedAppName.ifBlank { vm.appLabel(selectedPkg) }
                            RestrictionType.GROUP -> groupName.ifBlank { vm.groupName(selectedGroupId) }
                        }
                        val target = if (type == RestrictionType.APP) selectedPkg else selectedGroupId

                        if (type == RestrictionType.GROUP) {
                            vm.updateGroupApps(selectedGroupId, groupApps)
                        }

                        vm.createRestriction(
                            Restriction(
                                id = id,
                                type = type,
                                targetId = target,
                                displayName = name,
                                activeEnabled = true,
                                activeLimitMin = limitMin,
                                activeNotifyEnabled = notifyEnabled,
                                activeNotifyEverySec = safeRepeat
                            )
                        )
                    } else {
                        if (initial.type == RestrictionType.GROUP) {
                            vm.updateGroupApps(initial.targetId, groupApps.ifEmpty { vm.groupApps(
                                initial.targetId) })
                        }

                        vm.setRestrictionPending(
                            initial,
                            newLimitMin = limitMin,
                            newNotifyEnabled = notifyEnabled,
                            newNotifyEverySec = safeRepeat
                        )
                    }

                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isEdit) "Guardar (mañana)" else "Crear") }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (showAppPicker) {
        AppPickerSheet(
            apps = s.appsCatalog,
            initiallySelected = selectedPkg,
            onClose = { showAppPicker = false },
            onPick = { app ->
                selectedPkg = app.packageName
                selectedAppName = app.label
                showAppPicker = false
            }
        )
    }

    if (showCreateGroup) {
        CreateGroupDialog(
            onDismiss = { showCreateGroup = false },
            onCreate = { name ->
                val id = vm.createGroup(name)
                selectedGroupId = id
                groupName = name
                groupApps = emptySet()
                showCreateGroup = false
                showGroupPickerApps = true
            }
        )
    }

    if (showGroupPickerApps) {
        MultiAppPickerSheet(
            apps = s.appsCatalog,
            selected = groupApps,
            title = "Apps del grupo",
            onClose = { showGroupPickerApps = false },
            onDone = { picked ->
                groupApps = picked
                if (selectedGroupId.isNotBlank()) vm.updateGroupApps(selectedGroupId, picked)
                showGroupPickerApps = false
            }
        )
    }
}

@Composable
private fun IntervalSliderSeconds(
    label: String,
    value: Int,
    onValue: (Int) -> Unit
) {
    val min = 5
    val max = 60
    val step = 5

    val stepsCount = ((max - min) / step) + 1
    val sliderSteps = (stepsCount - 2).coerceAtLeast(0)

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("Seleccionado: ${value}s", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Slider(
                value = value.toFloat(),
                onValueChange = { raw ->
                    val snapped = snapToStep(raw.roundToInt(), min, max, step)
                    onValue(snapped)
                },
                valueRange = min.toFloat()..max.toFloat(),
                steps = sliderSteps
            )
        }
    }
}

@Composable
private fun LimitSlider(
    label: String,
    min: Int,
    max: Int,
    step: Int,
    value: Int,
    onValue: (Int) -> Unit
) {
    val stepsCount = ((max - min) / step) + 1
    val sliderSteps = (stepsCount - 2).coerceAtLeast(0)

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("Seleccionado: ${fmtMinutes(value)}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Slider(
                value = value.toFloat(),
                onValueChange = { raw ->
                    val snapped = snapToStep(raw.roundToInt(), min, max, step)
                    onValue(snapped)
                },
                valueRange = min.toFloat()..max.toFloat(),
                steps = sliderSteps
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(min, min + step * 2, min + step * 6, max).distinct().forEach { preset ->
                    AssistChip(onClick = { onValue(preset) }, label = { Text(fmtMinutes(preset)) })
                }
            }
        }
    }
}

private fun snapToStep(v: Int, min: Int, max: Int, step: Int): Int {
    val clamped = v.coerceIn(min, max)
    val offset = clamped - min
    val snapped = (offset / step) * step + min
    val next = (snapped + step).coerceAtMost(max)
    return if ((clamped - snapped) <= (next - clamped)) snapped else next
}

private fun fmtMinutes(min: Int): String {
    val h = min / 60
    val m = min % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

@Composable
private fun AppPickerSheet(
    apps: List<AppEntry>,
    initiallySelected: String,
    onPick: (AppEntry) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) apps
        else apps.filter { it.label.lowercase().contains(q) || it.packageName.contains(q) }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Elegir app", fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                items(filtered) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(app) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (app.icon != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(app.icon).build(),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Box(Modifier.size(36.dp))
                        }

                        Column(Modifier.weight(1f)) {
                            Text(app.label, fontWeight = FontWeight.SemiBold)
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (app.packageName == initiallySelected) {
                            Text("✓", fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider()
                }
            }

            TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Cerrar") }
        }
    }
}

@Composable
private fun MultiAppPickerSheet(
    apps: List<AppEntry>,
    selected: Set<String>,
    title: String,
    onClose: () -> Unit,
    onDone: (Set<String>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var sel by remember { mutableStateOf(selected) }

    val filtered = remember(apps, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) apps
        else apps.filter { it.label.lowercase().contains(q) || it.packageName.contains(q) }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                "${sel.size} seleccionadas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                items(filtered) { app ->
                    val checked = sel.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                sel = if (checked) sel - app.packageName else sel + app.packageName
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (app.icon != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(app.icon).build(),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        } else {
                            Box(Modifier.size(34.dp))
                        }

                        Column(Modifier.weight(1f)) {
                            Text(app.label, fontWeight = FontWeight.SemiBold)
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                sel = if (it) sel + app.packageName else sel - app.packageName
                            }
                        )
                    }
                    HorizontalDivider()
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Cancelar") }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { onDone(sel) }) { Text("Listo") }
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear grupo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (ej. Redes sociales)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name.trim().ifBlank { "Grupo" }) }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}