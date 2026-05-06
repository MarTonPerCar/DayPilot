package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotEmptyState
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.ui.components.cards.ReminderCard
import com.example.daypilot_test_desing.ui.components.forms.ReminderFormCard
import com.example.daypilot_test_desing.ui.model.ReminderData
import com.example.daypilot_test_desing.ui.model.ReminderFormDataInfo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    reminders: List<ReminderData>,
    onAddReminder: (ReminderFormDataInfo) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onToggleReminder: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            ReminderFormCard(
                onSave = { data ->
                    onAddReminder(data)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false },
                modifier = Modifier.padding(16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title = stringResource(R.string.reminders_title),
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.reminders_add)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (reminders.isEmpty()) {
            DayPilotEmptyState(
                message = stringResource(R.string.reminders_empty),
                icon = Icons.Default.Add,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        title = reminder.title,
                        time = reminder.time,
                        isEnabled = reminder.isEnabled,
                        onToggle = { onToggleReminder(reminder.id, it) },
                        onDelete = { onDeleteReminder(reminder.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}