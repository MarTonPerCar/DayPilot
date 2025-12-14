package com.example.daypilot.firebaseLogic.taskLogic

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun tasksCollection(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("tasks")

    // ---------- GET TASKS (lee bien isCompleted y completedAt) ----------
    suspend fun getTasks(uid: String): List<Task> {
        val snap = tasksCollection(uid).get().await()

        return snap.documents.map { doc ->
            val data = doc.data ?: emptyMap<String, Any?>()

            val difficultyStr = data["difficulty"] as? String ?: "MEDIUM"
            val difficulty = runCatching {
                TaskDifficulty.valueOf(difficultyStr)
            }.getOrElse { TaskDifficulty.MEDIUM }

            val createdAtLong =
                (data["createdAt"] as? Number)?.toLong()
                    ?: (data["createdAt"] as? Timestamp)?.toDate()?.time
                    ?: 0L

            val completedAtTs = data["completedAt"] as? Timestamp

            Task(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                difficulty = difficulty,
                estimatedMinutes = (data["estimatedMinutes"] as? Number)?.toInt() ?: 30,
                days = (data["days"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                category = data["category"] as? String ?: "General",
                reminderEnabled = data["reminderEnabled"] as? Boolean ?: false,
                createdAt = createdAtLong,
                isCompleted = data["isCompleted"] as? Boolean ?: false,   // 👈 AQUÍ
                completedAt = completedAtTs                                   // 👈 Y AQUÍ
            )
        }
    }

    // ---------- CREATE ----------
    suspend fun createTask(uid: String, task: Task) {
        val ref = tasksCollection(uid).document()
        val data = mapOf(
            "title" to task.title,
            "description" to task.description,
            "difficulty" to task.difficulty.name,
            "estimatedMinutes" to task.estimatedMinutes,
            "days" to task.days,
            "category" to task.category,
            "reminderEnabled" to task.reminderEnabled,
            "createdAt" to FieldValue.serverTimestamp(),
            "isCompleted" to false,
            "completedAt" to null
        )
        ref.set(data).await()
    }

    // ---------- UPDATE ----------
    suspend fun updateTask(uid: String, task: Task) {
        if (task.id.isBlank()) return
        val ref = tasksCollection(uid).document(task.id)
        val data = mapOf(
            "title" to task.title,
            "description" to task.description,
            "difficulty" to task.difficulty.name,
            "estimatedMinutes" to task.estimatedMinutes,
            "days" to task.days,
            "category" to task.category,
            "reminderEnabled" to task.reminderEnabled
            // createdAt / isCompleted / completedAt no se tocan aquí
        )
        ref.update(data).await()
    }

    // ---------- DELETE ----------
    suspend fun deleteTask(uid: String, taskId: String) {
        tasksCollection(uid).document(taskId).delete().await()
    }

    // ---------- COMPLETE (solo tocar la tarea) ----------
    suspend fun completeTask(uid: String, task: Task) {
        if (task.id.isBlank()) return

        val ref = tasksCollection(uid).document(task.id)

        // Aquí solo marcamos la tarea; los puntos los hace AuthRepository.addPoints
        ref.update(
            mapOf(
                "isCompleted" to true,
                "completedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}