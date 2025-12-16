package com.example.daypilot.firebaseLogic.pointsLogic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
class PointsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ========== Refs ==========

    private fun userRef(uid: String) = firestore.collection("users").document(uid)
    private fun logRef(uid: String) = userRef(uid).collection("pointsLog").document()
    private fun dailyRef(uid: String, dayKey: String) =
        userRef(uid).collection("pointsDaily").document(dayKey)

    // ========== Public API ==========

    suspend fun addPoints(
        uid: String,
        points: Long,
        source: PointSource,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        if (points == 0L) return

        firestore.runTransaction { tx ->
            val uRef = userRef(uid)
            val uSnap = tx.get(uRef)

            val region = uSnap.getString("region")
            val zoneId = PointsTime.zoneIdFromRegion(region)
            val todayKey = PointsTime.todayKey(zoneId)

            val lastPrunedKey = uSnap.getString("rollingLastPruneKey")
            val targetOutKey = PointsTime.outKey(zoneId)

            val pruneKeys = computePruneKeys(lastPrunedKey, targetOutKey)
            val pruneSnaps = pruneKeys.map { k -> tx.get(dailyRef(uid, k)) }

            var subTotal = 0L
            var subTasks = 0L
            var subSteps = 0L
            var subWell = 0L

            pruneSnaps.forEach { s ->
                if (s.exists()) {
                    subTotal += (s.getLong("total") ?: 0L)
                    subTasks += (s.getLong("tasks") ?: 0L)
                    subSteps += (s.getLong("steps") ?: 0L)
                    subWell += (s.getLong("wellness") ?: 0L)
                }
            }

            pruneKeys.forEach { k ->
                tx.delete(dailyRef(uid, k))
            }

            val tasksCountDelta: Long =
                if (source == PointSource.TASKS)
                    (metadata["tasksCountDelta"] as? Number)?.toLong() ?: 1L
                else 0L

            val stepsCountDelta: Long =
                if (source == PointSource.STEPS)
                    (metadata["stepsCountDelta"] as? Number)?.toLong() ?: 0L
                else 0L

            val eRef = logRef(uid)
            val logData = mutableMapOf<String, Any?>(
                "points" to points,
                "source" to source.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "dayKey" to todayKey,
                "zoneId" to zoneId.id
            )
            if (metadata.isNotEmpty()) logData["metadata"] = metadata
            tx.set(eRef, logData)

            val dRef = dailyRef(uid, todayKey)
            val dailyUpdate = mutableMapOf<String, Any?>(
                "date" to todayKey,
                "zoneId" to zoneId.id,
                "updatedAt" to FieldValue.serverTimestamp(),
                "total" to FieldValue.increment(points)
            )
            dailyUpdate[sourceField(source)] = FieldValue.increment(points)

            if (tasksCountDelta != 0L) {
                dailyUpdate["tasksCount"] = FieldValue.increment(tasksCountDelta)
            }
            if (stepsCountDelta != 0L) {
                dailyUpdate["stepsCount"] = FieldValue.increment(stepsCountDelta)
            }

            tx.set(dRef, dailyUpdate, SetOptions.merge())

            val deltaTotal = points - subTotal
            val deltaTasks = (if (source == PointSource.TASKS) points else 0L) - subTasks
            val deltaSteps = (if (source == PointSource.STEPS) points else 0L) - subSteps
            val deltaWell = (if (source == PointSource.WELLNESS) points else 0L) - subWell

            val userUpdates = mutableMapOf<String, Any?>(
                "totalPoints" to FieldValue.increment(deltaTotal),
                "pointsTasks" to FieldValue.increment(deltaTasks),
                "pointsSteps" to FieldValue.increment(deltaSteps),
                "pointsWellness" to FieldValue.increment(deltaWell)
            )

            val storedTodayKey = uSnap.getString("todayPointsDate")
            if (storedTodayKey != todayKey) {
                userUpdates["todayPointsDate"] = todayKey
                userUpdates["todayPoints"] = points
            } else {
                userUpdates["todayPoints"] = FieldValue.increment(points)
            }

            if (source == PointSource.STEPS && stepsCountDelta != 0L) {
                val storedStepsKey = uSnap.getString("todayStepsDate")
                if (storedStepsKey != todayKey) {
                    userUpdates["todayStepsDate"] = todayKey
                    userUpdates["todaySteps"] = stepsCountDelta
                } else {
                    userUpdates["todaySteps"] = FieldValue.increment(stepsCountDelta)
                }
            }

            if (pruneKeys.isNotEmpty()) {
                userUpdates["rollingLastPruneKey"] = targetOutKey
            } else if (lastPrunedKey.isNullOrBlank()) {
                userUpdates["rollingLastPruneKey"] = targetOutKey
            }

            userUpdates["pointsSystemVersion"] = 2L
            userUpdates["pointsZoneId"] = zoneId.id
            userUpdates["pointsUpdatedAt"] = FieldValue.serverTimestamp()

            tx.update(uRef, userUpdates)
            null
        }.await()
    }

    suspend fun refreshRollingTotals(uid: String) {

        firestore.runTransaction { tx ->
            val uRef = userRef(uid)
            val uSnap = tx.get(uRef)

            val region = uSnap.getString("region")
            val zoneId = PointsTime.zoneIdFromRegion(region)

            val lastPrunedKey = uSnap.getString("rollingLastPruneKey")
            val targetOutKey = PointsTime.outKey(zoneId)

            val pruneKeys = computePruneKeys(lastPrunedKey, targetOutKey)
            if (pruneKeys.isEmpty()) return@runTransaction null

            val snaps = pruneKeys.map { k -> tx.get(dailyRef(uid, k)) }

            var subTotal = 0L
            var subTasks = 0L
            var subSteps = 0L
            var subWell = 0L

            snaps.forEach { s ->
                if (s.exists()) {
                    subTotal += (s.getLong("total") ?: 0L)
                    subTasks += (s.getLong("tasks") ?: 0L)
                    subSteps += (s.getLong("steps") ?: 0L)
                    subWell += (s.getLong("wellness") ?: 0L)
                }
            }

            val userUpdates = mutableMapOf<String, Any?>(
                "totalPoints" to FieldValue.increment(-subTotal),
                "pointsTasks" to FieldValue.increment(-subTasks),
                "pointsSteps" to FieldValue.increment(-subSteps),
                "pointsWellness" to FieldValue.increment(-subWell),
                "rollingLastPruneKey" to targetOutKey,
                "pointsZoneId" to zoneId.id,
                "pointsUpdatedAt" to FieldValue.serverTimestamp()
            )
            tx.update(uRef, userUpdates)

            pruneKeys.forEach { k ->
                tx.delete(dailyRef(uid, k))
            }

            null
        }.await()
    }

    suspend fun purgePointsLogOlderThan30Days(uid: String) {

        val uSnap = userRef(uid).get().await()
        val zoneId = PointsTime.zoneIdFromRegion(uSnap.getString("region"))
        val cutoffTs = cutoffTimestampForLog(zoneId)

        val col = userRef(uid).collection("pointsLog")

        while (true) {
            val snap = col
                .whereLessThan("createdAt", cutoffTs)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(450)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    suspend fun sweepPointsDailyOlderThan30Days(uid: String) {

        val uSnap = userRef(uid).get().await()
        val zoneId = PointsTime.zoneIdFromRegion(uSnap.getString("region"))
        val minKey = PointsTime.minKey(zoneId)

        val col = userRef(uid).collection("pointsDaily")

        while (true) {
            val snap = col
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
                .whereLessThan(FieldPath.documentId(), minKey)
                .limit(450)
                .get()
                .await()

            if (snap.isEmpty) break

            val batch = firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    suspend fun refreshAndPurge(uid: String) {
        refreshRollingTotals(uid)
        sweepPointsDailyOlderThan30Days(uid)
        purgePointsLogOlderThan30Days(uid)
    }

    // ========== Internals ==========

    private fun computePruneKeys(lastPrunedKey: String?, targetOutKey: String): List<String> {
        val last = lastPrunedKey?.takeIf { it.isNotBlank() } ?: return listOf(targetOutKey)
        return try {
            val lastDate = PointsTime.parseKey(last)
            val outDate = PointsTime.parseKey(targetOutKey)
            if (!lastDate.isBefore(outDate)) emptyList()
            else {
                val keys = mutableListOf<String>()
                var d = lastDate.plusDays(1)
                while (!d.isAfter(outDate)) {
                    keys.add(d.toString())
                    d = d.plusDays(1)
                    if (keys.size > 120) break
                }
                keys
            }
        } catch (_: Exception) {
            listOf(targetOutKey)
        }
    }

    private fun cutoffTimestampForLog(zoneId: ZoneId): Timestamp {
        val cutoffInstant = Instant.now()
            .atZone(zoneId)
            .toLocalDate()
            .minusDays(29)
            .atStartOfDay(zoneId)
            .toInstant()

        return Timestamp(cutoffInstant.epochSecond, 0)
    }

    private fun sourceField(source: PointSource): String {
        return when (source) {
            PointSource.TASKS -> "tasks"
            PointSource.STEPS -> "steps"
            PointSource.WELLNESS -> "wellness"
        }
    }
}