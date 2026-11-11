package com.example.gestioninventarios.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

enum class EstadoPermisoAcceso {
    CONCEDIDO,
    FALTA
}

object EstadoAccesoArchivos {

    fun evaluarEstado(context: Context): EstadoPermisoAcceso {
        val concedido = when {
            Build.VERSION.SDK_INT >= 30 -> {
                Environment.isExternalStorageManager()
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        return if (concedido) EstadoPermisoAcceso.CONCEDIDO else EstadoPermisoAcceso.FALTA
    }
}
