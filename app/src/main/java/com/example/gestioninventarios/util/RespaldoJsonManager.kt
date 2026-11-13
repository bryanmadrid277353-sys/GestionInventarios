package com.example.gestioninventarios.util

import android.util.JsonReader
import android.util.JsonWriter
import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter

data class RespaldoValidacionInfo(
    val formato: Int,
    val exportadoEn: Long,
    val cantCategorias: Int,
    val cantProveedores: Int,
    val cantUsuarios: Int = 0,
    val cantProductos: Int,
    val cantVentas: Int,
    val cantDetalleVenta: Int
)

data class RespaldoDatosExportar(
    val categorias: List<CategoriaEntity>,
    val proveedores: List<ProveedorEntity>,
    val productos: List<ProductoEntity>,
    val ventas: List<VentaEntity>,
    val detallesVenta: List<DetalleVentaEntity>,
    val mapaUsuariosEmail: Map<Int, String> = emptyMap(),
    val mapaUsuariosNombre: Map<Int, String> = emptyMap()
)

data class RespaldoResultadoImportacion(
    val categoriasImportadas: Int,
    val categoriasRenombradas: Int,
    val proveedoresImportados: Int,
    val proveedoresRenombrados: Int,
    val usuariosImportados: Int = 0,
    val usuariosRenombrados: Int = 0,
    val productosImportados: Int,
    val productosRenombrados: Int,
    val ventasImportadas: Int,
    val detallesImportados: Int,
    val rutaRespaldoPrevio: String = "",
    val rutaRespaldoPosterior: String = ""
)

object RespaldoJsonManager {

    const val APP_IDENTIFIER = "InventarioPYME"
    const val FORMATO_VERSION = 2
    const val VERSION_BD = 3
    const val TAMANO_MAX_BYTES = 50 * 1024 * 1024 // 50 MB

    fun exportarAStream(datos: RespaldoDatosExportar, outputStream: OutputStream) {
        val writer = JsonWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
        writer.setIndent("  ")

        writer.beginObject()
        writer.name("app").value(APP_IDENTIFIER)
        writer.name("formato").value(FORMATO_VERSION)
        writer.name("versionBD").value(VERSION_BD)
        writer.name("exportadoEn").value(System.currentTimeMillis())

        // Header conteos
        writer.name("conteos").beginObject()
        writer.name("categorias").value(datos.categorias.size.toLong())
        writer.name("proveedores").value(datos.proveedores.size.toLong())
        writer.name("productos").value(datos.productos.size.toLong())
        writer.name("ventas").value(datos.ventas.size.toLong())
        writer.name("detalle_venta").value(datos.detallesVenta.size.toLong())
        writer.endObject()

        // Categorías
        writer.name("categorias").beginArray()
        for (cat in datos.categorias) {
            writer.beginObject()
            writer.name("id").value(cat.id.toLong())
            writer.name("nombre").value(cat.nombre)
            if (cat.descripcion != null) writer.name("descripcion").value(cat.descripcion) else writer.name("descripcion").nullValue()
            writer.name("activo").value(cat.activo)
            writer.name("codigo").value(cat.codigo.toLong())
            writer.endObject()
        }
        writer.endArray()

        // Proveedores
        writer.name("proveedores").beginArray()
        for (prov in datos.proveedores) {
            writer.beginObject()
            writer.name("id").value(prov.id.toLong())
            writer.name("nombre").value(prov.nombre)
            if (prov.contacto != null) writer.name("contacto").value(prov.contacto) else writer.name("contacto").nullValue()
            if (prov.telefono != null) writer.name("telefono").value(prov.telefono) else writer.name("telefono").nullValue()
            if (prov.email != null) writer.name("email").value(prov.email) else writer.name("email").nullValue()
            writer.name("activo").value(prov.activo)
            writer.name("codigo").value(prov.codigo.toLong())
            writer.endObject()
        }
        writer.endArray()

        // Productos
        writer.name("productos").beginArray()
        for (prod in datos.productos) {
            writer.beginObject()
            writer.name("id").value(prod.id.toLong())
            writer.name("codigoBarras").value(prod.codigoBarras)
            writer.name("nombre").value(prod.nombre)
            writer.name("precio").value(prod.precio)
            writer.name("costo").value(prod.costo)
            writer.name("stock").value(prod.stock.toLong())
            writer.name("stockMinimo").value(prod.stockMinimo.toLong())
            writer.name("categoriaId").value(prod.categoriaId.toLong())
            writer.name("proveedorId").value(prod.proveedorId.toLong())
            writer.name("activo").value(prod.activo)
            if (prod.categoriaNombre != null) writer.name("categoriaNombre").value(prod.categoriaNombre) else writer.name("categoriaNombre").nullValue()
            if (prod.proveedorNombre != null) writer.name("proveedorNombre").value(prod.proveedorNombre) else writer.name("proveedorNombre").nullValue()
            writer.endObject()
        }
        writer.endArray()

        // Ventas
        writer.name("ventas").beginArray()
        for (vta in datos.ventas) {
            writer.beginObject()
            writer.name("id").value(vta.id.toLong())
            writer.name("usuarioId").value(vta.usuarioId.toLong())
            writer.name("usuarioNombre").value(vta.usuarioNombre)
            val correo = datos.mapaUsuariosEmail[vta.usuarioId] ?: ""
            val nomComp = datos.mapaUsuariosNombre[vta.usuarioId] ?: vta.usuarioNombre
            writer.name("vendedorCorreo").value(correo)
            writer.name("vendedorNombre").value(nomComp)
            writer.name("fechaVenta").value(vta.fechaVenta)
            writer.name("total").value(vta.total)
            writer.endObject()
        }
        writer.endArray()

        // Detalle Venta
        writer.name("detalle_venta").beginArray()
        for (det in datos.detallesVenta) {
            writer.beginObject()
            writer.name("id").value(det.id.toLong())
            writer.name("ventaId").value(det.ventaId.toLong())
            writer.name("productoId").value(det.productoId.toLong())
            writer.name("productoNombre").value(det.productoNombre)
            writer.name("codigoBarras").value(det.codigoBarras)
            writer.name("cantidad").value(det.cantidad.toLong())
            writer.name("precioUnitario").value(det.precioUnitario)
            writer.name("subtotal").value(det.subtotal)
            writer.endObject()
        }
        writer.endArray()

        writer.endObject()
        writer.flush()
    }

    fun verificarArchivoTemporal(
        fileTmp: File,
        dbCounts: Map<String, Int>
    ): Result<RespaldoValidacionInfo> {
        return try {
            val reader = JsonReader(BufferedReader(InputStreamReader(FileInputStream(fileTmp), Charsets.UTF_8)))
            var app = ""
            var formato = 0
            var exportadoEn = 0L

            var cantCatHeader = -1
            var cantProvHeader = -1
            var cantUsrHeader = -1
            var cantProdHeader = -1
            var cantVtaHeader = -1
            var cantDetHeader = -1

            var cantCatArray = 0
            var cantProvArray = 0
            var cantUsrArray = 0
            var cantProdArray = 0
            var cantVtaArray = 0
            var cantDetArray = 0

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "app" -> app = reader.nextString()
                    "formato" -> formato = reader.nextInt()
                    "versionBD" -> reader.nextInt()
                    "exportadoEn" -> exportadoEn = reader.nextLong()
                    "conteos" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "categorias" -> cantCatHeader = reader.nextInt()
                                "proveedores" -> cantProvHeader = reader.nextInt()
                                "usuarios" -> cantUsrHeader = reader.nextInt()
                                "productos" -> cantProdHeader = reader.nextInt()
                                "ventas" -> cantVtaHeader = reader.nextInt()
                                "detalle_venta" -> cantDetHeader = reader.nextInt()
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    "categorias" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantCatArray++
                        }
                        reader.endArray()
                    }
                    "proveedores" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantProvArray++
                        }
                        reader.endArray()
                    }
                    "usuarios" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantUsrArray++
                        }
                        reader.endArray()
                    }
                    "productos" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantProdArray++
                        }
                        reader.endArray()
                    }
                    "ventas" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantVtaArray++
                        }
                        reader.endArray()
                    }
                    "detalle_venta" -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.skipValue()
                            cantDetArray++
                        }
                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            reader.close()

            if (app != APP_IDENTIFIER) {
                return Result.failure(IllegalStateException("Identificador de aplicación inválido en el respaldo"))
            }
            if (formato > FORMATO_VERSION || formato < 1) {
                return Result.failure(IllegalStateException("Formato de versión ($formato) no soportado"))
            }

            // Verificar conteos
            val expectedCat = dbCounts["categorias"] ?: 0
            val expectedProv = dbCounts["proveedores"] ?: 0
            val expectedProd = dbCounts["productos"] ?: 0
            val expectedVta = dbCounts["ventas"] ?: 0
            val expectedDet = dbCounts["detalle_venta"] ?: 0

            if (cantCatHeader != expectedCat || cantCatArray != expectedCat) {
                return Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Categorías: esperadas $expectedCat, en archivo $cantCatArray)"))
            }
            if (cantProvHeader != expectedProv || cantProvArray != expectedProv) {
                return Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Proveedores: esperados $expectedProv, en archivo $cantProvArray)"))
            }
            if (cantProdHeader != expectedProd || cantProdArray != expectedProd) {
                return Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Productos: esperados $expectedProd, en archivo $cantProdArray)"))
            }
            if (cantVtaHeader != expectedVta || cantVtaArray != expectedVta) {
                return Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Ventas: esperadas $expectedVta, en archivo $cantVtaArray)"))
            }
            if (cantDetHeader != expectedDet || cantDetArray != expectedDet) {
                return Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Detalles de Venta: esperados $expectedDet, en archivo $cantDetArray)"))
            }

            Result.success(
                RespaldoValidacionInfo(
                    formato = formato,
                    exportadoEn = exportadoEn,
                    cantCategorias = cantCatArray,
                    cantProveedores = cantProvArray,
                    cantUsuarios = cantUsrArray,
                    cantProductos = cantProdArray,
                    cantVentas = cantVtaArray,
                    cantDetalleVenta = cantDetArray
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException("El respaldo no quedó completo y no se guardó (Excepción: ${e.localizedMessage})", e))
        }
    }

    fun validarJsonImportacion(jsonStr: String): Result<RespaldoValidacionInfo> {
        return try {
            val bytesCount = jsonStr.toByteArray(Charsets.UTF_8).size
            if (bytesCount > TAMANO_MAX_BYTES) {
                return Result.failure(IllegalArgumentException("El archivo supera el tamaño máximo permitido (50 MB)"))
            }

            val root = JSONObject(jsonStr)

            val app = root.optString("app", "")
            if (app != APP_IDENTIFIER) {
                return Result.failure(IllegalArgumentException("El archivo no es un respaldo de InventarioPyME"))
            }

            val formato = root.optInt("formato", -1)
            if (formato > FORMATO_VERSION || formato < 1) {
                return Result.failure(IllegalArgumentException("Este respaldo es de una versión ($formato) no soportada por esta app"))
            }

            val exportadoEn = root.optLong("exportadoEn", 0L)

            val conteos = root.optJSONObject("conteos")
                ?: return Result.failure(IllegalArgumentException("El archivo no incluye el resumen de conteos"))

            val catArray = root.optJSONArray("categorias") ?: JSONArray()
            val provArray = root.optJSONArray("proveedores") ?: JSONArray()
            val usrArray = root.optJSONArray("usuarios") ?: JSONArray()
            val prodArray = root.optJSONArray("productos") ?: JSONArray()
            val vtaArray = root.optJSONArray("ventas") ?: JSONArray()
            val detArray = root.optJSONArray("detalle_venta") ?: JSONArray()

            val usrCountHeader = if (formato >= 2) usrArray.length() else conteos.optInt("usuarios", -1)

            if (conteos.optInt("categorias", -1) != catArray.length() ||
                conteos.optInt("proveedores", -1) != provArray.length() ||
                usrCountHeader != usrArray.length() ||
                conteos.optInt("productos", -1) != prodArray.length() ||
                conteos.optInt("ventas", -1) != vtaArray.length() ||
                conteos.optInt("detalle_venta", -1) != detArray.length()
            ) {
                return Result.failure(IllegalArgumentException("El archivo está corrupto o incompleto (conteos no coinciden)"))
            }

            Result.success(
                RespaldoValidacionInfo(
                    formato = formato,
                    exportadoEn = exportadoEn,
                    cantCategorias = catArray.length(),
                    cantProveedores = provArray.length(),
                    cantUsuarios = usrArray.length(),
                    cantProductos = prodArray.length(),
                    cantVentas = vtaArray.length(),
                    cantDetalleVenta = detArray.length()
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Formato JSON no válido o corrupto: ${e.localizedMessage}", e))
        }
    }

    // ---------- Funciones auxiliares para unicidad (hacerUnico) ----------

    fun hacerNombreUnico(
        baseNombre: String,
        existentes: Set<String>,
        sufijo: String = " (importado)"
    ): String {
        if (!existentes.contains(baseNombre.lowercase())) {
            return baseNombre
        }
        var i = 1
        while (true) {
            val candidato = if (i == 1) "$baseNombre$sufijo" else "$baseNombre$sufijo $i"
            if (!existentes.contains(candidato.lowercase())) {
                return candidato
            }
            i++
        }
    }

    fun hacerCodigoBarrasUnico(
        baseCodigo: String,
        existentes: Set<String>
    ): String {
        if (!existentes.contains(baseCodigo)) {
            return baseCodigo
        }
        var i = 1
        while (true) {
            val candidato = "$baseCodigo-I$i"
            if (!existentes.contains(candidato)) {
                return candidato
            }
            i++
        }
    }

    fun obtenerSiguienteCodigoInterno(existentes: Set<Int>): Int {
        for (code in 1..99) {
            if (!existentes.contains(code)) {
                return code
            }
        }
        throw IllegalStateException("No hay códigos internos disponibles (máximo 99)")
    }
}
