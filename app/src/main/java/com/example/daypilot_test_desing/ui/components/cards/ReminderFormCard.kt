package com.example.daypilot_test_desing.ui.components.cards

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTextField
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import java.util.Calendar

data class ReminderFormData(
    val title: String,
    val frequencyType: FrequencyType,
    val earlyWarning: Boolean,
    val quickMinutes: Int?,
    val scheduledDateTime: Calendar?
)

enum class FrequencyType(val label: String) {
    ONCE("Una vez"),
    DAILY("Diario"),
    WEEKLY("Semanal")
}

@Composable
fun ReminderFormCard(
    onSave: (ReminderFormData) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title         by remember { mutableStateOf("") }
    var frequency     by remember { mutableStateOf(FrequencyType.ONCE) }
    var earlyWarning  by remember { mutableStateOf(false) }
    var quickMinutes  by remember { mutableStateOf<Int?>(null) }
    var selectedDate  by remember { mutableStateOf<Calendar?>(null) }

    val isValid = title.isNotBlank() && (quickMinutes != null || selectedDate != null)

    val dateLabel = selectedDate?.let { cal ->
        val day   = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year  = cal.get(Calendar.YEAR)
        val hour  = cal.get(Calendar.HOUR_OF_DAY)
        val min   = cal.get(Calendar.MINUTE)
        "%02d/%02d/%d %02d:%02d".format(day, month, year, hour, min)
    } ?: "Sin fecha seleccionada"

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Título ───────────────────────────────────────────
            Text(
                text       = "Nuevo recordatorio",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            // ── Nombre ───────────────────────────────────────────
            DayPilotTextField(
                value         = title,
                onValueChange = { title = it },
                label         = "Nombre del recordatorio"
            )

            // ── Accesos rápidos ──────────────────────────────────
            Text(
                text  = "Acceso rápido",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30, 60).forEach { minutes ->
                    val isSelected = quickMinutes == minutes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                quickMinutes = if (quickMinutes == minutes) null else minutes
                                selectedDate = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = if (minutes == 60) "1h" else "${minutes}m",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Separador ────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text  = "o elige fecha y hora",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // ── Selector fecha y hora ─────────────────────────────
            OutlinedButton(
                onClick = {
                    val now = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    selectedDate = Calendar.getInstance().apply {
                                        set(year, month, day, hour, minute)
                                    }
                                    quickMinutes = null
                                },
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedDate != null)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        Color.Transparent
                )
            ) {
                Icon(
                    imageVector        = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedDate != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Frecuencia ────────────────────────────────────────
            Text(
                text  = "Frecuencia",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrequencyType.entries.forEach { freq ->
                    val isSelected = frequency == freq
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { frequency = freq }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = freq.label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Aviso previo ──────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text       = "Aviso previo",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text  = "Notifica 10 min antes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked         = earlyWarning,
                        onCheckedChange = { earlyWarning = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // ── Botones ───────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onCancel,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick  = {
                        if (isValid) {
                            onSave(
                                ReminderFormData(
                                    title           = title,
                                    frequencyType   = frequency,
                                    earlyWarning    = earlyWarning,
                                    quickMinutes    = quickMinutes,
                                    scheduledDateTime = selectedDate
                                )
                            )
                        }
                    },
                    enabled  = isValid,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear recordatorio")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderFormCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ReminderFormCard(onSave = {}, onCancel = {})
        }
    }
}