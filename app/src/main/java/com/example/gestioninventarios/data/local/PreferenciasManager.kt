package com.example.gestioninventarios.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenciasManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "preferencias_inventario_pyme"
        private const val KEY_MODO_TEMA = "modo_tema"
        private const val KEY_DATOS_SEMBRADOS = "datos_sembrados"
        private const val KEY_ULTIMO_RESPALDO_FECHA = "ultimo_respaldo_fecha"
        private const val KEY_ULTIMO_RESPALDO_UBICACION = "ultimo_respaldo_ubicacion"

        const val TEMA_AUTOMATICO = "AUTOMATICO"
        const val TEMA_CLARO = "CLARO"
        const val TEMA_OSCURO = "OSCURO"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _modoTema = MutableStateFlow(getModoTema())
    val modoTema: StateFlow<String> = _modoTema.asStateFlow()

    fun getModoTema(): String {
        return prefs.getString(KEY_MODO_TEMA, TEMA_AUTOMATICO) ?: TEMA_AUTOMATICO
    }

    fun setModoTema(modo: String) {
        prefs.edit().putString(KEY_MODO_TEMA, modo).apply()
        _modoTema.value = modo
    }

    fun isDatosSembrados(): Boolean {
        return prefs.getBoolean(KEY_DATOS_SEMBRADOS, false)
    }

    fun setDatosSembrados(sembrados: Boolean) {
        prefs.edit().putBoolean(KEY_DATOS_SEMBRADOS, sembrados).apply()
    }

    fun getUltimoRespaldoFecha(): String {
        return prefs.getString(KEY_ULTIMO_RESPALDO_FECHA, "") ?: ""
    }

    fun getUltimoRespaldoUbicacion(): String {
        return prefs.getString(KEY_ULTIMO_RESPALDO_UBICACION, "") ?: ""
    }

    fun guardarUltimoRespaldo(fecha: String, ubicacion: String) {
        prefs.edit()
            .putString(KEY_ULTIMO_RESPALDO_FECHA, fecha)
            .putString(KEY_ULTIMO_RESPALDO_UBICACION, ubicacion)
            .apply()
    }
}
