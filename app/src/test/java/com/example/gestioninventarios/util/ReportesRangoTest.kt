package com.example.gestioninventarios.util

import com.example.gestioninventarios.ui.reportes.Periodo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReportesRangoTest {

    @Test
    fun testReportesRangoZonaHorariaElSalvador() {
        val zona = ZoneId.of("America/El_Salvador")
        val hoy = LocalDate.of(2026, 11, 9)

        val rangoSemana = DateTimeUtils.rangoDe(Periodo.SEMANA, zona, hoy)

        // Venta 1: 9 nov a las 22:18 locales (en UTC es 10 nov a las 04:18)
        val venta2218IsoUtc = "2026-11-10T04:18:00Z"
        val ms2218 = DateTimeUtils.parseFechaVentaToEpochMilli(venta2218IsoUtc)

        // Venta 2: 9 nov a las 23:59 locales (en UTC es 10 nov a las 05:59)
        val venta2359IsoUtc = "2026-11-10T05:59:00Z"
        val ms2359 = DateTimeUtils.parseFechaVentaToEpochMilli(venta2359IsoUtc)

        // Venta 3: 10 nov a las 00:01 locales (en UTC es 10 nov a las 06:01)
        val venta0001IsoUtc = "2026-11-10T06:01:00Z"
        val ms0001 = DateTimeUtils.parseFechaVentaToEpochMilli(venta0001IsoUtc)

        // Verificaciones
        assertTrue("La venta de las 22:18 del 9 nov debe estar dentro del rango de Semana",
            ms2218 >= rangoSemana.desdeMs && ms2218 < rangoSemana.hastaMs)

        assertTrue("La venta de las 23:59 del 9 nov debe estar dentro del rango de Semana",
            ms2359 >= rangoSemana.desdeMs && ms2359 < rangoSemana.hastaMs)

        assertFalse("La venta de las 00:01 del 10 nov debe quedar fuera del rango de Semana cuando hoy = 9 nov",
            ms0001 >= rangoSemana.desdeMs && ms0001 < rangoSemana.hastaMs)
    }
}
