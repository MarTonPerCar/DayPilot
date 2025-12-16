package com.example.daypilot.main.profile.settings

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.authLogic.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authRepo: AuthRepository,
    uid: String,
    isDarkModeInitial: Boolean,
    notificationsInitial: Boolean,
    languageInitial: String,
    onDarkModeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {

    // ========== State ==========

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    var isDarkMode by remember { mutableStateOf(isDarkModeInitial) }
    var notificationsEnabled by remember { mutableStateOf(notificationsInitial) }
    var selectedLanguage by remember { mutableStateOf(languageInitial) }

    var regionDisplay by remember { mutableStateOf("") }
    var regionExpanded by remember { mutableStateOf(false) }

    var languageExpanded by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // ========== Load Profile ==========

    LaunchedEffect(uid) {
        isLoading = true
        errorMessage = null
        try {
            val p = authRepo.getUserProfile(uid)
            profile = p
            regionDisplay = regionLabel(p?.region)
        } catch (_: Exception) {
            errorMessage = "Error cargando configuración."
        } finally {
            isLoading = false
        }
    }

    // ========== Launchers ==========

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isUploadingPhoto = true
            errorMessage = null
            try {
                val url = authRepo.uploadProfilePhotoFromUri(uid, uri)
                profile = (profile ?: UserProfile()).copy(photoUrl = url)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error subiendo la foto."
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult
        scope.launch {
            isUploadingPhoto = true
            errorMessage = null
            try {
                val url = authRepo.uploadProfilePhotoFromBitmap(uid, bitmap)
                profile = (profile ?: UserProfile()).copy(photoUrl = url)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error subiendo la foto."
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    // ========== Dialogs ==========

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            confirmButton = {},
            title = { Text("Cambiar foto de perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) { Text("Elegir de la galería") }

                    TextButton(
                        onClick = {
                            showPhotoDialog = false
                            cameraLauncher.launch(null)
                        }
                    ) { Text("Tomar foto con la cámara") }
                }
            }
        )
    }

    // ========== UI ==========

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    ProfilePhotoSection(
                        profile = profile,
                        isUploading = isUploadingPhoto,
                        onChangePhotoClick = { showPhotoDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ToggleCard(
                        icon = { DarkModeIcon() },
                        title = "Modo oscuro",
                        subtitle = "Activa el tema oscuro de la app",
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            onDarkModeChange(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ToggleCard(
                        icon = { NotificationsIcon() },
                        title = "Notificaciones",
                        subtitle = "Activar o desactivar notificaciones",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            onNotificationsChange(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DropdownCard(
                        icon = { LanguageIcon() },
                        title = "Idioma",
                        subtitle = "Idioma de la aplicación",
                        fieldLabel = "Idioma",
                        value = languageLabel(selectedLanguage),
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = it }
                    ) {
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            LanguageOptions.forEach { (code, text) ->
                                DropdownMenuItem(
                                    text = { Text(text) },
                                    onClick = {
                                        languageExpanded = false
                                        selectedLanguage = code
                                        onLanguageChange(code)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    DropdownCard(
                        icon = { RegionIcon() },
                        title = "Región",
                        subtitle = "Usada para resetear los pasos diarios",
                        fieldLabel = "Zona horaria / región",
                        value = regionDisplay,
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = it }
                    ) {
                        DropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            RegionOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        regionExpanded = false
                                        regionDisplay = option.label
                                        scope.launch {
                                            errorMessage = null
                                            runCatching { authRepo.updateUserRegion(uid, option.id) }
                                                .onFailure { errorMessage = "Error actualizando la región." }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    LogoutButton(onLogout = onLogout)
                }
            }
        }
    }
}