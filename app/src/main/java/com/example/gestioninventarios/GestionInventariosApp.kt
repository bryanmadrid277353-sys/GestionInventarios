package com.example.gestioninventarios

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GestionInventariosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.sesionManager.attachToLifecycle()
        CoroutineScope(Dispatchers.IO).launch {
            container.authRepository.cleanupLegacyData()
        }
    }
}
