package com.example.daypilot_test_desing.feature.techhealth

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.*
import com.example.daypilot_test_desing.core.ui.components.cards.*
import com.example.daypilot_test_desing.core.ui.components.forms.AppLimitFormCard
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction

data class TechHealthActions(
    val onSaveApp: (AppRestriction, isEdit: Boolean) -> Unit,
    val onSaveGroup: (GroupRestriction, isEdit: Boolean) -> Unit,
    val onToggleRestriction: (String, Boolean) -> Unit,
    val onDeleteRestriction: (String) -> Unit,
    val onToggleGroup: (String, Boolean) -> Unit,
    val onDeleteGroup: (String) -> Unit,
    val onBack: () -> Unit
)

@Composable
private fun TechHealthInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tech_health_point_info_title)) },
        text  = { Text(stringResource(R.string.tech_health_point_info_body)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
private fun TechHealthPointStatusCard(
    earned: Boolean,
    warning: Boolean,
    onInfoClick: () -> Unit
) {
    val containerColor = when {
        earned  -> MaterialTheme.colorScheme.primaryContainer
        warning -> MaterialTheme.colorScheme.errorContainer
        else    -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        earned  -> MaterialTheme.colorScheme.onPrimaryContainer
        warning -> MaterialTheme.colorScheme.onErrorContainer
        else    -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val icon = when {
        earned  -> Icons.Default.CheckCircle
        warning -> Icons.Default.Warning
        else    -> Icons.Default.Info
    }
    val label = when {
        earned  -> R.string.tech_health_point_earned
        warning -> R.string.tech_health_point_warning
        else    -> R.string.tech_health_point_pending
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!earned && !warning)
                    Modifier.clickable { onInfoClick() }
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = contentColor,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text  = stringResource(label),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun TechHealthFormSheetContent(
    editingAppId: String?,
    editingGroupId: String?,
    appRestrictions: List<AppRestriction>,
    groupRestrictions: List<GroupRestriction>,
    onSaveApp: (AppRestriction) -> Unit,
    onSaveGroup: (GroupRestriction) -> Unit,
    onCancel: () -> Unit
) {
    AppLimitFormCard(
        isEditing    = editingAppId != null || editingGroupId != null,
        initialApp   = appRestrictions.find { it.id == editingAppId },
        initialGroup = groupRestrictions.find { it.id == editingGroupId },
        onSaveApp    = onSaveApp,
        onSaveGroup  = onSaveGroup,
        onCancel     = onCancel,
        modifier = Modifier.padding(16.dp)
    )
    Spacer(Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechHealthScreen(
    state: TechHealthUiState,
    actions: TechHealthActions
) {
    val appRestrictions             = state.appRestrictions
    val groupRestrictions           = state.groupRestrictions
    val hasUsagePermission          = state.hasUsagePermission
    val hasAccessibilityPermission  = state.hasAccessibilityPermission
    val techHealthPointEarned       = state.techHealthPointEarned
    val activeRestrictionCount      = state.activeRestrictionCount
    val onSaveApp             = actions.onSaveApp
    val onSaveGroup            = actions.onSaveGroup
    val onToggleRestriction    = actions.onToggleRestriction
    val onDeleteRestriction    = actions.onDeleteRestriction
    val onToggleGroup          = actions.onToggleGroup
    val onDeleteGroup          = actions.onDeleteGroup
    val onBack                  = actions.onBack
    if (!hasUsagePermission || !hasAccessibilityPermission) {
        TechHealthPermissionGate(
            hasUsagePermission         = hasUsagePermission,
            hasAccessibilityPermission = hasAccessibilityPermission,
            onBack                     = onBack
        )
        return
    }

    val context = LocalContext.current
    var showAddSheet   by remember { mutableStateOf(false) }
    var editingAppId   by remember { mutableStateOf<String?>(null) }
    var editingGroupId by remember { mutableStateOf<String?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total          = appRestrictions.size + groupRestrictions.size

    if (showInfoDialog) {
        TechHealthInfoDialog(onDismiss = { showInfoDialog = false })
    }

    if (showAddSheet || editingAppId != null || editingGroupId != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet   = false
                editingAppId   = null
                editingGroupId = null
            },
            sheetState     = sheetState,
            shape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            TechHealthFormSheetContent(
                editingAppId = editingAppId,
                editingGroupId = editingGroupId,
                appRestrictions = appRestrictions,
                groupRestrictions = groupRestrictions,
                onSaveApp = { restriction ->
                    onSaveApp(restriction, editingAppId != null)
                    showAddSheet   = false
                    editingAppId   = null
                },
                onSaveGroup = { group ->
                    onSaveGroup(group, editingGroupId != null)
                    showAddSheet   = false
                    editingGroupId = null
                },
                onCancel = {
                    showAddSheet   = false
                    editingAppId   = null
                    editingGroupId = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.tech_health_title),
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                shape          = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = stringResource(R.string.tech_health_add_restriction)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TechHealthPointStatusCard(
                    earned = techHealthPointEarned && activeRestrictionCount >= 3,
                    warning = activeRestrictionCount < 3,
                    onInfoClick = { showInfoDialog = true }
                )
            }

            item {
                DayPilotSectionHeader(title = stringResource(R.string.tech_health_restrictions_title))
                Text(
                    text = stringResource(R.string.tech_health_restrictions_count, total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }

            if (total == 0) {
                item {
                    DayPilotEmptyState(
                        message  = stringResource(R.string.tech_health_empty),
                        modifier = Modifier.height(200.dp)
                    )
                }
            }

            items(appRestrictions, key = { it.id }) { restriction ->
                AppLimitCard(
                    restriction = restriction,
                    onToggle    = { onToggleRestriction(restriction.id, it) },
                    onEdit      = { editingAppId = restriction.id },
                    onDelete    = { onDeleteRestriction(restriction.id) }
                )
            }

            if (groupRestrictions.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    DayPilotSectionHeader(title = stringResource(R.string.tech_health_groups_title))
                }
                items(groupRestrictions, key = { it.id }) { group ->
                    GroupLimitCard(
                        restriction = group,
                        onToggle    = { onToggleGroup(group.id, it) },
                        onEdit      = { editingGroupId = group.id },
                        onDelete    = { onDeleteGroup(group.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


@Composable
private fun TechHealthPermissionGate(
    hasUsagePermission: Boolean,
    hasAccessibilityPermission: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.tech_health_perm_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Icon(
                    imageVector        = Icons.Default.Lock,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .wrapContentWidth()
                        .size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = stringResource(R.string.tech_health_perm_subtitle),
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.tech_health_perm_note),
                        modifier   = Modifier.padding(14.dp),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            item {
                PermissionCard(
                    number      = "1",
                    title       = stringResource(R.string.tech_health_perm_usage_title),
                    description = stringResource(R.string.tech_health_perm_usage_desc),
                    steps       = stringResource(R.string.tech_health_perm_usage_steps),
                    buttonText  = stringResource(R.string.tech_health_perm_usage_button),
                    isGranted   = hasUsagePermission,
                    onOpen      = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
            }

            item {
                PermissionCard(
                    number      = "2",
                    title       = stringResource(R.string.tech_health_perm_a11y_title),
                    description = stringResource(R.string.tech_health_perm_a11y_desc),
                    steps       = stringResource(R.string.tech_health_perm_a11y_steps),
                    buttonText  = stringResource(R.string.tech_health_perm_a11y_button),
                    isGranted   = hasAccessibilityPermission,
                    onOpen      = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PermissionStatusBadgeIcon(number: String, isGranted: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isGranted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isGranted) {
            Icon(
                imageVector        = Icons.Default.Check,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onPrimary,
                modifier           = Modifier.size(18.dp)
            )
        } else {
            Text(
                text       = number,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PermissionGrantedBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text     = stringResource(R.string.tech_health_perm_granted),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PermissionActionSection(steps: String, buttonText: String, onOpen: () -> Unit) {
    Text(
        text       = steps,
        style      = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurface
    )
    Button(
        onClick  = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Text(buttonText)
    }
}

@Composable
private fun PermissionCard(
    number: String,
    title: String,
    description: String,
    steps: String,
    buttonText: String,
    isGranted: Boolean,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PermissionStatusBadgeIcon(number = number, isGranted = isGranted)
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                if (isGranted) PermissionGrantedBadge()
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text  = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) PermissionActionSection(steps = steps, buttonText = buttonText, onOpen = onOpen)
        }
    }
}
