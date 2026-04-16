package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TaskDifficulty(val label: String, val color: Color) {
    EASY("Fácil",    Color(0xFF4CAF50)),
    MEDIUM("Media",  Color(0xFFFF9800)),
    HARD("Difícil",  Color(0xFFF44336))
}

enum class TaskCategory(val label: String, val icon: ImageVector) {
    WORK("Trabajo",      Icons.Default.Build),
    STUDY("Estudio",     Icons.Default.Create),
    SPORT("Deporte",     Icons.Default.PlayArrow),
    HEALTH("Salud",      Icons.Default.Favorite),
    PERSONAL("Personal", Icons.Default.Person),
    HOME("Hogar",        Icons.Default.Home),
    OTHER("Otro",        Icons.Default.Star)
}