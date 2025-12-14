package com.example.daypilot.firebaseLogic.taskLogic

import android.util.Log
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val authRepo: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun tasksCollection(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("tasks")

    // Crear tarea
    suspend fun createTask(uid: String, task: Task): String {
        return try {
            val docRef = tasksCollection(uid).document()
            val toSave = task.copy(id = docRef.id)
            docRef.set(toSave).await()
            docRef.id
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error creando tarea", e)
            throw e
        }
    }

    // Actualizar tarea
    suspend fun updateTask(uid: String, task: Task) {
        if (task.id.isBlank()) {
            Log.w("TaskRepo", "updateTask: id vacío, no se actualiza")
            return
        }
        try {
            tasksCollection(uid)
                .document(task.id)
                .set(task, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error actualizando tarea", e)
            throw e
        }
    }

    // Eliminar tarea
    suspend fun deleteTask(uid: String, taskId: String) {
        try {
            tasksCollection(uid)
                .document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error eliminando tarea", e)
            throw e
        }
    }

    // Obtener todas las tareas del usuario
    suspend fun getTasks(uid: String): List<Task> {
        return try {
            val snap = tasksCollection(uid)
                .orderBy("createdAt")
                .get()
                .await()

            snap.documents.mapNotNull { doc ->
                val task = doc.toObject(Task::class.java)
                task?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("TaskRepo", "Error obteniendo tareas", e)
            emptyList()
        }
    }

    suspend fun completeTask(uid: String, task: Task) {
        if (task.id.isBlank()) {
            throw IllegalArgumentException("Task id vacío, no se puede completar.")
        }

        val userRef = firestore.collection("users").document(uid)
        val taskRef = userRef.collection("tasks").document(task.id)

        firestore.runTransaction { tx ->
            val taskSnap = tx.get(taskRef)
            if (!taskSnap.exists()) {
                throw IllegalStateException("La tarea ya no existe en Firestore.")
            }

            // 1) marcar la tarea como completada
            tx.update(
                taskRef,
                mapOf(
                    "isCompleted" to true,
                    "completedAt" to FieldValue.serverTimestamp()
                )
            )

            // 2) sumar 2 puntos al usuario (totalPoints y pointsTasks)
            val userSnap = tx.get(userRef)
            val currentTotal = userSnap.getLong("totalPoints") ?: 0L
            val currentTasks = userSnap.getLong("pointsTasks") ?: 0L

            tx.update(
                userRef,
                mapOf(
                    "totalPoints" to currentTotal + 2L,
                    "pointsTasks" to currentTasks + 2L
                )
            )

            // 3) añadir entrada al log de puntos
            val logRef = userRef.collection("pointsLog").document()
            val logData = mapOf(
                "points" to 2L,
                "source" to "TASKS",
                "metadata" to mapOf(
                    "taskId" to task.id,
                    "title" to task.title
                ),
                "createdAt" to FieldValue.serverTimestamp()
            )
            tx.set(logRef, logData)
        }.await()
    }
}