package com.example.daypilot_test_desing.feature.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.reminders.ReliabilitySettings
import com.example.daypilot_test_desing.core.ui.components.basic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    name: String,
    isDarkMode: Boolean,
    selectedThemeId: String,
    selectedLanguage: String,
    notificationsEnabled: Boolean,
    taskRemindersEnabled: Boolean,
    streakAlertsEnabled: Boolean,
    exactAlarmsGranted: Boolean,
    batteryOptimizationExempt: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onThemeSelect: (String) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleTaskReminders: (Boolean) -> Unit,
    onToggleStreakAlerts: (Boolean) -> Unit,
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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                DayPilotSwitchRow(
                    title           = stringResource(R.string.settings_task_reminders),
                    description     = stringResource(R.string.settings_task_reminders_description),
                    icon            = Icons.Default.CalendarMonth,
                    checked         = notificationsEnabled && taskRemindersEnabled,
                    onCheckedChange = onToggleTaskReminders,
                    enabled         = notificationsEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                DayPilotSwitchRow(
                    title           = stringResource(R.string.settings_streak_alerts),
                    description     = stringResource(R.string.settings_streak_alerts_description),
                    icon            = Icons.Default.Whatshot,
                    checked         = notificationsEnabled && streakAlertsEnabled,
                    onCheckedChange = onToggleStreakAlerts,
                    enabled         = notificationsEnabled
                )
            }

            val context = LocalContext.current
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text     = stringResource(R.string.settings_reliability_section),
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                ReliabilityRow(
                    icon         = Icons.Default.Alarm,
                    title        = stringResource(R.string.settings_exact_alarms),
                    description  = stringResource(R.string.settings_exact_alarms_description),
                    granted      = exactAlarmsGranted,
                    grantedLabel = stringResource(R.string.settings_exact_alarms_granted),
                    actionLabel  = stringResource(R.string.settings_exact_alarms_action),
                    onAction     = { context.startActivity(ReliabilitySettings.exactAlarmSettingsIntent(context)) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ReliabilityRow(
                    icon         = Icons.Default.BatteryChargingFull,
                    title        = stringResource(R.string.settings_battery_optimization),
                    description  = stringResource(R.string.settings_battery_optimization_description),
                    granted      = batteryOptimizationExempt,
                    grantedLabel = stringResource(R.string.settings_battery_optimization_granted),
                    actionLabel  = stringResource(R.string.settings_battery_optimization_action),
                    onAction     = { context.startActivity(ReliabilitySettings.batteryOptimizationIntent(context)) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = stringResource(R.string.settings_autostart_info_title),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text     = stringResource(R.string.settings_autostart_info_description),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                    )
                    DayPilotButtonText(
                        text    = stringResource(R.string.settings_autostart_open_settings),
                        onClick = { context.startActivity(ReliabilitySettings.appDetailsSettingsIntent(context)) }
                    )
                }
            }

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

            Spacer(Modifier.height(8.dp))

            DayPilotButtonError(
                text    = stringResource(R.string.settings_logout),
                onClick = onLogout
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReliabilityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    grantedLabel: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            modifier              = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (granted) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text  = grantedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            DayPilotButtonText(text = actionLabel, onClick = onAction)
        }
    }
}