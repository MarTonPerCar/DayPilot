package com.example.daypilot.firebaseLogic.taskLogic

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ========== Refs ==========

    private fun tasksCollection(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("tasks")

    // ========== Read ==========

    suspend fun getTasks(uid: String): List<Task> {
        val snap = tasksCollection(uid).get().await()

        return snap.documents.map { doc ->
            val data = doc.data.orEmpty()

            val difficulty = runCatching {
                TaskDifficulty.valueOf(data["difficulty"] as? String ?: "MEDIUM")
            }.getOrElse { TaskDifficulty.MEDIUM }

            val createdAt =
                (data["createdAt"] as? Number)?.toLong()
                    ?: (data["createdAt"] as? Timestamp)?.toDate()?.time
                    ?: 0L

            Task(
                id = doc.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                difficulty = difficulty,
                estimatedMinutes = (data["estimatedMinutes"] as? Number)?.toInt() ?: 30,
                days = (data["days"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
                category = data["category"] as? String ?: "General",
                reminderEnabled = data["reminderEnabled"] as? Boolean ?: false,
                createdAt = createdAt,
                isCompleted = data["isCompleted"] as? Boolean ?: false,
                completedAt = data["completedAt"] as? Timestamp
            )
        }
    }

    // ========== Create ==========

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

    // ========== Update ==========

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
        )
        ref.update(data).await()
    }

    // ========== Delete ==========

    suspend fun deleteTask(uid: String, taskId: String) {
        tasksCollection(uid).document(taskId).delete().await()
    }

    // ========== Complete ==========

    suspend fun completeTask(uid: String, task: Task) {
        if (task.id.isBlank()) return

        tasksCollection(uid)
            .document(task.id)
            .update(
                mapOf(
                    "isCompleted" to true,
                    "completedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}