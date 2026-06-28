package com.example.daypilot_test_desing.backend.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*

enum class NotificationType(
    val icon: ImageVector,
    val color: Color
) {
    TASK(        Icons.Default.CheckCircle,                       Color(0xFF4CAF50)),
    SOCIAL(      Icons.Default.People,                            Color(0xFF2196F3)),
    STEPS(       Icons.AutoMirrored.Filled.DirectionsWalk,        Color(0xFFFF9800)),
    STREAK(      Icons.Default.Whatshot,                          Color(0xFFFF5722)),
    REMINDER(    Icons.Default.Notifications,                     Color(0xFF9C27B0)),
    ACHIEVEMENT( Icons.Default.EmojiEvents,                       Color(0xFFFFD700))
}

data class NotificationData(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean
)