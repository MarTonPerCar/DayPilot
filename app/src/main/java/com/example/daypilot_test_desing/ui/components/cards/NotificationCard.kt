package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

enum class NotificationType(
    val icon: ImageVector,
    val color: Color
) {
    TASK(Icons.Default.CheckCircle,    Color(0xFF4CAF50)),
    SOCIAL(Icons.Default.People,       Color(0xFF2196F3)),
    STEPS(Icons.AutoMirrored.Filled.DirectionsWalk,Color(0xFFFF9800)),
    STREAK(Icons.Default.Whatshot, Color(0xFFFF5722)),
    REMINDER(Icons.Default.Notifications, Color(0xFF9C27B0)),
    ACHIEVEMENT(Icons.Default.EmojiEvents, Color(0xFFFFD700))
}

@Composable
fun NotificationCard(
    title: String,
    message: String,
    timeAgo: String,
    type: NotificationType,
    isRead: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icono con color del tipo
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

        // Contenido
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

        // Punto de no leída
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