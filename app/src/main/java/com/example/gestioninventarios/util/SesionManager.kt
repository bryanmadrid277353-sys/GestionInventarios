package com.example.gestioninventarios.util

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.gestioninventarios.data.local.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SesionManager(
    private val context: Context,
    private val tokenStore: TokenStore
) : DefaultLifecycleObserver {

    companion object {
        private const val PREFS_NAME = "sesion_inactividad_prefs"
        private const val KEY_ULTIMA_ACTIVIDAD = "ultima_actividad"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _expirationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val expirationEvent: SharedFlow<String> = _expirationEvent.asSharedFlow()

    fun attachToLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun registrarActividad() {
        prefs.edit().putLong(KEY_ULTIMA_ACTIVIDAD, System.currentTimeMillis()).commit()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        verificarExpiracion()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        registrarActividad()
    }

    fun verificarExpiracion() {
        val isLoggedIn = runBlocking { tokenStore.isLoggedIn.first() }
        if (!isLoggedIn) return

        val ahora = System.currentTimeMillis()
        val ultimaActividad = prefs.getLong(KEY_ULTIMA_ACTIVIDAD, 0L)

        if (ultimaActividad > 0L) {
            val diff = ahora - ultimaActividad
            if (diff > SesionConfig.TIEMPO_SESION_INACTIVA_MS || diff < 0L) {
                cerrarSesion("Tu sesión expiró por inactividad. Inicia sesión nuevamente.")
            } else {
                registrarActividad()
            }
        } else {
            registrarActividad()
        }
    }

    fun cerrarSesion(motivo: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            tokenStore.clearSession()
            prefs.edit().clear().commit()
            _expirationEvent.emit(motivo ?: "Sesión cerrada")
        }
    }
}
