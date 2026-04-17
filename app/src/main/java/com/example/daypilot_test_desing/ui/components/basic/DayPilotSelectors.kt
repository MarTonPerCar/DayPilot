package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Datos de cada tema ───────────────────────────────────────────
data class ThemeOption(
    val id: String,
    val name: String,
    val colors: List<Color>
)

val dayPilotThemes = listOf(
    ThemeOption(
        id = "sage_green",
        name = "Verde Salvia",
        colors = listOf(
            Color(0xFF4A7C59),
            Color(0xFF81A88D),
            Color(0xFFA8C5A0),
            Color(0xFFE8F2EB),
            Color(0xFFF4F9F5)
        )
    ),
    ThemeOption(
        id = "ocean",
        name = "Azul Océano",
        colors = listOf(
            Color(0xFF1A6B8A),
            Color(0xFF4A9BB5),
            Color(0xFF80C4D8),
            Color(0xFFE0F2F8),
            Color(0xFFF2F8FB)
        )
    ),
    ThemeOption(
        id = "lavender",
        name = "Morado Lavanda",
        colors = listOf(
            Color(0xFF6B4FA8),
            Color(0xFF9B7FCC),
            Color(0xFFC4AAEE),
            Color(0xFFEDE8F8),
            Color(0xFFF7F4FC)
        )
    ),
    ThemeOption(
        id = "amber",
        name = "Naranja Ámbar",
        colors = listOf(
            Color(0xFFB85C00),
            Color(0xFFD4843A),
            Color(0xFFEFB870),
            Color(0xFFFFF0DE),
            Color(0xFFFFF8F2)
        )
    ),
    ThemeOption(
        id = "amoled",
        name = "Oscuro Puro",
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF0A0A0A),
            Color(0xFF141414),
            Color(0xFF424242),
            Color(0xFFE0E0E0)
        )
    )
)

// ── Selector de tema ─────────────────────────────────────────────
@Composable
fun DayPilotThemeSelector(
    selectedThemeId: String,
    onThemeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTheme = dayPilotThemes.find { it.id == selectedThemeId }
        ?: dayPilotThemes.first()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tema de color",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Paleta expandible
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Franjas de colores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    dayPilotThemes.forEach { theme ->
                        val isSelected = theme.id == selectedThemeId
                        val weight by animateDpAsState(
                            targetValue = if (isSelected) 80.dp else 40.dp,
                            animationSpec = tween(300),
                            label = "theme_weight_${theme.id}"
                        )

                        Box(
                            modifier = Modifier
                                .width(weight)
                                .fillMaxHeight()
                                .background(theme.colors.first())
                                .clickable { onThemeSelect(theme.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Mini paleta del tema seleccionado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    selectedTheme.colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(color)
                        )
                    }
                }

                // Nombre del tema activo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTheme.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(selectedTheme.colors.first())
                    )
                }
            }
        }
    }
}

// ── Selector modo oscuro ─────────────────────────────────────────
@Composable
fun DayPilotDarkModeSelector(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode
                        else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Modo oscuro",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Activado" else "Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// ── Selector de región/idioma ────────────────────────────────────
@Composable
fun DayPilotOptionSelector(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedOption: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedOption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(option)
                                expanded = false
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(horizontal = 68.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (option != options.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 68.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DayPilotSelectorsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        var selectedTheme by remember { mutableStateOf("sage_green") }
        var isDarkMode    by remember { mutableStateOf(true) }
        var language      by remember { mutableStateOf("Español") }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DayPilotThemeSelector(
                selectedThemeId = selectedTheme,
                onThemeSelect = { selectedTheme = it }
            )
            DayPilotDarkModeSelector(
                isDarkMode = isDarkMode,
                onToggle = { isDarkMode = it }
            )
            DayPilotOptionSelector(
                title = "Idioma",
                subtitle = language,
                icon = Icons.Default.Language,
                selectedOption = language,
                options = listOf("Español", "English", "Deutsch"),
                onSelect = { language = it }
            )
        }
    }
}