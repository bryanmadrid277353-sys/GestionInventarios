package com.example.gestioninventarios.util

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
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
        const val CARPETA_RAIZ_NOMBRE = "Inventario PYME"
        const val CARPETA_SUB_NOMBRE = "Respaldos de Datos"
        const val RUTA_RELATIVA_DOCUMENTOS = "Documents/$CARPETA_RAIZ_NOMBRE/$CARPETA_SUB_NOMBRE/"
        const val RUTA_RELATIVA_DESCARGAS = "Download/$CARPETA_RAIZ_NOMBRE/$CARPETA_SUB_NOMBRE/"

        fun generarNombreArchivo(prefijo: String = "InventarioPYME"): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT)
            val fecha = sdf.format(Date())
            return "${prefijo}_$fecha.json"
        }
    }

    /**
     * Devuelve verdadero si se cuenta con permiso total para escribir en la carpeta raíz externa.
     */
    fun tieneAccesoCompleto(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 30 -> {
                Environment.isExternalStorageManager()
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Guarda el JSON de respaldo intentando secuencialmente las 4 estrategias.
     */
    fun guardarRespaldoJson(
        jsonContenido: String,
        prefijoNombre: String = "InventarioPYME"
    ): Result<RespaldoResultado> {
        val nombreArchivo = generarNombreArchivo(prefijoNombre)

        // Estrategia 1: Carpeta Raíz con File API (Si se cuenta con acceso completo)
        if (tieneAccesoCompleto()) {
            val res1 = guardarEnCarpetaRaizDirecta(nombreArchivo, jsonContenido)
            if (res1.isSuccess) {
                return res1
            }
        }

        // Estrategia 2: MediaStore.Files en "Documents/Inventario PYME/Respaldos de Datos/"
        val res2 = guardarEnMediaStore(
            nombreArchivo = nombreArchivo,
            jsonContenido = jsonContenido,
            relativePath = RUTA_RELATIVA_DOCUMENTOS,
            etiquetaRuta = "Documentos > $CARPETA_RAIZ_NOMBRE > $CARPETA_SUB_NOMBRE > $nombreArchivo",
            estrategiaClave = "MEDIA_STORE_DOCUMENTS"
        )
        if (res2.isSuccess) {
            return res2
        }

        // Estrategia 3: MediaStore.Downloads en "Download/Inventario PYME/Respaldos de Datos/"
        val res3 = guardarEnMediaStore(
            nombreArchivo = nombreArchivo,
            jsonContenido = jsonContenido,
            relativePath = RUTA_RELATIVA_DESCARGAS,
            etiquetaRuta = "Descargas > $CARPETA_RAIZ_NOMBRE > $CARPETA_SUB_NOMBRE > $nombreArchivo",
            estrategiaClave = "MEDIA_STORE_DOWNLOADS"
        )
        if (res3.isSuccess) {
            return res3
        }

        // Estrategia 4 (Garantizada): Almacenamiento privado de la app (getExternalFilesDir)
        return guardarEnAlmacenamientoPrivadoApp(nombreArchivo, jsonContenido)
    }

    private fun guardarEnCarpetaRaizDirecta(
        nombreArchivo: String,
        jsonContenido: String
    ): Result<RespaldoResultado> {
        return try {
            val dirRaiz = Environment.getExternalStorageDirectory()
            val carpetaDestino = File(dirRaiz, "$CARPETA_RAIZ_NOMBRE/$CARPETA_SUB_NOMBRE")
            if (!carpetaDestino.exists()) {
                val ok = carpetaDestino.mkdirs()
                if (!ok && !carpetaDestino.exists()) {
                    return Result.failure(IllegalStateException("No se pudo crear la carpeta raíz en $CARPETA_RAIZ_NOMBRE"))
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

                val rutaLegible = "Almacenamiento principal > $CARPETA_RAIZ_NOMBRE > $CARPETA_SUB_NOMBRE > $nombreArchivo"
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

    private fun guardarEnMediaStore(
        nombreArchivo: String,
        jsonContenido: String,
        relativePath: String,
        etiquetaRuta: String,
        estrategiaClave: String
    ): Result<RespaldoResultado> {
        if (Build.VERSION.SDK_INT < 29) {
            return Result.failure(IllegalStateException("MediaStore Scoped Storage requiere API 29+"))
        }

        val resolver = context.contentResolver

        // Selección rigurosa de colección según el relativePath
        val collection = if (relativePath.startsWith("Download/")) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        var uriResult: Uri? = null
        return try {
            uriResult = resolver.insert(collection, values)
                ?: return Result.failure(IllegalStateException("No se pudo crear la entrada en MediaStore"))

            resolver.openOutputStream(uriResult)?.use { out ->
                out.write(jsonContenido.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: run {
                resolver.delete(uriResult, null, null)
                return Result.failure(IllegalStateException("No se pudo abrir el stream de salida"))
            }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uriResult, values, null, null)

            Result.success(
                RespaldoResultado(
                    nombreArchivo = nombreArchivo,
                    rutaLegible = etiquetaRuta,
                    uriStr = uriResult.toString(),
                    estrategiaUsada = estrategiaClave,
                    accesoCompleto = false
                )
            )
        } catch (e: Exception) {
            uriResult?.let { uri ->
                try {
                    resolver.delete(uri, null, null)
                } catch (_: Exception) {}
            }
            Result.failure(e)
        }
    }

    private fun guardarEnAlmacenamientoPrivadoApp(
        nombreArchivo: String,
        jsonContenido: String
    ): Result<RespaldoResultado> {
        return try {
            val dirPrivado = context.getExternalFilesDir(CARPETA_SUB_NOMBRE)
                ?: context.filesDir

            if (!dirPrivado.exists()) {
                dirPrivado.mkdirs()
            }

            val archivoFinal = File(dirPrivado, nombreArchivo)
            FileOutputStream(archivoFinal).use { out ->
                out.write(jsonContenido.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            val rutaLegible = "Carpeta privada de la app > $CARPETA_SUB_NOMBRE > $nombreArchivo"
            Result.success(
                RespaldoResultado(
                    nombreArchivo = nombreArchivo,
                    rutaLegible = rutaLegible,
                    uriStr = archivoFinal.absolutePath,
                    estrategiaUsada = "APP_PRIVATE_EXTERNAL",
                    accesoCompleto = false
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
