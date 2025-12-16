package com.example.daypilot.firebaseLogic

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object AppOpenStatsSync {

    private fun safeTimeZone(zoneId: String?): TimeZone {
        val id = zoneId?.trim().orEmpty().ifBlank { "UTC" }
        return TimeZone.getTimeZone(id)
    }

    private fun dayKeyNow(tz: TimeZone): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = tz
        return sdf.format(Calendar.getInstance(tz).time)
    }

    private fun dayKeyMinusDays(tz: TimeZone, days: Int): String {
        val cal = Calendar.getInstance(tz)
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = tz
        return sdf.format(cal.time)
    }

    suspend fun run(uid: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(uid)

        val userSnap = userRef.get().await()

        val tz = safeTimeZone(userSnap.getString("region"))
        val todayKey = dayKeyNow(tz)
        val keepMinKey = dayKeyMinusDays(tz, 29)

        val currentStepsDate = userSnap.getString("todayStepsDate")
        val currentPointsDate = userSnap.getString("todayPointsDate")

        val rollover = mutableMapOf<String, Any>()
        if (currentStepsDate != todayKey) {
            rollover["todaySteps"] = 0L
            rollover["todayStepsDate"] = todayKey
        }
        if (currentPointsDate != todayKey) {
            rollover["todayPoints"] = 0L
            rollover["todayPointsDate"] = todayKey
        }

        rollover["pointsZoneId"] = tz.id
        rollover["pointsUpdatedAt"] = FieldValue.serverTimestamp()

        if (rollover.isNotEmpty()) {
            userRef.set(rollover, SetOptions.merge()).await()
        }

        val todayDailyRef = userRef.collection("pointsDaily").document(todayKey)
        todayDailyRef.set(
            mapOf(
                "date" to todayKey,
                "zoneId" to tz.id,
                "total" to 0L,
                "tasks" to 0L,
                "steps" to 0L,
                "wellness" to 0L,
                "tasksCount" to 0L,
                "stepsCount" to 0L,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()

        sweepPointsDailyOlderThan(userRef = userRef, minKeyExclusive = keepMinKey)
        sweepPointsLogOlderThan(userRef = userRef, minKeyExclusive = keepMinKey)
        recomputeRollingTotals(userRef = userRef, todayKey = todayKey)
    }

    private suspend fun sweepPointsDailyOlderThan(
        userRef: com.google.firebase.firestore.DocumentReference,
        minKeyExclusive: String
    ) {
        val col = userRef.collection("pointsDaily")
        while (true) {
            val snap = col
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
                .whereLessThan(FieldPath.documentId(), minKeyExclusive)
                .limit(450)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = userRef.firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private suspend fun sweepPointsLogOlderThan(
        userRef: com.google.firebase.firestore.DocumentReference,
        minKeyExclusive: String
    ) {
        val col = userRef.collection("pointsLog")
        while (true) {
            val snap = col
                .orderBy("dayKey", Query.Direction.ASCENDING)
                .whereLessThan("dayKey", minKeyExclusive)
                .limit(450)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = userRef.firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private suspend fun recomputeRollingTotals(
        userRef: com.google.firebase.firestore.DocumentReference,
        todayKey: String
    ) {
        val snap = userRef.collection("pointsDaily")
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .await()

        var total = 0L
        var tasks = 0L
        var steps = 0L
        var wellness = 0L
        var tasksCount = 0L
        var stepsCount = 0L
        var todayTotal = 0L

        snap.documents.forEach { d ->
            val dTotal = d.getLong("total") ?: 0L
            val dTasks = d.getLong("tasks") ?: 0L
            val dSteps = d.getLong("steps") ?: 0L
            val dWell = d.getLong("wellness") ?: 0L
            val dTasksCount = d.getLong("tasksCount") ?: 0L
            val dStepsCount = d.getLong("stepsCount") ?: 0L

            total += dTotal
            tasks += dTasks
            steps += dSteps
            wellness += dWell
            tasksCount += dTasksCount
            stepsCount += dStepsCount

            if (d.id == todayKey) todayTotal = dTotal
        }

        userRef.set(
            mapOf(
                "totalPoints" to total,
                "pointsTasks" to tasks,
                "pointsSteps" to steps,
                "pointsWellness" to wellness,
                "todayPoints" to todayTotal,
                "todayPointsDate" to todayKey,
                "tasksCount30d" to tasksCount,
                "stepsCount30d" to stepsCount,
                "pointsUpdatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }
}