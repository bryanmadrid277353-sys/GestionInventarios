package com.example.gestioninventarios.util

import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.local.entity.UsuarioEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import org.json.JSONArray
import org.json.JSONObject

data class RespaldoValidacionInfo(
    val exportadoEn: Long,
    val cantCategorias: Int,
    val cantProveedores: Int,
    val cantUsuarios: Int,
    val cantProductos: Int,
    val cantVentas: Int,
    val cantDetalleVenta: Int
)

data class RespaldoDatosExportar(
    val categorias: List<CategoriaEntity>,
    val proveedores: List<ProveedorEntity>,
    val usuarios: List<UsuarioEntity>,
    val productos: List<ProductoEntity>,
    val ventas: List<VentaEntity>,
    val detallesVenta: List<DetalleVentaEntity>
)

data class RespaldoResultadoImportacion(
    val categoriasImportadas: Int,
    val categoriasRenombradas: Int,
    val proveedoresImportados: Int,
    val proveedoresRenombrados: Int,
    val usuariosImportados: Int,
    val usuariosRenombrados: Int,
    val productosImportados: Int,
    val productosRenombrados: Int,
    val ventasImportadas: Int,
    val detallesImportados: Int,
    val rutaRespaldoPrevio: String = "",
    val rutaRespaldoPosterior: String = ""
)

object RespaldoJsonManager {

    const val APP_IDENTIFIER = "InventarioPYME"
    const val FORMATO_VERSION = 1
    const val VERSION_BD = 3
    const val TAMANO_MAX_BYTES = 50 * 1024 * 1024 // 50 MB

    fun exportarAJson(datos: RespaldoDatosExportar): String {
        val root = JSONObject()
        root.put("app", APP_IDENTIFIER)
        root.put("formato", FORMATO_VERSION)
        root.put("versionBD", VERSION_BD)
        root.put("exportadoEn", System.currentTimeMillis())

        val conteos = JSONObject()
        conteos.put("categorias", datos.categorias.size)
        conteos.put("proveedores", datos.proveedores.size)
        conteos.put("usuarios", datos.usuarios.size)
        conteos.put("productos", datos.productos.size)
        conteos.put("ventas", datos.ventas.size)
        conteos.put("detalle_venta", datos.detallesVenta.size)
        root.put("conteos", conteos)

        // Categorias
        val arrayCat = JSONArray()
        for (cat in datos.categorias) {
            val item = JSONObject()
            item.put("id", cat.id)
            item.put("nombre", cat.nombre)
            item.put("descripcion", cat.descripcion ?: JSONObject.NULL)
            item.put("activo", cat.activo)
            item.put("codigo", cat.codigo)
            arrayCat.put(item)
        }
        root.put("categorias", arrayCat)

        // Proveedores
        val arrayProv = JSONArray()
        for (prov in datos.proveedores) {
            val item = JSONObject()
            item.put("id", prov.id)
            item.put("nombre", prov.nombre)
            item.put("contacto", prov.contacto ?: JSONObject.NULL)
            item.put("telefono", prov.telefono ?: JSONObject.NULL)
            item.put("email", prov.email ?: JSONObject.NULL)
            item.put("activo", prov.activo)
            item.put("codigo", prov.codigo)
            arrayProv.put(item)
        }
        root.put("proveedores", arrayProv)

        // Usuarios
        val arrayUsr = JSONArray()
        for (usr in datos.usuarios) {
            val item = JSONObject()
            item.put("id", usr.id)
            item.put("nombre", usr.nombre)
            item.put("email", usr.email)
            item.put("passwordHash", usr.passwordHash)
            item.put("rol", usr.rol)
            item.put("activo", usr.activo)
            arrayUsr.put(item)
        }
        root.put("usuarios", arrayUsr)

        // Productos
        val arrayProd = JSONArray()
        for (prod in datos.productos) {
            val item = JSONObject()
            item.put("id", prod.id)
            item.put("codigoBarras", prod.codigoBarras)
            item.put("nombre", prod.nombre)
            item.put("precio", prod.precio)
            item.put("costo", prod.costo)
            item.put("stock", prod.stock)
            item.put("stockMinimo", prod.stockMinimo)
            item.put("categoriaId", prod.categoriaId)
            item.put("proveedorId", prod.proveedorId)
            item.put("activo", prod.activo)
            item.put("categoriaNombre", prod.categoriaNombre ?: JSONObject.NULL)
            item.put("proveedorNombre", prod.proveedorNombre ?: JSONObject.NULL)
            arrayProd.put(item)
        }
        root.put("productos", arrayProd)

        // Ventas
        val arrayVta = JSONArray()
        for (vta in datos.ventas) {
            val item = JSONObject()
            item.put("id", vta.id)
            item.put("usuarioId", vta.usuarioId)
            item.put("usuarioNombre", vta.usuarioNombre)
            item.put("fechaVenta", vta.fechaVenta)
            item.put("total", vta.total)
            arrayVta.put(item)
        }
        root.put("ventas", arrayVta)

        // Detalle Venta
        val arrayDet = JSONArray()
        for (det in datos.detallesVenta) {
            val item = JSONObject()
            item.put("id", det.id)
            item.put("ventaId", det.ventaId)
            item.put("productoId", det.productoId)
            item.put("productoNombre", det.productoNombre)
            item.put("codigoBarras", det.codigoBarras)
            item.put("cantidad", det.cantidad)
            item.put("precioUnitario", det.precioUnitario)
            item.put("subtotal", det.subtotal)
            arrayDet.put(item)
        }
        root.put("detalle_venta", arrayDet)

        return root.toString(2)
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
            if (formato > FORMATO_VERSION) {
                return Result.failure(IllegalArgumentException("Este respaldo es de una versión más reciente de la app"))
            }
            if (formato < 1) {
                return Result.failure(IllegalArgumentException("Formato de archivo no válido"))
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

            if (conteos.optInt("categorias", -1) != catArray.length() ||
                conteos.optInt("proveedores", -1) != provArray.length() ||
                conteos.optInt("usuarios", -1) != usrArray.length() ||
                conteos.optInt("productos", -1) != prodArray.length() ||
                conteos.optInt("ventas", -1) != vtaArray.length() ||
                conteos.optInt("detalle_venta", -1) != detArray.length()
            ) {
                return Result.failure(IllegalArgumentException("El archivo está corrupto o incompleto (conteos no coinciden)"))
            }

            Result.success(
                RespaldoValidacionInfo(
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

    fun hacerEmailUnico(
        baseEmail: String,
        existentes: Set<String>
    ): String {
        if (!existentes.contains(baseEmail.lowercase())) {
            return baseEmail
        }
        val partes = baseEmail.split("@")
        val local = partes.getOrElse(0) { "usuario" }
        val dominio = partes.getOrElse(1) { "inventariopyme.com" }
        var i = 1
        while (true) {
            val candidato = "${local}+imp$i@$dominio"
            if (!existentes.contains(candidato.lowercase())) {
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
