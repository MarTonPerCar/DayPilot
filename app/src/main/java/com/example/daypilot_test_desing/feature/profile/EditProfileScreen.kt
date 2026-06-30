package com.example.daypilot_test_desing.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentUsername: String,
    currentRegion: TimeZoneRegion = TimeZoneRegion.EUROPE_MADRID,
    avatarUrl: String? = null,
    isUploadingAvatar: Boolean = false,
    avatarUploadError: Boolean = false,
    onSave: (name: String, username: String, region: TimeZoneRegion) -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onAvatarErrorDismissed: () -> Unit = {},
    onBack: () -> Unit
) {
    val context         = LocalContext.current
    val snackbarHost    = remember { SnackbarHostState() }
    var name            by remember { mutableStateOf(currentName) }
    var username        by remember { mutableStateOf(currentUsername) }
    var region          by remember { mutableStateOf(currentRegion) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val photoErrorMsg = stringResource(R.string.edit_profile_photo_error)
    LaunchedEffect(avatarUploadError) {
        if (avatarUploadError) {
            snackbarHost.showSnackbar(photoErrorMsg)
            onAvatarErrorDismissed()
        }
    }

    // Crop result → upload
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val croppedUri = UCrop.getOutput(result.data ?: return@rememberLauncherForActivityResult)
        croppedUri?.let { onPhotoSelected(it) }
    }

    fun launchCrop(sourceUri: Uri) {
        val destFile = File(context.cacheDir, "avatar_cropped_${System.currentTimeMillis()}.jpg")
        val destUri  = Uri.fromFile(destFile)
        UCrop.of(sourceUri, destUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)
            .getIntent(context)
            .also { cropLauncher.launch(it) }
    }

    // Camera temp file
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { launchCrop(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { launchCrop(it) }
    }

    val regions = TimeZoneRegion.entries

    if (showPhotoDialog) {
        DayPilotPhotoPickerDialog(
            onDismiss         = { showPhotoDialog = false },
            onPickFromCamera  = {
                showPhotoDialog = false
                val tempFile = File(context.cacheDir, "avatar_temp.jpg")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                cameraUri = uri
                cameraLauncher.launch(uri)
            },
            onPickFromGallery = {
                showPhotoDialog = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.edit_profile_title),
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
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
                Box(contentAlignment = Alignment.Center) {
                    DayPilotAvatar(
                        name      = name,
                        avatarUrl = avatarUrl,
                        size      = 90
                    )
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(90.dp),
                            strokeWidth = 3.dp,
                            color       = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick  = { if (!isUploadingAvatar) showPhotoDialog = true },
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

            TextButton(
                onClick  = { showPhotoDialog = true },
                enabled  = !isUploadingAvatar
            ) {
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
                displayText = { it.value }
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
