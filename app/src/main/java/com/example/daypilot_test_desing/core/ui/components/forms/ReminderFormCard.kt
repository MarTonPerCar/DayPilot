package com.example.daypilot_test_desing.core.ui.components.forms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotTextField
import com.example.daypilot_test_desing.core.data.model.FrequencyType
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme
import java.util.Calendar

private fun showDateTimePicker(context: Context, onSelected: (Calendar) -> Unit) {
    val now = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onSelected(
                        Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                        }
                    )
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
}

@Composable
private fun QuickMinutesSelector(quickMinutes: Int?, onSelect: (Int?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    .clickable { onSelect(if (quickMinutes == minutes) null else minutes) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (minutes == 60) "1h" else "${minutes}m",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrPickDateDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.reminder_form_or_pick_date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DatePickerButton(selectedDate: Calendar?, dateLabel: String, onDateSelected: (Calendar) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = { showDateTimePicker(context) { onDateSelected(it) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selectedDate != null)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else Color.Transparent
        )
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (selectedDate != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FrequencySelector(frequency: FrequencyType, onSelect: (FrequencyType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    .clickable { onSelect(freq) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(freq.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EarlyWarningToggle(earlyWarning: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.reminder_form_early_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.reminder_form_early_warning_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = earlyWarning,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun ReminderFormActionsRow(isValid: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) { Text(stringResource(R.string.common_cancel)) }

        Button(
            onClick = onSave,
            enabled = isValid,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) { Text(stringResource(R.string.reminder_form_create)) }
    }
}

@Composable
fun ReminderFormCard(
    onSave: (ReminderFormDataInfo) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(FrequencyType.ONCE) }
    var earlyWarning by remember { mutableStateOf(false) }
    var quickMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    val isValid = title.isNotBlank() && (quickMinutes != null || selectedDate != null)

    val dateLabel = selectedDate?.let { cal ->
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        "%02d/%02d/%d %02d:%02d".format(day, month, year, hour, min)
    } ?: stringResource(R.string.reminder_form_no_date)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.reminder_form_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            DayPilotTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.reminder_form_name_label)
            )

            Text(
                text = stringResource(R.string.reminder_form_quick_access),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            QuickMinutesSelector(
                quickMinutes = quickMinutes,
                onSelect = { minutes ->
                    quickMinutes = minutes
                    selectedDate = null
                }
            )

            OrPickDateDivider()

            DatePickerButton(
                selectedDate = selectedDate,
                dateLabel = dateLabel,
                onDateSelected = { date ->
                    selectedDate = date
                    quickMinutes = null
                }
            )

            Text(
                text = stringResource(R.string.reminder_form_frequency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FrequencySelector(frequency = frequency, onSelect = { frequency = it })

            EarlyWarningToggle(earlyWarning = earlyWarning, onToggle = { earlyWarning = it })

            ReminderFormActionsRow(
                isValid = isValid,
                onCancel = onCancel,
                onSave = {
                    if (isValid) {
                        onSave(
                            ReminderFormDataInfo(
                                title = title,
                                frequencyType = frequency,
                                earlyWarning = earlyWarning,
                                quickMinutes = quickMinutes,
                                scheduledDateTime = selectedDate
                            )
                        )
                    }
                }
            )
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
