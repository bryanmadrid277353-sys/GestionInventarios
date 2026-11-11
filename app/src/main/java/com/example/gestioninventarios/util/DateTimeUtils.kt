package com.example.gestioninventarios.util

import com.example.gestioninventarios.ui.reportes.Periodo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RangoPeriodo(
    val desdeMs: Long,
    val hastaMs: Long,
    val fechaInicioLocal: LocalDate,
    val fechaFinLocal: LocalDate
)

object DateTimeUtils {

    /**
     * Devuelve el rango de tiempo exacto en milisegundos (epoch milli) y fechas locales para un período dado.
     * 'desdeMs' es inclusivo (>= desdeMs) y 'hastaMs' es exclusivo (< hastaMs).
     */
    fun rangoDe(
        periodo: Periodo,
        zona: ZoneId = ZoneId.systemDefault(),
        hoy: LocalDate = LocalDate.now(zona)
    ): RangoPeriodo {
        val fechaFinLocal = hoy
        val fechaInicioLocal = hoy.minusDays((periodo.dias - 1).toLong())

        val desdeMs = fechaInicioLocal.atStartOfDay(zona).toInstant().toEpochMilli()
        val hastaMs = hoy.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli()

        return RangoPeriodo(
            desdeMs = desdeMs,
            hastaMs = hastaMs,
            fechaInicioLocal = fechaInicioLocal,
            fechaFinLocal = fechaFinLocal
        )
    }

    /**
     * Devuelve el rango de tiempo del período previo de igual duración (ej. días inmediatamente anteriores).
     */
    fun rangoPrevioDe(
        periodo: Periodo,
        zona: ZoneId = ZoneId.systemDefault(),
        hoy: LocalDate = LocalDate.now(zona)
    ): RangoPeriodo {
        val actual = rangoDe(periodo, zona, hoy)
        val fechaFinPrevio = actual.fechaInicioLocal.minusDays(1)
        val fechaInicioPrevio = fechaFinPrevio.minusDays((periodo.dias - 1).toLong())

        val desdeMs = fechaInicioPrevio.atStartOfDay(zona).toInstant().toEpochMilli()
        val hastaMs = actual.desdeMs

        return RangoPeriodo(
            desdeMs = desdeMs,
            hastaMs = hastaMs,
            fechaInicioLocal = fechaInicioPrevio,
            fechaFinLocal = fechaFinPrevio
        )
    }

    /**
     * Parsea un string ISO de venta (ej: "2026-11-10T04:18:00") asumiendo tiempo UTC y lo convierte a milisegundos epoch.
     */
    fun parseFechaVentaToEpochMilli(fechaStr: String): Long {
        if (fechaStr.isBlank()) return 0L
        return try {
            val isoClean = if (fechaStr.endsWith("Z") || fechaStr.contains("+") || (fechaStr.length > 10 && fechaStr.substring(10).contains("-"))) {
                fechaStr
            } else {
                "${fechaStr}Z"
            }
            Instant.parse(isoClean).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Parsea un string ISO de venta en UTC y lo convierte a ZonedDateTime en la zona indicada.
     */
    fun parseFechaVentaToZonedDateTime(
        fechaStr: String,
        zona: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime {
        val ms = parseFechaVentaToEpochMilli(fechaStr)
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms), zona)
    }

    /**
     * Formatea el encabezado de fecha para el Historial de ventas:
     * - "Hoy" si es la fecha de hoy.
     * - "Ayer" si es la fecha de ayer.
     * - "d MMM yyyy" (ej. "9 nov 2026") para otras fechas (Locale es-SV).
     */
    fun formatearEncabezadoFechaHistorial(
        fechaLocal: LocalDate,
        zona: ZoneId = ZoneId.systemDefault(),
        hoy: LocalDate = LocalDate.now(zona)
    ): String {
        return when (fechaLocal) {
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> {
                val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "SV"))
                fechaLocal.format(fmt)
            }
        }
    }

    /**
     * Formatea la hora local en formato de 24 horas "HH:mm" (ej. "22:18").
     */
    fun formatearHoraLocal(
        fechaStr: String,
        zona: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = parseFechaVentaToZonedDateTime(fechaStr, zona)
        val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return zdt.format(fmt)
    }
}
