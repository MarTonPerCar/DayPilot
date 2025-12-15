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

@RequiresApi(Build.VERSION_CODES.O)
class PointsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun userRef(uid: String) = firestore.collection("users").document(uid)
    private fun logRef(uid: String) = userRef(uid).collection("pointsLog").document()
    private fun dailyRef(uid: String, dayKey: String) = userRef(uid).collection("pointsDaily").document(dayKey)

    /**
     * Sistema v2:
     * - Ledger: users/{uid}/pointsLog/{event}
     * - Diario: users/{uid}/pointsDaily/{yyyy-MM-dd}
     * - Rolling 30d cacheado en users/{uid}.totalPoints (+ pointsTasks/pointsSteps/pointsWellness)
     */
    suspend fun addPoints(
        uid: String,
        points: Long,
        source: PointSource,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        if (points == 0L) return

        // 1) Asegura migración/initialization v2 (para arreglar lo que ya existe)
        ensureInitializedV2(uid)

        // 2) Transacción: escribe evento + diario + actualiza rolling (con pruning por días antiguos)
        firestore.runTransaction { tx ->
            val uRef = userRef(uid)
            val uSnap = tx.get(uRef)

            val region = uSnap.getString("region")
            val zoneId = PointsTime.zoneIdFromRegion(region)
            val todayKey = PointsTime.todayKey(zoneId)

            val lastPrunedKey = uSnap.getString("rollingLastPruneKey") // key del último día ya expulsado
            val targetOutKey = PointsTime.outKey(zoneId)

            // --- LECTURAS (todas antes de escribir) ---
            val pruneKeys = computePruneKeys(lastPrunedKey, targetOutKey)
            val pruneSnaps = pruneKeys.map { k -> tx.get(dailyRef(uid, k)) }

            // sumas a restar (días que salen de la ventana)
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

            // --- ESCRITURAS ---
            // Evento (ledger)
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

            // Diario
            val dRef = dailyRef(uid, todayKey)
            val dailyUpdate = mutableMapOf<String, Any?>(
                "date" to todayKey,
                "zoneId" to zoneId.id,
                "updatedAt" to FieldValue.serverTimestamp(),
                "total" to FieldValue.increment(points)
            )
            dailyUpdate[sourceField(source)] = FieldValue.increment(points)
            tx.set(dRef, dailyUpdate, SetOptions.merge())

            // User rolling totals (cache)
            // Ventana = hoy-29 .. hoy. Por eso restamos el día(s) "outKey(s)" si toca.
            val deltaTotal = points - subTotal
            val deltaTasks = (if (source == PointSource.TASKS) points else 0L) - subTasks
            val deltaSteps = (if (source == PointSource.STEPS) points else 0L) - subSteps
            val deltaWell = (if (source == PointSource.WELLNESS) points else 0L) - subWell

            val userUpdates = mutableMapOf<String, Any?>(
                // rolling (lo que la app considera "total actual")
                "totalPoints" to FieldValue.increment(deltaTotal),
                "pointsTasks" to FieldValue.increment(deltaTasks),
                "pointsSteps" to FieldValue.increment(deltaSteps),
                "pointsWellness" to FieldValue.increment(deltaWell),
            )

            // “Hoy” (cache rápido, opcional pero útil)
            val storedTodayKey = uSnap.getString("todayPointsDate")
            if (storedTodayKey != todayKey) {
                userUpdates["todayPointsDate"] = todayKey
                userUpdates["todayPoints"] = points
            } else {
                userUpdates["todayPoints"] = FieldValue.increment(points)
            }

            // Mantén compatibilidad con tu campo todaySteps/todayStepsDate (solo para STEPS)
            if (source == PointSource.STEPS) {
                val storedStepsKey = uSnap.getString("todayStepsDate")
                if (storedStepsKey != todayKey) {
                    userUpdates["todayStepsDate"] = todayKey
                    userUpdates["todaySteps"] = points
                } else {
                    userUpdates["todaySteps"] = FieldValue.increment(points)
                }
            }

            if (pruneKeys.isNotEmpty()) {
                userUpdates["rollingLastPruneKey"] = targetOutKey
            } else {
                // si no existía, lo fijamos igual para que quede consistente
                if (lastPrunedKey.isNullOrBlank()) {
                    userUpdates["rollingLastPruneKey"] = targetOutKey
                }
            }

            userUpdates["pointsSystemVersion"] = 2L
            userUpdates["pointsZoneId"] = zoneId.id
            userUpdates["pointsUpdatedAt"] = FieldValue.serverTimestamp()

            tx.update(uRef, userUpdates)

            null
        }.await()
    }

    /**
     * “Arregla lo que ya existe”:
     * Si el usuario no está en v2, crea pointsDaily desde pointsLog (últimos 30 días por region-zone),
     * y convierte totalPoints/pointsTasks/... a rolling 30 días.
     */
    suspend fun ensureInitializedV2(uid: String) {
        val uRef = userRef(uid)
        val uSnap = uRef.get().await()

        val ver = (uSnap.getLong("pointsSystemVersion") ?: 0L)
        if (ver >= 2L) return

        val region = uSnap.getString("region")
        val zoneId = PointsTime.zoneIdFromRegion(region)

        // Guarda all-time con lo que tengas ahora (para no perderlo)
        val existingTotal = (uSnap.getLong("totalPoints") ?: 0L)
        val existingTasks = (uSnap.getLong("pointsTasks") ?: 0L)
        val existingSteps = (uSnap.getLong("pointsSteps") ?: 0L)
        val existingWell = (uSnap.getLong("pointsWellness") ?: 0L)

        // Calcula ventana (últimos 30 días) leyendo pointsLog
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

        // User: guarda all-time (si no existía) y setea rolling
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

    /** Puntos de hoy (según region/zone) leyendo pointsDaily */
    suspend fun getTodayPoints(uid: String): Long {
        val uSnap = userRef(uid).get().await()
        val zoneId = PointsTime.zoneIdFromRegion(uSnap.getString("region"))
        val key = PointsTime.todayKey(zoneId)
        val dSnap = dailyRef(uid, key).get().await()
        return dSnap.getLong("total") ?: 0L
    }

    /** Rolling total cacheado en el user (ya es 30 días) */
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
}