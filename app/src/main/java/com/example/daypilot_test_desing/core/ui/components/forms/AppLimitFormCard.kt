package com.example.daypilot_test_desing.ui.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.AppInfo
import com.example.daypilot_test_desing.ui.components.basic.AppMultiPickerSheet
import com.example.daypilot_test_desing.ui.components.basic.AppPickerSheet
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTextField
import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Tipo de restricción ───────────────────────────────────────────
private enum class RestrictionType { APP, GROUP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitFormCard(
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    initialApp: AppRestriction? = null,
    initialGroup: GroupRestriction? = null,
    onSaveApp: (AppRestriction) -> Unit,
    onSaveGroup: (GroupRestriction) -> Unit,
    onCancel: () -> Unit,
) {
    var restrictionType by remember {
        mutableStateOf(if (initialGroup != null) RestrictionType.GROUP else RestrictionType.APP)
    }
    val isGroup = restrictionType == RestrictionType.GROUP

    var selectedApp by remember {
        mutableStateOf(initialApp?.let { AppInfo(it.appName, it.packageName) })
    }

    var groupName by remember { mutableStateOf(initialGroup?.groupName ?: "") }
    var groupApps: List<AppInfo> by remember {
        mutableStateOf(
            initialGroup?.apps?.map { AppInfo(it.appName, it.packageName) } ?: emptyList()
        )
    }

    val maxMinutes = if (isGroup) 600f else 360f
    var limitMinutes by remember {
        mutableFloatStateOf(
            (initialApp?.dailyLimitMinutes ?: initialGroup?.dailyLimitMinutes ?: 60)
                .toFloat().coerceAtMost(maxMinutes)
        )
    }
    var notifSeconds by remember {
        mutableFloatStateOf(
            (initialApp?.notificationIntervalSeconds
                ?: initialGroup?.notificationIntervalSeconds ?: 60).toFloat()
        )
    }
    var notifEnabled by remember { mutableStateOf(true) }
    var showAppPicker by remember { mutableStateOf(false) }
    var showGroupAppPicker by remember { mutableStateOf(false) }

    val isValid = if (isGroup) groupName.isNotBlank() && groupApps.isNotEmpty()
    else selectedApp != null

    val limitLabel = when {
        limitMinutes >= 60 -> "${(limitMinutes / 60).toInt()}h ${(limitMinutes % 60).toInt()}m"
            .replace(" 0m", "")

        else -> "${limitMinutes.toInt()}min"
    }

    // ── App picker sheet ──────────────────────────────────────────
    if (showAppPicker) {
        ModalBottomSheet(
            onDismissRequest = { showAppPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AppPickerSheet(
                onSelect = { selectedApp = it },
                onDismiss = { showAppPicker = false }
            )
        }
    }

    // ── Group app picker sheet ────────────────────────────────────
    if (showGroupAppPicker) {
        ModalBottomSheet(
            onDismissRequest = { showGroupAppPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AppMultiPickerSheet(
                initialSelected = groupApps,
                onConfirm = { groupApps = it },
                onDismiss = { showGroupAppPicker = false }
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Título ────────────────────────────────────────────
            Text(
                text = stringResource(
                    if (isEditing) R.string.app_limit_form_title_edit
                    else R.string.app_limit_form_title_new
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ── Selector de tipo (solo al crear) ──────────────────
            if (!isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RestrictionType.APP to stringResource(R.string.app_limit_form_type_app),
                        RestrictionType.GROUP to stringResource(R.string.app_limit_form_type_group)
                    ).forEach { (type, label) ->
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
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── App ───────────────────────────────────────────────
            if (!isGroup) {
                Text(
                    text = stringResource(R.string.app_limit_form_app_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { showAppPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedApp!!.name.first().uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedApp!!.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = selectedApp!!.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_limit_form_app_pick),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Grupo ─────────────────────────────────────────────
            if (isGroup) {
                DayPilotTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = stringResource(R.string.app_limit_form_group_name_label),
                    placeholder = stringResource(R.string.app_limit_form_group_name_placeholder)
                )
                Text(
                    text = stringResource(R.string.app_limit_form_group_apps_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.name.first().uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { groupApps = groupApps - app },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showGroupAppPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(
                            if (groupApps.isEmpty()) R.string.app_limit_form_group_apps_pick
                            else R.string.app_limit_form_group_apps_change
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // ── Notificaciones ────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.app_limit_form_notif_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.app_limit_form_notif_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = { notifEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    if (notifEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.app_limit_form_notif_interval_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(
                                    R.string.app_limit_form_notif_selected,
                                    notifSeconds.toInt()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Slider(
                                value = notifSeconds,
                                onValueChange = { notifSeconds = it },
                                valueRange = 5f..60f,
                                steps = 10,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // ── Límite diario ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        if (isGroup) R.string.app_limit_form_limit_group
                        else R.string.app_limit_form_limit_app
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.app_limit_form_limit_selected, limitLabel),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = limitMinutes,
                    onValueChange = { limitMinutes = it },
                    valueRange = if (isGroup) 90f..600f else 30f..360f,
                    steps = if (isGroup) 16 else 10,
                    modifier = Modifier.fillMaxWidth()
                )
                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Botones ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.common_cancel)) }

                Button(
                    onClick = {
                        if (isValid) {
                            if (isGroup) {
                                onSaveGroup(
                                    GroupRestriction(
                                        id = initialGroup?.id
                                            ?: System.currentTimeMillis().toString(),
                                        groupName = groupName,
                                        apps = groupApps.map {
                                            AppRestriction(
                                                id = it.packageName,
                                                appName = it.name,
                                                packageName = it.packageName,
                                                dailyLimitMinutes = limitMinutes.toInt(),
                                                notificationIntervalSeconds = if (notifEnabled) notifSeconds.toInt() else 0,
                                                isEnabled = true
                                            )
                                        },
                                        dailyLimitMinutes = limitMinutes.toInt(),
                                        notificationIntervalSeconds = if (notifEnabled) notifSeconds.toInt() else 0,
                                        isEnabled = true
                                    )
                                )
                            } else {
                                onSaveApp(
                                    AppRestriction(
                                        id = initialApp?.id
                                            ?: System.currentTimeMillis().toString(),
                                        appName = selectedApp!!.name,
                                        packageName = selectedApp!!.packageName,
                                        dailyLimitMinutes = limitMinutes.toInt(),
                                        notificationIntervalSeconds = if (notifEnabled) notifSeconds.toInt() else 0,
                                        isEnabled = true
                                    )
                                )
                            }
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(
                            if (isEditing) R.string.app_limit_form_save
                            else R.string.app_limit_form_create
                        )
                    )
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
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
                onSaveApp = {},
                onSaveGroup = {},
                onCancel = {}
            )
        }
    }
}