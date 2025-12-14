package com.example.daypilot.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.authLogic.UserProfile
import kotlinx.coroutines.launch

data class RegionOption(
    val id: String,
    val label: String
)

private val regionOptions = listOf(
    RegionOption("Europe/Madrid", "Europe/Madrid (UTC+01:00)"),
    RegionOption("Atlantic/Canary", "Atlantic/Canary (UTC+00:00)"),
    RegionOption("Europe/London", "Europe/London (UTC+00:00)"),
    RegionOption("Europe/Paris", "Europe/Paris (UTC+01:00)"),
    RegionOption("America/New_York", "America/New_York (UTC-05:00)"),
    RegionOption("America/Los_Angeles", "America/Los_Angeles (UTC-08:00)"),
    RegionOption("America/Mexico_City", "America/Mexico_City (UTC-06:00)"),
    RegionOption("America/Sao_Paulo", "America/Sao_Paulo (UTC-03:00)"),
    RegionOption("Asia/Tokyo", "Asia/Tokyo (UTC+09:00)"),
    RegionOption("Asia/Shanghai", "Asia/Shanghai (UTC+08:00)"),
    RegionOption("Asia/Dubai", "Asia/Dubai (UTC+04:00)"),
    RegionOption("Australia/Sydney", "Australia/Sydney (UTC+10:00)")
)

private val languageOptions = listOf(
    "es" to "Español",
    "en" to "English"
)

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
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    var isDarkMode by remember { mutableStateOf(isDarkModeInitial) }
    var notificationsEnabled by remember { mutableStateOf(notificationsInitial) }
    var selectedLanguage by remember { mutableStateOf(languageInitial) }

    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    var regionDisplay by remember { mutableStateOf("") }
    var regionExpanded by remember { mutableStateOf(false) }

    var languageExpanded by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Cargar perfil y región
    LaunchedEffect(uid) {
        try {
            val p = authRepo.getUserProfile(uid)
            profile = p
            selectedRegionId = p?.region
            regionDisplay = regionOptions.find { it.id == p?.region }?.label ?: (p?.region ?: "")
        } catch (e: Exception) {
            errorMessage = "Error cargando configuración."
        } finally {
            isLoading = false
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    isUploadingPhoto = true

                    val url = authRepo.uploadProfilePhotoFromUri(uid, uri)
                    profile = (profile ?: UserProfile()).copy(photoUrl = url)

                } catch (e: Exception) {
                    errorMessage = e.localizedMessage ?: "Error subiendo la foto."
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scope.launch {
                try {
                    isUploadingPhoto = true

                    val url = authRepo.uploadProfilePhotoFromBitmap(uid, bitmap)
                    profile = (profile ?: UserProfile()).copy(photoUrl = url)

                } catch (e: Exception) {
                    errorMessage = e.localizedMessage ?: "Error subiendo la foto."
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            confirmButton = {},
            title = { Text("Cambiar foto de perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        showPhotoDialog = false
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Elegir de la galería")
                    }
                    TextButton(onClick = {
                        showPhotoDialog = false
                        cameraLauncher.launch(null)
                    }) {
                        Text("Tomar foto con la cámara")
                    }
                }
            }
        )
    }

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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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

                DarkModeCard(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        onDarkModeChange(it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotificationsCard(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        onNotificationsChange(it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LanguageCard(
                    selectedLanguage = selectedLanguage,
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = it },
                    onLanguageSelected = { code ->
                        languageExpanded = false
                        selectedLanguage = code
                        onLanguageChange(code)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                RegionCard(
                    regionDisplay = regionDisplay,
                    expanded = regionExpanded,
                    onExpandedChange = { regionExpanded = it },
                    onRegionSelected = { option ->
                        regionExpanded = false
                        selectedRegionId = option.id
                        regionDisplay = option.label
                        scope.launch {
                            try {
                                authRepo.updateUserRegion(uid, option.id)
                            } catch (e: Exception) {
                                errorMessage = "Error actualizando la región."
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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

@Composable
private fun ProfilePhotoSection(
    profile: UserProfile?,
    isUploading: Boolean,
    onChangePhotoClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable { onChangePhotoClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profile?.photoUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Foto de perfil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Text(
                text = "Cambiar foto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DarkModeCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Modo oscuro",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Activa el tema oscuro de la app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun NotificationsCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Activar o desactivar notificaciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageCard(
    selectedLanguage: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Idioma",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Idioma de la aplicación (pendiente implementar)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange(!expanded) }
            ) {
                val label = languageOptions.firstOrNull { it.first == selectedLanguage }?.second
                    ?: "Desconocido"

                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Idioma") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    languageOptions.forEach { (code, text) ->
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = { onLanguageSelected(code) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionCard(
    regionDisplay: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRegionSelected: (RegionOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Región",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Usada para resetear los pasos diarios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange(!expanded) }
            ) {
                OutlinedTextField(
                    value = regionDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Zona horaria / región") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    regionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { onRegionSelected(option) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit
) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Cerrar sesión")
    }
}