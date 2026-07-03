package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme


@Composable
fun NotificationCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    timeAgo: String,
    type: NotificationType,
    isRead: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(type.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                tint = type.color,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }

        if (!isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationCard(
                title = "¡Tarea completada!",
                message = "Has completado 'Salir a correr' y ganado 2 puntos",
                timeAgo = "hace 5min",
                type = NotificationType.TASK,
                isRead = false,
                onClick = {}
            )
            NotificationCard(
                title = "Nueva solicitud",
                message = "Ana López quiere ser tu amiga",
                timeAgo = "hace 1h",
                type = NotificationType.SOCIAL,
                isRead = false,
                onClick = {}
            )
            NotificationCard(
                title = "¡Meta de pasos!",
                message = "Has alcanzado el 75% de tu meta diaria",
                timeAgo = "hace 2h",
                type = NotificationType.STEPS,
                isRead = true,
                onClick = {}
            )
            NotificationCard(
                title = "Racha de 7 días 🔥",
                message = "Llevas 7 días consecutivos cumpliendo tus objetivos",
                timeAgo = "ayer",
                type = NotificationType.STREAK,
                isRead = true,
                onClick = {}
            )
            NotificationCard(
                title = "Recordatorio",
                message = "Tienes 3 tareas pendientes para hoy",
                timeAgo = "ayer",
                type = NotificationType.REMINDER,
                isRead = true,
                onClick = {}
            )
        }
    }
}