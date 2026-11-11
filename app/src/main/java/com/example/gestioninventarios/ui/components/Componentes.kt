package com.example.gestioninventarios.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gestioninventarios.ui.theme.Espacio
import com.example.gestioninventarios.ui.theme.Semantico
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

enum class TipoStatus {
    Exito, Advertencia, Error, Info, Neutro
}

@Composable
fun StatusChip(
    texto: String,
    tipo: TipoStatus = TipoStatus.Info,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val s = Semantico.colores
    val c = MaterialTheme.colorScheme
    val (fondo, textoColor) = when (tipo) {
        TipoStatus.Exito -> s.exitoContenedor to s.onExitoContenedor
        TipoStatus.Advertencia -> s.advertenciaContenedor to s.onAdvertenciaContenedor
        TipoStatus.Error -> c.errorContainer to c.onErrorContainer
        TipoStatus.Info -> c.primaryContainer to c.onPrimaryContainer
        TipoStatus.Neutro -> c.surfaceContainerHigh to c.onSurfaceVariant
    }

    Surface(
        color = fondo,
        contentColor = textoColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StockBadge(stock: Int, minimo: Int, modifier: Modifier = Modifier) {
    val s = Semantico.colores
    val c = MaterialTheme.colorScheme
    val (fondo, texto, etiqueta) = when {
        stock <= 0 -> Triple(c.errorContainer, c.onErrorContainer, "Agotado")
        stock <= minimo -> Triple(s.advertenciaContenedor, s.onAdvertenciaContenedor, "Stock bajo: $stock")
        else -> Triple(s.exitoContenedor, s.onExitoContenedor, "Stock: $stock")
    }
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = etiqueta,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class TipoKpi {
    Info, Exito, Advertencia, Error, Primario
}

@Composable
fun KpiCard(
    icono: ImageVector,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    tipo: TipoKpi = TipoKpi.Info,
    onClick: (() -> Unit)? = null
) {
    val s = Semantico.colores
    val c = MaterialTheme.colorScheme
    val (containerColor, contentColor, iconBg) = when (tipo) {
        TipoKpi.Info -> Triple(c.surfaceContainerHigh, c.onSurface, c.primaryContainer)
        TipoKpi.Exito -> Triple(s.exitoContenedor, s.onExitoContenedor, s.exito.copy(alpha = 0.25f))
        TipoKpi.Advertencia -> Triple(s.advertenciaContenedor, s.onAdvertenciaContenedor, s.advertencia.copy(alpha = 0.25f))
        TipoKpi.Error -> Triple(c.errorContainer, c.onErrorContainer, c.error.copy(alpha = 0.25f))
        TipoKpi.Primario -> Triple(c.primaryContainer, c.onPrimaryContainer, c.primary.copy(alpha = 0.25f))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = iconBg,
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    textoConfirmar: String = "Confirmar",
    textoCancelar: String = "Cancelar",
    destructivo: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = if (destructivo) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(textoConfirmar)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(textoCancelar)
            }
        }
    )
}

@Composable
fun BuscadorCampo(
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    enabled: Boolean = true,
    onImeDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(
                    onClick = { onValorChange("") },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (onImeDone != null) ImeAction.Done else ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeDone?.invoke() },
            onSearch = { onImeDone?.invoke() }
        ),
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EsqueletoCarga(
    modifier: Modifier = Modifier,
    filas: Int = 4
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Espacio.m),
        verticalArrangement = Arrangement.spacedBy(Espacio.m)
    ) {
        repeat(filas) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            ) {}
        }
    }
}

@Composable
fun AvatarIniciales(
    nombre: String,
    modifier: Modifier = Modifier,
    colorFondo: Color? = null,
    tamano: Dp = 40.dp
) {
    val iniciales = remember(nombre) {
        nombre.trim()
            .split("\\s+".toRegex())
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    val fondo = colorFondo ?: MaterialTheme.colorScheme.primaryContainer
    val textoColor = MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        color = fondo,
        contentColor = textoColor,
        shape = CircleShape,
        modifier = modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iniciales,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatoMoneda(monto: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV"))
    return formato.format(monto)
}

fun formatoMonedaCompacta(monto: Double): String {
    return if (monto >= 1000.0) {
        String.format(Locale.US, "$%.1fk", monto / 1000.0)
    } else {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV"))
        formato.format(monto)
    }
}

@Composable
fun rememberAppSnackbarHostState(): SnackbarHostState {
    return remember { SnackbarHostState() }
}

suspend fun SnackbarHostState.mostrarMensaje(
    mensaje: String,
    accionDeshacer: (() -> Unit)? = null,
    textoAccion: String = "Deshacer",
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    val result = showSnackbar(
        message = mensaje,
        actionLabel = if (accionDeshacer != null) textoAccion else null,
        duration = duration
    )
    if (result == SnackbarResult.ActionPerformed) {
        accionDeshacer?.invoke()
    }
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        accion?.let {
            Spacer(Modifier.height(16.dp))
            it()
        }
    }
}
