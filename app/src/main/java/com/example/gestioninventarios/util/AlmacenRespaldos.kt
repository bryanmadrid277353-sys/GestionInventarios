package com.example.gestioninventarios.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RespaldoResultado(
    val nombreArchivo: String,
    val rutaLegible: String,
    val uriStr: String? = null,
    val estrategiaUsada: String = "CARPETA_RAIZ",
    val accesoCompleto: Boolean = true
)

class AlmacenRespaldos(private val context: Context) {

    companion object {
        const val CARPETA_APP = "Inventario PYME"
        const val CARPETA_RESPALDOS = "Respaldos de Datos"
        const val CARPETA_PREVIO_IMPORTAR = "Previo a Importar"

        fun generarNombreArchivo(prefijo: String = "InventarioPYME", marcaTiempo: String? = null): String {
            val fecha = marcaTiempo ?: SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(Date())
            return "${prefijo}_$fecha.json"
        }
    }

    /**
     * Devuelve verdadero si el acceso a todos los archivos está CONCEDIDO.
     */
    fun tieneAccesoCompleto(): Boolean {
        return EstadoAccesoArchivos.evaluarEstado(context) == EstadoPermisoAcceso.CONCEDIDO
    }

    /**
     * Garantiza que la carpeta de respaldos (y subcarpetas) exista en la raíz del almacenamiento.
     */
    fun asegurarCarpetaExiste(subcarpeta: String? = null): Boolean {
        if (!tieneAccesoCompleto()) return false
        return try {
            val dirRaiz = Environment.getExternalStorageDirectory()
            val carpetaBase = File(dirRaiz, "$CARPETA_APP/$CARPETA_RESPALDOS")
            val carpetaDestino = if (!subcarpeta.isNullOrEmpty()) {
                File(carpetaBase, subcarpeta)
            } else {
                carpetaBase
            }
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdirs()
            } else true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Guarda el JSON de respaldo ÚNICAMENTE en la carpeta raíz del dispositivo si el permiso está CONCEDIDO.
     */
    fun guardarRespaldoJson(
        jsonContenido: String,
        prefijoNombre: String = "InventarioPYME",
        subcarpeta: String? = null,
        marcaTiempo: String? = null
    ): Result<RespaldoResultado> {
        if (!tieneAccesoCompleto()) {
            return Result.failure(IllegalStateException("No se puede crear el respaldo porque falta el permiso de acceso a archivos del dispositivo."))
        }

        val nombreArchivo = generarNombreArchivo(prefijoNombre, marcaTiempo)

        return try {
            val dirRaiz = Environment.getExternalStorageDirectory()
            val carpetaBase = File(dirRaiz, "$CARPETA_APP/$CARPETA_RESPALDOS")
            val carpetaDestino = if (!subcarpeta.isNullOrEmpty()) {
                File(carpetaBase, subcarpeta)
            } else {
                carpetaBase
            }

            if (!carpetaDestino.exists()) {
                val ok = carpetaDestino.mkdirs()
                if (!ok && !carpetaDestino.exists()) {
                    return Result.failure(IllegalStateException("No se pudo crear la carpeta de respaldos en el almacenamiento del teléfono."))
                }
            }

            val archivoTemp = File(carpetaDestino, "$nombreArchivo.tmp")
            val archivoFinal = File(carpetaDestino, nombreArchivo)

            try {
                FileOutputStream(archivoTemp).use { out ->
                    out.write(jsonContenido.toByteArray(Charsets.UTF_8))
                    out.flush()
                }

                if (archivoFinal.exists()) {
                    archivoFinal.delete()
                }

                val renamOk = archivoTemp.renameTo(archivoFinal)
                if (!renamOk) {
                    archivoTemp.copyTo(archivoFinal, overwrite = true)
                    archivoTemp.delete()
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(archivoFinal.absolutePath),
                    arrayOf("application/json"),
                    null
                )

                val subRutaTexto = if (!subcarpeta.isNullOrEmpty()) " > $subcarpeta" else ""
                val rutaLegible = "Almacenamiento principal > $CARPETA_APP > $CARPETA_RESPALDOS$subRutaTexto > $nombreArchivo"

                Result.success(
                    RespaldoResultado(
                        nombreArchivo = nombreArchivo,
                        rutaLegible = rutaLegible,
                        uriStr = archivoFinal.absolutePath,
                        estrategiaUsada = "CARPETA_RAIZ",
                        accesoCompleto = true
                    )
                )
            } catch (e: Exception) {
                if (archivoTemp.exists()) {
                    archivoTemp.delete()
                }
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
