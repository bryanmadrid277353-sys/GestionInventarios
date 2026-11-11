package com.example.gestioninventarios.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodigoInternoTest {

    @Test
    fun testGenerarCodigoInternoValoresEspecificos() {
        assertEquals("2010100000013", CodigoInterno.generar(1, 1, 1))
        assertEquals("2010200000012", CodigoInterno.generar(1, 2, 1))
        assertEquals("2030100000127", CodigoInterno.generar(3, 1, 12))
        assertEquals("2000000000015", CodigoInterno.generar(0, 0, 1))
    }

    @Test
    fun testEsEan13Valido() {
        assertTrue(CodigoInterno.esEan13Valido("4006381333931"))
        assertFalse(CodigoInterno.esEan13Valido("4006381333932"))
    }

    @Test
    fun testConsecutivosIncrementales() {
        val prod1 = CodigoInterno.generar(catCodigo = 1, provCodigo = 1, consecutivo = 1)
        val prod2 = CodigoInterno.generar(catCodigo = 1, provCodigo = 1, consecutivo = 2)

        assertEquals("2010100000013", prod1)
        assertEquals("2010100000020", prod2)
    }

    @Test
    fun testFormatear() {
        assertEquals("2 01 01 0000001 3", CodigoInterno.formatear("2010100000013"))
    }
}
