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
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Launcher para seleccionar archivo JSON
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val jsonStr = inputStream?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText() }
                if (!jsonStr.isNullOrEmpty()) {
                    viewModel.procesarArchivoSeleccionado(jsonStr)
                }
            } catch (e: Exception) {
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

    // Mostrar Snackbar cuando cambia de FALTA a CONCEDIDO
    LaunchedEffect(uiState.mostrarSnackbarAccesoConcedido) {
        if (uiState.mostrarSnackbarAccesoConcedido) {
            snackbarHostState.showSnackbar("Acceso a archivos concedido")
            viewModel.consumirSnackbarAccesoConcedido()
        }
    }

    fun solicitarConcederAcceso() {
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
            } catch (e: ActivityNotFoundException) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            } catch (e: Exception) {
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
        onDismissDialogoFaltaAccesoImportar = viewModel::dismissDialogoFaltaAccesoImportar,
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
    onDismissDialogoFaltaAccesoImportar: () -> Unit,
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
                                imageVector = Icons.Default.Backup,
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
                            text = "Exporta toda la base de datos (productos, categorías, proveedores, ventas y usuarios) a un archivo JSON seguro.",
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
                            onClick = onExportar,
                            enabled = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null)
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
                                imageVector = Icons.Default.Publish,
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
                            onClick = onSolicitarImportar,
                            enabled = !uiState.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (uiState.estadoAcceso == EstadoPermisoAcceso.FALTA) Icons.Default.Lock else Icons.Default.Publish,
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
                                imageVector = Icons.Default.Warning,
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
                            text = "Elimina permanentemente todos los productos, categorías, proveedores, ventas y usuarios (manteniendo tu cuenta activa).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onSolicitarEliminar,
                            enabled = !uiState.cargando,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
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
        AlertDialog(
            onDismissRequest = onDismissExitoExportacion,
            title = { Text("Respaldo Creado Exitosamente", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("El archivo se guardó correctamente en:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.rutaLegible,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!res.accesoCompleto) {
                        Text(
                            text = "Nota: Se guardó en la carpeta de Documentos. Concede el acceso a archivos si deseas guardar directo en la carpeta principal 'Inventario PYME'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (res.estrategiaUsada == "APP_PRIVATE_EXTERNAL") {
                        Text(
                            text = "Aviso: Se guardó en la carpeta privada de la app. Este archivo se eliminará si desinstalas la aplicación.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "Este archivo contiene datos del negocio y usuarios con contraseñas cifradas. Guárdalo en un lugar seguro.",
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

    // C) Diálogo de Falta Acceso para Importar
    if (uiState.mostrarDialogoFaltaAccesoImportar) {
        AlertDialog(
            onDismissRequest = onDismissDialogoFaltaAccesoImportar,
            title = { Text("Falta el acceso a archivos", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Para importar un respaldo necesitas activar el acceso a archivos del dispositivo. Así se evita importar por error desde ubicaciones no controladas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = {
                    onDismissDialogoFaltaAccesoImportar()
                    onSolicitarConcederAcceso()
                }) {
                    Text("Conceder acceso")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissDialogoFaltaAccesoImportar) {
                    Text("Cancelar")
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
                Text(
                    text = "Los registros del archivo se agregarán como NUEVOS, por encima de los existentes. No se modificará ni se eliminará ningún dato actual. Si el archivo contiene datos que ya tienes, verás registros repetidos (las ventas repetidas se sumarán dos veces en los reportes). Los usuarios importados llegan desactivados. Antes de importar se guardará automáticamente un respaldo de tus datos actuales. Consejo: para restaurar en una instalación limpia, usa primero 'Eliminar todos los datos'.",
                    style = MaterialTheme.typography.bodyMedium
                )
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
                        text = "Se importarán ${info.cantCategorias} categorías, ${info.cantProveedores} proveedores, ${info.cantProductos} productos, ${info.cantVentas} ventas y ${info.cantUsuarios} usuarios.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Respaldo realizado el: $fechaStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Categorías: ${res.categoriasImportadas} (${res.categoriasRenombradas} renombradas)")
                    Text("• Proveedores: ${res.proveedoresImportados} (${res.proveedoresRenombrados} renombrados)")
                    Text("• Productos: ${res.productosImportados} (${res.productosRenombrados} renombrados)")
                    Text("• Ventas: ${res.ventasImportadas} (${res.detallesImportados} detalles)")
                    Text("• Usuarios: ${res.usuariosImportados} (desactivados por seguridad)")
                }
            },
            confirmButton = {
                Button(onClick = onDismissResultadoImportacion) {
                    Text("Aceptar")
                }
            }
        )
    }

    // G) Diálogo Confirmar Eliminación con Temporizador de 5 Segundos
    if (uiState.mostrarConfirmarEliminar) {
        var contador by remember { mutableIntStateOf(5) }

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
                        text = "Se eliminarán permanentemente todos los productos, categorías, proveedores, ventas e historial, y todos los usuarios excepto tu cuenta de administrador actual.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta acción no se puede deshacer. Antes se guardará un respaldo automático. Los archivos de la carpeta Respaldos de Datos no se borran.",
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
    estadoAcceso: EstadoPermisoAcceso,
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
                    "Sin este permiso la app no puede guardar en la carpeta principal ni importar respaldos. Exportar sí funciona: el archivo se guarda en Documentos › Inventario PYME › Respaldos de Datos."
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
