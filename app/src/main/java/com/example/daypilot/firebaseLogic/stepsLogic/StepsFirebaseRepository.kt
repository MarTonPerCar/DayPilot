package com.example.daypilot.firebaseLogic.stepsLogic

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class StepsFirebaseRepository(
    private val fs: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun upsertDaily(
        uid: String,
        dayKey: String,
        steps: Int,
        goal: Int,
        updatedAtMs: Long = System.currentTimeMillis(),
        finalized: Boolean = false
    ) {
        val doc = fs.collection("users")
            .document(uid)
            .collection("stepsDaily")
            .document(dayKey)

        val data = hashMapOf(
            "dayKey" to dayKey,
            "steps" to steps,
            "goal" to goal,
            "updatedAtMs" to updatedAtMs,
            "finalized" to finalized
        )

        doc.set(data, SetOptions.merge()).await()
    }
}