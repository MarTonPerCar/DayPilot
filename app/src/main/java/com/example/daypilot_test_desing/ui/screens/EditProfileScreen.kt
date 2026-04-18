package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentUsername: String,
    currentRegion: String,
    avatarUrl: String? = null,
    onSave: (name: String, username: String, region: String) -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onPickFromCamera: () -> Unit,
    onPickFromGallery: () -> Unit,
    onBack: () -> Unit
) {
    var name            by remember { mutableStateOf(currentName) }
    var username        by remember { mutableStateOf(currentUsername) }
    var region          by remember { mutableStateOf(currentRegion) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val regions = listOf(
        "Europe/Madrid", "Atlantic/Canary", "America/New_York",
        "America/Los_Angeles", "America/Mexico_City", "America/Sao_Paulo",
        "Asia/Tokyo", "Asia/Shanghai", "Australia/Sydney"
    )

    if (showPhotoDialog) {
        DayPilotPhotoPickerDialog(
            onDismiss         = { showPhotoDialog = false },
            onPickFromCamera  = onPickFromCamera,
            onPickFromGallery = onPickFromGallery
        )
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.edit_profile_title),
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Avatar ───────────────────────────────────────────
            Box(contentAlignment = Alignment.BottomEnd) {
                DayPilotAvatar(
                    name      = name,
                    avatarUrl = avatarUrl,
                    size      = 90
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick  = { showPhotoDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.edit_profile_change_photo),
                            tint               = MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(14.dp)
                        )
                    }
                }
            }

            TextButton(onClick = { showPhotoDialog = true }) {
                Text(
                    text  = stringResource(R.string.edit_profile_change_photo),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ── Información personal ─────────────────────────────
            DayPilotSectionHeader(
                title = stringResource(R.string.edit_profile_personal_info)
            )

            DayPilotTextField(
                value         = name,
                onValueChange = { name = it },
                label         = stringResource(R.string.name)
            )

            DayPilotTextField(
                value         = username,
                onValueChange = { username = it },
                label         = stringResource(R.string.username)
            )

            DayPilotDropdownField(
                value       = region,
                options     = regions,
                onSelect    = { region = it },
                label       = stringResource(R.string.region),
                displayText = { it }
            )

            // ── Seguridad ────────────────────────────────────────
            DayPilotSectionHeader(
                title = stringResource(R.string.edit_profile_security)
            )

            DayPilotButtonOutlined(
                text    = stringResource(R.string.edit_profile_change_password),
                onClick = onNavigateToResetPassword
            )

            Spacer(Modifier.height(8.dp))

            DayPilotButtonPrimary(
                text    = stringResource(R.string.edit_profile_save),
                onClick = { onSave(name, username, region) }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}