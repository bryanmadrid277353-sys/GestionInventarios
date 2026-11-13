package com.example.gestioninventarios.util

import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RespaldoJsonManagerTest {

    @Test
    fun hacerNombreUnico_sinColision_devuelveMismoNombre() {
        val existentes = setOf("abarrotes", "limpieza")
        val resultado = RespaldoJsonManager.hacerNombreUnico("Bebidas", existentes)
        assertEquals("Bebidas", resultado)
    }

    @Test
    fun hacerNombreUnico_conColisionesConsecutivas_agregaSufijoNumerado() {
        val existentes = setOf("bebidas", "bebidas (importado)", "bebidas (importado) 2")
        val resultado = RespaldoJsonManager.hacerNombreUnico("Bebidas", existentes)
        assertEquals("Bebidas (importado) 3", resultado)
    }

    @Test
    fun hacerCodigoBarrasUnico_conColision_agregaSufijoI() {
        val existentes = setOf("2010100000013", "2010100000013-I1")
        val resultado = RespaldoJsonManager.hacerCodigoBarrasUnico("2010100000013", existentes)
        assertEquals("2010100000013-I2", resultado)
    }

    @Test
    fun obtenerSiguienteCodigoInterno_buscaSiguienteLibre() {
        val existentes = setOf(1, 2, 3)
        val resultado = RespaldoJsonManager.obtenerSiguienteCodigoInterno(existentes)
        assertEquals(4, resultado)
    }

    @Test
    fun validarJsonImportacion_appIncorrecta_falla() {
        val json = """
            {
              "app": "OtraApp",
              "formato": 2,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 0, "proveedores": 0, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "productos": [], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("no es un respaldo de InventarioPyME") == true)
    }

    @Test
    fun validarJsonImportacion_versionMasReciente_falla() {
        val json = """
            {
              "app": "InventarioPYME",
              "formato": 99,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 0, "proveedores": 0, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "productos": [], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("no soportada") == true)
    }

    @Test
    fun validarJsonImportacion_formato1ConUsuarios_correcto() {
        val json = """
            {
              "app": "InventarioPYME",
              "formato": 1,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 0, "proveedores": 0, "usuarios": 1, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "usuarios": [{"id": 1, "email": "test@test.com"}], "productos": [], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isSuccess)

        val info = res.getOrThrow()
        assertEquals(1, info.formato)
        assertEquals(1, info.cantUsuarios)
    }

    @Test
    fun validarJsonImportacion_formato2SinUsuarios_correcto() {
        val json = """
            {
              "app": "InventarioPYME",
              "formato": 2,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 1, "proveedores": 1, "productos": 1, "ventas": 0, "detalle_venta": 0 },
              "categorias": [{"id": 1}], "proveedores": [{"id": 1}], "productos": [{"id": 1}], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isSuccess)

        val info = res.getOrThrow()
        assertEquals(2, info.formato)
        assertEquals(0, info.cantUsuarios)
        assertEquals(1, info.cantProductos)
    }
}
