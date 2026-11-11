package com.example.gestioninventarios.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ---------- Paleta de marca (parte del azul petróleo que ya usa la app) ----------
private val ColoresOscuro = darkColorScheme(
    primary = Color(0xFF7DD3FC), onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004D6D), onPrimaryContainer = Color(0xFFC7EBFF),
    secondary = Color(0xFF86D9CB), onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF1F4F48), onSecondaryContainer = Color(0xFFA2F2E4),
    tertiary = Color(0xFFFBBF24), onTertiary = Color(0xFF3F2E00),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1519), onBackground = Color(0xFFE1E8EC),
    surface = Color(0xFF0D1519), onSurface = Color(0xFFE1E8EC),
    surfaceVariant = Color(0xFF263238), onSurfaceVariant = Color(0xFFBCC8CF),
    surfaceContainer = Color(0xFF182126), surfaceContainerHigh = Color(0xFF222C31),
    outline = Color(0xFF6F7C83), outlineVariant = Color(0xFF3A464C)
)

private val ColoresClaro = lightColorScheme(
    primary = Color(0xFF00658B), onPrimary = Color.White,
    primaryContainer = Color(0xFFC7EBFF), onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF006B5E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFA2F2E4), onSecondaryContainer = Color(0xFF00201B),
    tertiary = Color(0xFF7A5900), onTertiary = Color.White,
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5FAFC), onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF5FAFC), onSurface = Color(0xFF171C1F),
    surfaceVariant = Color(0xFFDCE4E9), onSurfaceVariant = Color(0xFF40484C),
    surfaceContainer = Color(0xFFE9EFF2), surfaceContainerHigh = Color(0xFFE3E9EC),
    outline = Color(0xFF70787D), outlineVariant = Color(0xFFC0C8CD)
)

// ---------- Colores semánticos: éxito / advertencia (stock normal, bajo, etc.) ----------
@Immutable
data class ColoresSemanticos(
    val exito: Color, val exitoContenedor: Color, val onExitoContenedor: Color,
    val advertencia: Color, val advertenciaContenedor: Color, val onAdvertenciaContenedor: Color
)

private val SemanticosOscuro = ColoresSemanticos(
    exito = Color(0xFF6EE7B7), exitoContenedor = Color(0xFF0F3D2E), onExitoContenedor = Color(0xFFB7F5DC),
    advertencia = Color(0xFFFBBF24), advertenciaContenedor = Color(0xFF4A3600), onAdvertenciaContenedor = Color(0xFFFFE08A)
)
private val SemanticosClaro = ColoresSemanticos(
    exito = Color(0xFF047857), exitoContenedor = Color(0xFFCDF5E3), onExitoContenedor = Color(0xFF00281B),
    advertencia = Color(0xFFB45309), advertenciaContenedor = Color(0xFFFFE9B8), onAdvertenciaContenedor = Color(0xFF3F2E00)
)

private val LocalSemanticos = staticCompositionLocalOf { SemanticosOscuro }

/** Uso: Semantico.colores.exito */
object Semantico {
    val colores: ColoresSemanticos @Composable get() = LocalSemanticos.current
}

// ---------- Formas y espaciado coherentes en toda la app ----------
private val Formas = Shapes(
    small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(28.dp)
)

object Espacio { val xs = 4.dp; val s = 8.dp; val m = 16.dp; val l = 24.dp; val xl = 32.dp }

@Composable
fun InventarioTheme(oscuro: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSemanticos provides if (oscuro) SemanticosOscuro else SemanticosClaro) {
        MaterialTheme(
            colorScheme = if (oscuro) ColoresOscuro else ColoresClaro,
            shapes = Formas,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun GestionInventariosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    InventarioTheme(oscuro = darkTheme, content = content)
}
