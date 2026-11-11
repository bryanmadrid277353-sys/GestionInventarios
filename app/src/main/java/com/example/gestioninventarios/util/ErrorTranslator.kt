package com.example.gestioninventarios.util

data class ErrorHumano(
    val mensajeSimple: String,
    val detalleTecnico: String
)

object ErrorTranslator {

    fun traducir(e: Throwable): ErrorHumano {
        val msgRaw = e.localizedMessage ?: e.message ?: e.toString()

        val mensajeSimple = when {
            msgRaw.contains("Primary directory", ignoreCase = true) ||
            msgRaw.contains("not allowed for content://", ignoreCase = true) -> {
                "No se pudo guardar en la carpeta seleccionada del dispositivo debido a restricciones de almacenamiento."
            }
            msgRaw.contains("Permission denied", ignoreCase = true) ||
            msgRaw.contains("permisos de escritura", ignoreCase = true) -> {
                "No se cuentan con los permisos necesarios para acceder al almacenamiento del dispositivo."
            }
            msgRaw.contains("ENOSPC", ignoreCase = true) ||
            msgRaw.contains("No space left", ignoreCase = true) -> {
                "El dispositivo no tiene suficiente espacio libre de almacenamiento."
            }
            msgRaw.contains("no es un respaldo", ignoreCase = true) -> {
                "El archivo seleccionado no corresponde a un respaldo válido de Inventario PyME."
            }
            msgRaw.contains("versión más reciente", ignoreCase = true) -> {
                "El respaldo fue creado con una versión más reciente de la aplicación."
            }
            msgRaw.contains("conteos no coinciden", ignoreCase = true) ||
            msgRaw.contains("corrupto", ignoreCase = true) -> {
                "El archivo de respaldo está incompleto o dañado."
            }
            msgRaw.contains("máximo 99", ignoreCase = true) -> {
                "No hay códigos internos de categoría/proveedor disponibles en el rango 1-99."
            }
            else -> {
                "Ocurrió un inconveniente al procesar los archivos de respaldo."
            }
        }

        return ErrorHumano(
            mensajeSimple = mensajeSimple,
            detalleTecnico = "${e.javaClass.simpleName}: $msgRaw"
        )
    }
}
