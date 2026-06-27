package com.example.daypilot_supabase_test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class TaskRow(
    val id: String? = null,
    val title: String = "",
    val category: String = "",
    val difficulty: String = "",
    val is_completed: Boolean = false
)

@Serializable
data class NewTask(
    val user_id: String,
    val title: String,
    val category: String = "General",
    val difficulty: String = "EASY"
)

@Composable
fun DbTestScreen() {
    var log by remember { mutableStateOf("Sin acciones todavía.") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("DayPilot — prueba Supabase", style = MaterialTheme.typography.titleLarge)

        Button(onClick = {
            scope.launch {
                log = try {
                    supabase.auth.signInWith(Email) {
                        email = "ana.garcia@daypilot.test"
                        password = "password123"
                    }
                    "Login OK. UID: ${supabase.auth.currentUserOrNull()?.id}"
                } catch (e: Exception) {
                    "Error login: ${e.message}"
                }
            }
        }) { Text("1. Login") }

        Button(onClick = {
            scope.launch {
                log = try {
                    val tasks = supabase.postgrest.from("tasks")
                        .select()
                        .decodeList<TaskRow>()
                    "Tareas: ${tasks.size}\n${tasks.joinToString("\n") { "- ${it.title}" }}"
                } catch (e: Exception) {
                    "Error tareas: ${e.message}"
                }
            }
        }) { Text("2. Traer tareas") }

        Button(onClick = {
            scope.launch {
                val uid = supabase.auth.currentUserOrNull()?.id
                log = if (uid == null) {
                    "Primero haz login"
                } else {
                    try {
                        supabase.postgrest.from("tasks")
                            .insert(NewTask(user_id = uid, title = "Tarea desde Android"))
                        "Tarea insertada OK"
                    } catch (e: Exception) {
                        "Error insertar: ${e.message}"
                    }
                }
            }
        }) { Text("3. Insertar tarea") }

        HorizontalDivider()
        Text("Resultado:", style = MaterialTheme.typography.titleMedium)
        Text(log)
    }
}