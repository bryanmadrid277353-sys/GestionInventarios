package com.example.gestioninventarios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gestioninventarios.data.local.PreferenciasManager
import com.example.gestioninventarios.ui.navigation.NavGraph
import com.example.gestioninventarios.ui.theme.InventarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as GestionInventariosApp).container
        val modoTemaInicial = appContainer.preferenciasManager.getModoTema()

        setContent {
            val modoTemaState by appContainer.preferenciasManager.modoTema.collectAsState(initial = modoTemaInicial)

            val isSystemInDark = isSystemInDarkTheme()
            val useDarkTheme = when (modoTemaState) {
                PreferenciasManager.TEMA_CLARO -> false
                PreferenciasManager.TEMA_OSCURO -> true
                else -> isSystemInDark
            }

            DisposableEffect(useDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { useDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { useDarkTheme }
                )
                onDispose {}
            }

            InventarioTheme(oscuro = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }
}
