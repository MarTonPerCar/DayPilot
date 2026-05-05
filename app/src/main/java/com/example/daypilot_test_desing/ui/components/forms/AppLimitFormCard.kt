package com.example.daypilot_test_desing.ui.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTextField
import com.example.daypilot_test_desing.ui.model.AppRestriction
import com.example.daypilot_test_desing.ui.model.GroupRestriction
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import androidx.compose.runtime.snapshots.SnapshotStateList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitFormCard(
    isEditing: Boolean = false,
    initialApp: AppRestriction? = null,
    initialGroup: GroupRestriction? = null,
    onSaveApp: (AppRestriction) -> Unit,
    onSaveGroup: (GroupRestriction) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tipo — si viene un grupo inicial forzamos "Grupo"
    var restrictionType by remember {
        mutableStateOf(if (initialGroup != null) "Grupo" else "App")
    }

    // App
    var selectedApp  by remember {
        mutableStateOf(
            initialApp?.let { AppInfo(it.appName, it.packageName) }
        )
    }

    // Grupo
    var groupName    by remember { mutableStateOf(initialGroup?.groupName ?: "") }
    var groupApps: List<AppInfo> by remember {
        mutableStateOf(
            initialGroup?.apps?.map { AppInfo(it.appName, it.packageName) } ?: emptyList()
        )
    }

    // Común
    val isGroup      = restrictionType == "Grupo"
    val maxMinutes   = if (isGroup) 600f else 360f
    var limitMinutes by remember {
        mutableFloatStateOf(
            (initialApp?.dailyLimitMinutes ?: initialGroup?.dailyLimitMinutes ?: 60).toFloat()
                .coerceAtMost(maxMinutes)
        )
    }
    var notifSeconds by remember {
        mutableFloatStateOf(
            (initialApp?.notificationIntervalSeconds
                ?: initialGroup?.notificationIntervalSeconds ?: 60).toFloat()
        )
    }
    var notifEnabled by remember { mutableStateOf(true) }

    // Sheets
    var showAppPicker      by remember { mutableStateOf(false) }
    var showGroupAppPicker by remember { mutableStateOf(false) }
    val sheetState         = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val isValid = if (isGroup) groupName.isNotBlank() && groupApps.isNotEmpty()
    else selectedApp != null

    val limitLabel = when {
        limitMinutes >= 60 -> "${(limitMinutes / 60).toInt()}h ${(limitMinutes % 60).toInt()}m"
            .replace(" 0m", "")
        else               -> "${limitMinutes.toInt()}min"
    }

    // ── App picker sheet ─────────────────────────────────────────
    if (showAppPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAppPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor   = MaterialTheme.colorScheme.background,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AppPickerSheet(
                onSelect  = { selectedApp = it },
                onDismiss = { showAppPicker = false }
            )
        }
    }

    // ── Group app picker sheet ────────────────────────────────────
    if (showGroupAppPicker) {
        ModalBottomSheet(
            onDismissRequest = { showGroupAppPicker = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor   = MaterialTheme.colorScheme.background,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AppMultiPickerSheet(
                initialSelected = groupApps,
                onConfirm       = { groupApps = it },
                onDismiss       = { showGroupAppPicker = false }
            )
        }
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Título ───────────────────────────────────────────
            Text(
                text       = if (isEditing) "Editar restricción" else "Nueva restricción",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            // ── Tipo (solo al crear) ──────────────────────────────
            if (!isEditing) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("App", "Grupo").forEach { type ->
                        val isSelected = restrictionType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { restrictionType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = type,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── App ───────────────────────────────────────────────
            if (!isGroup) {
                Text(
                    text  = "Aplicación",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick  = { showAppPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedApp != null)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                ) {
                    if (selectedApp != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text      = selectedApp!!.name.first().uppercase(),
                                fontSize  = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color     = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = selectedApp!!.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text  = selectedApp!!.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text  = "Elegir app",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Grupo ─────────────────────────────────────────────
            if (isGroup) {
                DayPilotTextField(
                    value         = groupName,
                    onValueChange = { groupName = it },
                    label         = "Nombre del grupo",
                    placeholder   = "Por ejemplo: Redes Sociales"
                )

                Text(
                    text  = "Apps del grupo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Apps seleccionadas
                if (groupApps.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupApps.forEach { app ->
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = app.name.first().uppercase(),
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text     = app.name,
                                    style    = MaterialTheme.typography.bodySmall,
                                    color    = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick  = { groupApps = groupApps - app },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector        = Icons.Default.Close,
                                        contentDescription = null,
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick  = { showGroupAppPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Add,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = if (groupApps.isEmpty()) "Elegir apps" else "Cambiar apps",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // ── Notificaciones ────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text       = "Notificaciones",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text  = "Avisar cuando supere el límite",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked         = notifEnabled,
                            onCheckedChange = { notifEnabled = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (notifEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text  = "Repetir notificación (5s → 60s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text       = "Seleccionado: ${notifSeconds.toInt()}s",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value         = notifSeconds,
                                onValueChange = { notifSeconds = it },
                                valueRange    = 5f..60f,
                                steps         = 10,
                                modifier      = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Límite diario ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = if (isGroup) "Límite diario (1.5h → 10h)"
                    else "Límite diario (30 min → 6 h)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = "Seleccionado: $limitLabel",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value         = limitMinutes,
                    onValueChange = { limitMinutes = it },
                    valueRange    = if (isGroup) 90f..600f else 30f..360f,
                    steps         = if (isGroup) 16 else 10,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Presets
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = if (isGroup)
                        listOf(90 to "1.5h", 120 to "2h", 240 to "4h", 600 to "10h")
                    else
                        listOf(30 to "30m", 60 to "1h", 120 to "2h", 360 to "6h")

                    presets.forEach { (min, label) ->
                        val isSelected = limitMinutes.toInt() == min
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { limitMinutes = min.toFloat() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = label,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Botones ───────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onCancel,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick  = {
                        if (isValid) {
                            if (isGroup) {
                                onSaveGroup(
                                    GroupRestriction(
                                        id                          = initialGroup?.id
                                            ?: System.currentTimeMillis().toString(),
                                        groupName                   = groupName,
                                        apps                        = groupApps.map {
                                            AppRestriction(
                                                id                          = it.packageName,
                                                appName                     = it.name,
                                                packageName                 = it.packageName,
                                                dailyLimitMinutes           = limitMinutes.toInt(),
                                                notificationIntervalSeconds = if (notifEnabled)
                                                    notifSeconds.toInt() else 0,
                                                isEnabled                   = true
                                            )
                                        },
                                        dailyLimitMinutes           = limitMinutes.toInt(),
                                        notificationIntervalSeconds = if (notifEnabled)
                                            notifSeconds.toInt() else 0,
                                        isEnabled                   = true
                                    )
                                )
                            } else {
                                onSaveApp(
                                    AppRestriction(
                                        id                          = initialApp?.id
                                            ?: System.currentTimeMillis().toString(),
                                        appName                     = selectedApp!!.name,
                                        packageName                 = selectedApp!!.packageName,
                                        dailyLimitMinutes           = limitMinutes.toInt(),
                                        notificationIntervalSeconds = if (notifEnabled)
                                            notifSeconds.toInt() else 0,
                                        isEnabled                   = true
                                    )
                                )
                            }
                        }
                    },
                    enabled  = isValid,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text(if (isEditing) "Guardar" else "Crear") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppLimitFormPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AppLimitFormCard(
                onSaveApp   = {},
                onSaveGroup = {},
                onCancel    = {}
            )
        }
    }
}