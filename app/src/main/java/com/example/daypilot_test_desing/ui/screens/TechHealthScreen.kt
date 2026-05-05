package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.components.forms.AppLimitFormCard
import com.example.daypilot_test_desing.ui.model.AppRestriction
import com.example.daypilot_test_desing.ui.model.GroupRestriction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechHealthScreen(
    appRestrictions: List<AppRestriction>,
    groupRestrictions: List<GroupRestriction>,
    onSaveApp: (AppRestriction, isEdit: Boolean) -> Unit,
    onSaveGroup: (GroupRestriction, isEdit: Boolean) -> Unit,
    onToggleRestriction: (String, Boolean) -> Unit,
    onDeleteRestriction: (String) -> Unit,
    onToggleGroup: (String, Boolean) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddSheet   by remember { mutableStateOf(false) }
    var editingAppId   by remember { mutableStateOf<String?>(null) }
    var editingGroupId by remember { mutableStateOf<String?>(null) }
    val sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total          = appRestrictions.size + groupRestrictions.size

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
            AppLimitFormCard(
                isEditing    = editingAppId != null || editingGroupId != null,
                initialApp   = appRestrictions.find { it.id == editingAppId },
                initialGroup = groupRestrictions.find { it.id == editingGroupId },
                onSaveApp    = { restriction ->
                    onSaveApp(restriction, editingAppId != null)
                    showAddSheet   = false
                    editingAppId   = null
                },
                onSaveGroup  = { group ->
                    onSaveGroup(group, editingGroupId != null)
                    showAddSheet   = false
                    editingGroupId = null
                },
                onCancel = {
                    showAddSheet   = false
                    editingAppId   = null
                    editingGroupId = null
                },
                modifier = Modifier.padding(16.dp)
            )
            Spacer(Modifier.height(16.dp))
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
                    contentDescription = "Añadir restricción"
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
                DayPilotSectionHeader(title = "Restricciones")
                Text(
                    text  = "$total en total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }

            if (total == 0) {
                item {
                    DayPilotEmptyState(
                        message  = "No hay restricciones\nPulsa + para crear una.",
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
                    DayPilotSectionHeader(title = "Grupos")
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