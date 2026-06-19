package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.model.NotificationData
import com.example.daypilot_test_desing.data.model.NotificationType
import com.example.daypilot_test_desing.data.repository.NotificationRepository

object FakeNotificationRepository : NotificationRepository {
    private val notifications = mutableListOf(
        NotificationData("n1", "Tarea completada",      "Completaste \"Reunión de equipo\"",        "hace 5 min",  NotificationType.TASK,        false),
        NotificationData("n2", "Ana García te reaccionó", "Ana te envió 🔥 esta semana",           "hace 1h",     NotificationType.SOCIAL,      false),
        NotificationData("n3", "Meta de pasos alcanzada","¡Superaste los 2.000 pasos de hoy!",    "hace 2h",     NotificationType.STEPS,       false),
        NotificationData("n4", "¡Racha de 7 días!",    "Llevas 7 días consecutivos activo",        "hace 3h",     NotificationType.STREAK,      true),
        NotificationData("n5", "Recordatorio",          "Tienes una tarea programada para mañana", "hace 5h",     NotificationType.REMINDER,    true),
        NotificationData("n6", "Nuevo logro",           "Desbloqueaste el logro \"Constante\"",    "ayer",        NotificationType.ACHIEVEMENT, true)
    )

    override fun getNotifications(): List<NotificationData> = notifications.toList()

    override fun markAsRead(id: String) {
        val idx = notifications.indexOfFirst { it.id == id }
        if (idx >= 0) notifications[idx] = notifications[idx].copy(isRead = true)
    }
}
