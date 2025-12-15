package com.example.daypilot.firebaseLogic.pointsLogic

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
@RequiresApi(Build.VERSION_CODES.O)
internal object PointsTime {

    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE


    fun zoneIdFromRegion(region: String?): ZoneId {
        val clean = region?.trim().orEmpty()
        return try {
            if (clean.isNotBlank()) ZoneId.of(clean) else ZoneId.systemDefault()
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    fun todayKey(zoneId: ZoneId): String {
        val date = Instant.now().atZone(zoneId).toLocalDate()
        return date.format(ISO)
    }

    fun parseKey(key: String): LocalDate = LocalDate.parse(key, ISO)

    fun keyFromInstant(instant: Instant, zoneId: ZoneId): String =
        instant.atZone(zoneId).toLocalDate().format(ISO)

    /** Ventana rolling: incluye hoy y los 29 anteriores => 30 días */
    fun outKey(zoneId: ZoneId): String {
        val today = Instant.now().atZone(zoneId).toLocalDate()
        return today.minusDays(30).format(ISO) // este día queda fuera de la ventana
    }
}