package com.example.daypilot.firebaseLogic.pointsLogic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant
import com.google.firebase.firestore.Query
import java.time.ZoneId
import com.google.firebase.firestore.FieldPath

@RequiresApi(Build.VERSION_CODES.O)
class PointsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userRef(uid: String) = firestore.collection("users").document(uid)
    private fun logRef(uid: String) = userRef(uid).collection("pointsLog").document()
    private fun dailyRef(uid: String, dayKey: String) = userRef(uid).collection("pointsDaily").document(dayKey)

    suspend fun addPoints(
        uid: String,
        points: Long,
        source: PointSource,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        if (points == 0L) return

        ensureInitializedV2(uid)

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

            // --------- NUEVO: contadores (tareas/pasos) ---------
            val tasksCountDelta: Long =
                if (source == PointSource.TASKS)
                    (metadata["tasksCountDelta"] as? Number)?.toLong() ?: 1L
                else 0L

            val stepsCountDelta: Long =
                if (source == PointSource.STEPS)
                    (metadata["stepsCountDelta"] as? Number)?.toLong() ?: 0L
                else 0L

            // --- ESCRITURAS ---
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

            // ✅ guardamos contadores para la gráfica
            if (tasksCountDelta != 0L) {
                dailyUpdate["tasksCount"] = FieldValue.increment(tasksCountDelta)
            }
            if (stepsCountDelta != 0L) {
                dailyUpdate["stepsCount"] = FieldValue.increment(stepsCountDelta)
            }

            tx.set(dRef, dailyUpdate, SetOptions.merge())

            // --- rolling points (igual que ya tenías) ---
            val deltaTotal = points - subTotal
            val deltaTasks = (if (source == PointSource.TASKS) points else 0L) - subTasks
            val deltaSteps = (if (source == PointSource.STEPS) points else 0L) - subSteps
            val deltaWell  = (if (source == PointSource.WELLNESS) points else 0L) - subWell

            val userUpdates = mutableMapOf<String, Any?>(
                "totalPoints" to FieldValue.increment(deltaTotal),
                "pointsTasks" to FieldValue.increment(deltaTasks),
                "pointsSteps" to FieldValue.increment(deltaSteps),
                "pointsWellness" to FieldValue.increment(deltaWell),
            )

            // todayPoints (puntos del día)
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

    suspend fun ensureInitializedV2(uid: String) {
        val uRef = userRef(uid)
        val uSnap = uRef.get().await()

        val ver = (uSnap.getLong("pointsSystemVersion") ?: 0L)
        if (ver >= 2L) return

        val region = uSnap.getString("region")
        val zoneId = PointsTime.zoneIdFromRegion(region)

        val startInstant = Instant.now().atZone(zoneId).toLocalDate().minusDays(29)
            .atStartOfDay(zoneId).toInstant()
        val startTs = Timestamp(startInstant.epochSecond, 0)

        val logs = uRef.collection("pointsLog")
            .whereGreaterThanOrEqualTo("createdAt", startTs)
            .orderBy("createdAt")
            .get()
            .await()

        // Agrega por día
        data class Agg(var total: Long = 0, var tasks: Long = 0, var steps: Long = 0, var well: Long = 0)

        val map = linkedMapOf<String, Agg>()

        for (doc in logs.documents) {
            val pts = (doc.getLong("points") ?: 0L)
            val src = doc.getString("source") ?: continue
            val createdAt = doc.getTimestamp("createdAt") ?: continue

            val dayKey = PointsTime.keyFromInstant(createdAt.toDate().toInstant(), zoneId)
            val agg = map.getOrPut(dayKey) { Agg() }
            agg.total += pts
            when (src) {
                PointSource.TASKS.name -> agg.tasks += pts
                PointSource.STEPS.name -> agg.steps += pts
                PointSource.WELLNESS.name -> agg.well += pts
            }
        }

        // Escribe pointsDaily (máximo 30 docs)
        val batch = firestore.batch()

        var rollingTotal = 0L
        var rollingTasks = 0L
        var rollingSteps = 0L
        var rollingWell = 0L

        map.forEach { (dayKey, agg) ->
            rollingTotal += agg.total
            rollingTasks += agg.tasks
            rollingSteps += agg.steps
            rollingWell += agg.well

            val dRef = dailyRef(uid, dayKey)
            val data = mapOf(
                "date" to dayKey,
                "zoneId" to zoneId.id,
                "total" to agg.total,
                "tasks" to agg.tasks,
                "steps" to agg.steps,
                "wellness" to agg.well,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(dRef, data, SetOptions.merge())
        }

        val outKey = PointsTime.outKey(zoneId)

        val userUpdate = mutableMapOf<String, Any?>(
            "pointsSystemVersion" to 2L,
            "pointsZoneId" to zoneId.id,
            "rollingLastPruneKey" to outKey,

            // rolling (total actual = 30 días)
            "totalPoints" to rollingTotal,
            "pointsTasks" to rollingTasks,
            "pointsSteps" to rollingSteps,
            "pointsWellness" to rollingWell,

            "todayPointsDate" to PointsTime.todayKey(zoneId),
            "todayPoints" to 0L,
            "pointsUpdatedAt" to FieldValue.serverTimestamp()
        )

        batch.set(uRef, userUpdate, SetOptions.merge())
        batch.commit().await()
    }

    suspend fun getTodayPoints(uid: String): Long {
        val uSnap = userRef(uid).get().await()
        val zoneId = PointsTime.zoneIdFromRegion(uSnap.getString("region"))
        val key = PointsTime.todayKey(zoneId)
        val dSnap = dailyRef(uid, key).get().await()
        return dSnap.getLong("total") ?: 0L
    }

    suspend fun getRollingTotal(uid: String): Long {
        val uSnap = userRef(uid).get().await()
        return uSnap.getLong("totalPoints") ?: 0L
    }

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
                    keys.add(d.toString()) // ISO_LOCAL_DATE = yyyy-MM-dd
                    d = d.plusDays(1)
                    // safety: por si alguien vuelve tras meses
                    if (keys.size > 120) break
                }
                keys
            }
        } catch (_: Exception) {
            listOf(targetOutKey)
        }
    }

    suspend fun refreshRollingTotals(uid: String) {
        ensureInitializedV2(uid)

        firestore.runTransaction { tx ->
            val uRef = userRef(uid)
            val uSnap = tx.get(uRef)

            val region = uSnap.getString("region")
            val zoneId = PointsTime.zoneIdFromRegion(region)

            val lastPrunedKey = uSnap.getString("rollingLastPruneKey")
            val targetOutKey = PointsTime.outKey(zoneId) // hoy-30 (fuera ventana)

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

            // ---- updates rolling totals ----
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

    private fun cutoffTimestampForLog(zoneId: ZoneId): Timestamp {
        val cutoffInstant = java.time.Instant.now()
            .atZone(zoneId)
            .toLocalDate()
            .minusDays(29)
            .atStartOfDay(zoneId)
            .toInstant()

        return Timestamp(cutoffInstant.epochSecond, 0)
    }

    suspend fun purgePointsLogOlderThan30Days(uid: String) {
        ensureInitializedV2(uid)

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
            for (doc in snap.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }
    }

    suspend fun sweepPointsDailyOlderThan30Days(uid: String) {
        ensureInitializedV2(uid)

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
}