package com.example.gestioninventarios.util

import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.local.entity.UsuarioEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun hacerEmailUnico_conColision_agregaImp() {
        val existentes = setOf("admin@inventariopyme.com", "admin+imp1@inventariopyme.com")
        val resultado = RespaldoJsonManager.hacerEmailUnico("admin@inventariopyme.com", existentes)
        assertEquals("admin+imp2@inventariopyme.com", resultado)
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
              "formato": 1,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 0, "proveedores": 0, "usuarios": 0, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "usuarios": [], "productos": [], "ventas": [], "detalle_venta": []
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
              "conteos": { "categorias": 0, "proveedores": 0, "usuarios": 0, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "usuarios": [], "productos": [], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("versión más reciente") == true)
    }

    @Test
    fun validarJsonImportacion_conteosNoCoinciden_falla() {
        val json = """
            {
              "app": "InventarioPYME",
              "formato": 1,
              "versionBD": 3,
              "exportadoEn": 1000,
              "conteos": { "categorias": 5, "proveedores": 0, "usuarios": 0, "productos": 0, "ventas": 0, "detalle_venta": 0 },
              "categorias": [], "proveedores": [], "usuarios": [], "productos": [], "ventas": [], "detalle_venta": []
            }
        """.trimIndent()

        val res = RespaldoJsonManager.validarJsonImportacion(json)
        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("conteos no coinciden") == true)
    }

    @Test
    fun exportarYValidarJson_correcto() {
        val datos = RespaldoDatosExportar(
            categorias = listOf(CategoriaEntity(1, "Bebidas", "Refrescos", true, 1)),
            proveedores = listOf(ProveedorEntity(1, "Proveedor 1", "Contacto", "555", "p1@test.com", true, 1)),
            usuarios = listOf(UsuarioEntity(1, "Admin", "admin@test.com", "hash", "ADMIN", true)),
            productos = listOf(ProductoEntity(1, "123456", "Prod 1", 1.0, 0.5, 10, 2, 1, 1, true, "Bebidas", "Proveedor 1")),
            ventas = emptyList(),
            detallesVenta = emptyList()
        )

        val jsonExportado = RespaldoJsonManager.exportarAJson(datos)
        assertNotNull(jsonExportado)

        val validacion = RespaldoJsonManager.validarJsonImportacion(jsonExportado)
        assertTrue(validacion.isSuccess)

        val info = validacion.getOrThrow()
        assertEquals(1, info.cantCategorias)
        assertEquals(1, info.cantProveedores)
        assertEquals(1, info.cantUsuarios)
        assertEquals(1, info.cantProductos)
        assertEquals(0, info.cantVentas)
        assertEquals(0, info.cantDetalleVenta)
    }
}
