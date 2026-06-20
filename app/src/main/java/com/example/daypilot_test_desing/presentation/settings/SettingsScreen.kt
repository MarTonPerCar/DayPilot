package com.example.daypilot_test_desing.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    name: String,
    isDarkMode: Boolean,
    selectedThemeId: String,
    selectedLanguage: String,
    notificationsEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onThemeSelect: (String) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.settings_title),
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

            // ── Apariencia ───────────────────────────────────────
            DayPilotThemeSelector(
                selectedThemeId = selectedThemeId,
                onThemeSelect   = onThemeSelect
            )

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DayPilotSwitchRow(
                    title           = stringResource(R.string.settings_dark_mode),
                    description     = stringResource(R.string.settings_dark_mode_description),
                    icon            = Icons.Default.DarkMode,
                    checked         = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }

            // ── Notificaciones ───────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DayPilotSwitchRow(
                    title           = stringResource(R.string.settings_notifications),
                    description     = stringResource(R.string.settings_notifications_description),
                    icon            = Icons.Default.Notifications,
                    checked         = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }

            // ── Perfil ───────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick   = onNavigateToEditProfile
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Person,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text       = stringResource(R.string.settings_edit_profile),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text  = stringResource(R.string.settings_edit_profile_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ── Idioma ───────────────────────────────────────────
            val languageDisplay = when (selectedLanguage) {
                "en" -> "English"
                "de" -> "Deutsch"
                else -> "Español"
            }
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DayPilotOptionSelector(
                    title          = stringResource(R.string.settings_language),
                    icon           = Icons.Default.Language,
                    selectedOption = languageDisplay,
                    options        = listOf("Español", "English", "Deutsch"),
                    onSelect       = { display ->
                        val code = when (display) {
                            "English" -> "en"
                            "Deutsch" -> "de"
                            else      -> "es"
                        }
                        onLanguageSelect(code)
                    }
                )
            }

            // ── Cerrar sesión ────────────────────────────────────
            Spacer(Modifier.height(8.dp))

            DayPilotButtonError(
                text    = stringResource(R.string.settings_logout),
                onClick = onLogout
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}