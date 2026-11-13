package com.example.gestioninventarios.ui.ajustes

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.theme.Semantico
import com.example.gestioninventarios.util.EstadoPermisoAcceso
import com.example.gestioninventarios.util.OpenDocumentInBackupFolderContract
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val SEGUNDOS_CONFIRMACION_ELIMINAR = 3

@Composable
fun AjustesAvanzadosScreen(
    viewModel: AjustesAvanzadosViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarPasosPermisoDialog by remember { mutableStateOf(false) }

    // Launcher para permisos runtime en Android <= 29
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.reevaluarEstadoAlmacenamiento(context)
    }

    // Launcher personalizado que abre directamente la carpeta de respaldos a partir de API 26
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = OpenDocumentInBackupFolderContract()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonStr = inputStream?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText() }
                if (!jsonStr.isNullOrEmpty()) {
                    viewModel.procesarArchivoSeleccionado(jsonStr)
                }
            } catch (_: Exception) {
                // Manejado en ViewModel
            }
        }
    }

    // Reevaluar estado en ON_RESUME
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.reevaluarEstadoAlmacenamiento(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Escuchar evento de Snackbar único cuando se concede el permiso
    LaunchedEffect(Unit) {
        viewModel.eventoSnackbarAccesoConcedido.collect {
            snackbarHostState.showSnackbar("Acceso a archivos concedido")
        }
    }

    fun solicitarConcederAcceso() {
        viewModel.marcarEsperandoRetornoDeAjustes()
        if (Build.VERSION.SDK_INT >= 30) {
            mostrarPasosPermisoDialog = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun ejecutarConcederAccesoProceso() {
        mostrarPasosPermisoDialog = false
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    AjustesAvanzadosScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSolicitarConcederAcceso = ::solicitarConcederAcceso,
        onExportar = viewModel::exportarRespaldo,
        onSolicitarImportar = viewModel::solicitarImportacion,
        onConfirmarAbrirDocumento = {
            viewModel.dismissAvisoImportar()
            openDocumentLauncher.launch(arrayOf("*/*"))
        },
        onDismissAvisoImportar = viewModel::dismissAvisoImportar,
        onConfirmarImportar = viewModel::confirmarEjecutarImportacion,
        onDismissResumenImportar = viewModel::dismissResumenImportacion,
        onSolicitarEliminar = viewModel::solicitarEliminacion,
        onConfirmarEliminar = viewModel::ejecutarEliminacionTotal,
        onDismissConfirmarEliminar = viewModel::dismissConfirmarEliminar,
        onDismissExitoExportacion = viewModel::dismissExitoExportacionDialog,
        onDismissResultadoImportacion = viewModel::dismissResultadoImportacion,
        onDismissResultadoEliminacion = viewModel::dismissResultadoEliminacion,
        onDismissError = viewModel::dismissErrorDialog,
        onNavigateBack = onNavigateBack
    )

    // Diálogo con los 4 pasos explicativos antes de abrir Configuración del sistema
    if (mostrarPasosPermisoDialog) {
        AlertDialog(
            onDismissRequest = { mostrarPasosPermisoDialog = false },
            title = { Text("Conceder acceso a archivos", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Sigue estos sencillos pasos en la pantalla que se abrirá:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("1. Se abrirá la configuración del sistema.")
                    Text("2. Busca 'Inventario PyME'.")
                    Text("3. Activa 'Permitir acceso para administrar todos los archivos'.")
                    Text("4. Vuelve a la app.")
                }
            },
            confirmButton = {
                Button(onClick = ::ejecutarConcederAccesoProceso) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarPasosPermisoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AjustesAvanzadosScreenContent(
    uiState: AjustesAvanzadosUiState,
    snackbarHostState: SnackbarHostState,
    onSolicitarConcederAcceso: () -> Unit,
    onExportar: () -> Unit,
    onSolicitarImportar: () -> Unit,
    onConfirmarAbrirDocumento: () -> Unit,
    onDismissAvisoImportar: () -> Unit,
    onConfirmarImportar: () -> Unit,
    onDismissResumenImportar: () -> Unit,
    onSolicitarEliminar: () -> Unit,
    onConfirmarEliminar: () -> Unit,
    onDismissConfirmarEliminar: () -> Unit,
    onDismissExitoExportacion: () -> Unit,
    onDismissResultadoImportacion: () -> Unit,
    onDismissResultadoEliminacion: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val esConcedido = (uiState.estadoAcceso == EstadoPermisoAcceso.CONCEDIDO)

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Ajustes Avanzados",
                onBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta de Estado del Permiso de Archivos
            item {
                TarjetaEstadoAccesoArchivos(
                    estadoAcceso = uiState.estadoAcceso,
                    onConcederAcceso = onSolicitarConcederAcceso
                )
            }

            // 2. Exportación de Respaldos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.Backup else Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Respaldos de Datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exporta toda la base de datos (productos, categorías, proveedores y ventas) a un archivo JSON seguro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.ultimoRespaldoFecha.isNotEmpty()) {
                            Text(
                                text = "Último respaldo: ${uiState.ultimoRespaldoFecha}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ubicación: ${uiState.ultimoRespaldoUbicacion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = if (esConcedido) onExportar else onSolicitarConcederAcceso,
                            enabled = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.Backup else Icons.Default.Lock,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exportar respaldo ahora")
                        }
                    }
                }
            }

            // 3. Importación de Respaldos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.Publish else Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Importar Respaldo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega registros desde un archivo JSON previamente exportado. Todos los datos ingresan como registros NUEVOS sin borrar nada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = if (esConcedido) onSolicitarImportar else onSolicitarConcederAcceso,
                            enabled = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.Publish else Icons.Default.Lock,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importar respaldo...")
                        }
                    }
                }
            }

            // 4. Zona de Peligro (Eliminar todos los datos)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.Warning else Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Zona de peligro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Elimina permanentemente todos los productos, categorías, proveedores y ventas (conservando los usuarios).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = if (esConcedido) onSolicitarEliminar else onSolicitarConcederAcceso,
                            enabled = !uiState.cargando,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (esConcedido) Icons.Default.DeleteForever else Icons.Default.Lock,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar todos los datos")
                        }
                    }
                }
            }
        }
    }

    // ================= DIÁLOGOS Y ESTADOS =================

    // A) Diálogo de Carga / Progreso
    if (uiState.cargando && uiState.mensajeFaseProgreso != null) {
        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("Procesando...", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(uiState.mensajeFaseProgreso ?: "Por favor espera...")
                }
            },
            confirmButton = {}
        )
    }

    // B) Diálogo Éxito Exportación
    uiState.mensajeExitoExportacion?.let { res ->
        val counts = uiState.conteosUltimaExportacion
        AlertDialog(
            onDismissRequest = onDismissExitoExportacion,
            title = { Text("Respaldo Creado Exitosamente", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (counts != null) {
                        Text(
                            text = "Se exportaron: ${counts["productos"] ?: 0} productos, ${counts["categorias"] ?: 0} categorías, ${counts["proveedores"] ?: 0} proveedores, ${counts["ventas"] ?: 0} ventas y ${counts["detalle_venta"] ?: 0} detalles de venta.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("El archivo se guardó correctamente en:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = res.rutaLegible,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Los usuarios no forman parte de los respaldos: no se exportan ni se importan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissExitoExportacion) {
                    Text("Aceptar")
                }
            }
        )
    }

    // D) Diálogo Aviso Previo a Seleccionar Archivo
    if (uiState.mostrarAvisoImportar) {
        AlertDialog(
            onDismissRequest = onDismissAvisoImportar,
            title = { Text("Importante antes de importar", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Los registros del archivo se agregarán como NUEVOS, por encima de los existentes. No se modificará ni se eliminará ningún dato actual. Si el archivo contiene datos que ya tienes, verás registros repetidos (las ventas repetidas se sumarán dos veces en los reportes). Los usuarios no forman parte de los respaldos: no se exportan ni se importan.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se guardarán dos copias automáticas en 'Respaldos de Datos › Otros' (una del estado previo y otra del posterior).",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Consejo: para restaurar en una instalación limpia, usa primero 'Eliminar todos los datos'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirmarAbrirDocumento) {
                    Text("Elegir archivo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissAvisoImportar) {
                    Text("Cancelar")
                }
            }
        )
    }

    // E) Diálogo Resumen Previo a Confirmar Importación
    uiState.respaldoValidado?.let { info ->
        val fechaStr = if (info.exportadoEn > 0) {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(Date(info.exportadoEn))
        } else "Desconocida"

        AlertDialog(
            onDismissRequest = onDismissResumenImportar,
            title = { Text("Confirmar Importación", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Se importarán ${info.cantCategorias} categorías, ${info.cantProveedores} proveedores, ${info.cantProductos} productos y ${info.cantVentas} ventas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (info.formato == 1 && info.cantUsuarios > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Este respaldo (formato 1) incluye ${info.cantUsuarios} usuarios; no se importarán.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Los usuarios no forman parte de los respaldos: no se exportan ni se importan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Respaldo realizado el: $fechaStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se crearán respaldos automático previo y posterior en 'Respaldos de Datos › Otros'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = onConfirmarImportar) {
                    Text("Importar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissResumenImportar) {
                    Text("Cancelar")
                }
            }
        )
    }

    // F) Diálogo Resultado Importación
    uiState.resultadoImportacion?.let { res ->
        AlertDialog(
            onDismissRequest = onDismissResultadoImportacion,
            title = { Text("Importación Completada", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Se agregaron exitosamente los siguientes registros:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Categorías: ${res.categoriasImportadas} (${res.categoriasRenombradas} renombradas)")
                    Text("• Proveedores: ${res.proveedoresImportados} (${res.proveedoresRenombrados} renombrados)")
                    Text("• Productos: ${res.productosImportados} (${res.productosRenombrados} renombrados)")
                    Text("• Ventas: ${res.ventasImportadas} (${res.detallesImportados} detalles)")
                    Spacer(modifier = Modifier.height(10.dp))
                    if (res.rutaRespaldoPrevio.isNotEmpty()) {
                        Text("Respaldo previo: ${res.rutaRespaldoPrevio}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    if (res.rutaRespaldoPosterior.isNotEmpty()) {
                        Text("Respaldo posterior: ${res.rutaRespaldoPosterior}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismissResultadoImportacion) {
                    Text("Aceptar")
                }
            }
        )
    }

    // G) Diálogo Confirmar Eliminación con Temporizador de 3 Segundos
    if (uiState.mostrarConfirmarEliminar) {
        var contador by remember { mutableIntStateOf(SEGUNDOS_CONFIRMACION_ELIMINAR) }

        LaunchedEffect(Unit) {
            while (contador > 0) {
                delay(1000)
                contador--
            }
        }

        AlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("¿Eliminar todos los datos?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Column {
                    Text(
                        text = "Se eliminarán productos, categorías, proveedores y ventas. Los usuarios no se eliminan.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta acción no se puede deshacer. Antes se guardará una copia automática en Respaldos de Datos › Otros. Los archivos de la carpeta Respaldos de Datos no se borran.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmarEliminar,
                    enabled = (contador == 0),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        if (contador > 0) "Eliminar ($contador)" else "Eliminar definitivamente"
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissConfirmarEliminar) {
                    Text("Cancelar")
                }
            }
        )
    }

    // H) Diálogo Resultado Eliminación Total
    uiState.resultadoEliminacion?.let { msg ->
        AlertDialog(
            onDismissRequest = onDismissResultadoEliminacion,
            title = { Text("Eliminación Completada", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = onDismissResultadoEliminacion) {
                    Text("Aceptar")
                }
            }
        )
    }

    // I) Diálogo Error en Español Simple con Detalles Técnicos Desplegables
    uiState.errorHumano?.let { err ->
        var mostrarDetalle by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Atención", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = err.mensajeSimple, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { mostrarDetalle = !mostrarDetalle }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (mostrarDetalle) "Ocultar detalles técnicos" else "Ver detalles técnicos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (mostrarDetalle) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    AnimatedVisibility(visible = mostrarDetalle) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = err.detalleTecnico,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismissError) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun TarjetaEstadoAccesoArchivos(
    estadoAcceso: EstadoPermisoAcceso?,
    onConcederAcceso: () -> Unit
) {
    val esConcedido = (estadoAcceso == EstadoPermisoAcceso.CONCEDIDO)
    val semantico = Semantico.colores

    val containerColor = if (esConcedido) semantico.exitoContenedor else semantico.advertenciaContenedor
    val contentColor = if (esConcedido) semantico.onExitoContenedor else semantico.onAdvertenciaContenedor

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (esConcedido) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (esConcedido) "Acceso a archivos activado" else "Falta permiso de acceso a archivos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (esConcedido) {
                    "Los respaldos se guardan en Inventario PYME › Respaldos de Datos, en la carpeta principal del teléfono."
                } else {
                    "Sin este permiso no se pueden crear respaldos ni importar o eliminar datos. Actívalo para proteger tu información."
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.9f)
            )

            if (!esConcedido) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConcederAcceso,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Conceder acceso", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
