package com.example.gestioninventarios.ui.venta

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestioninventarios.data.model.ProductoDto
import com.example.gestioninventarios.data.model.VentaResponseDto
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.BuscadorCampo
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.EstadoVacio
import com.example.gestioninventarios.ui.components.StockBadge
import com.example.gestioninventarios.ui.components.formatoMoneda
import com.example.gestioninventarios.ui.theme.GestionInventariosTheme
import com.example.gestioninventarios.util.DateTimeUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    viewModel: VentaViewModel,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToProductos: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is VentaEvento.ProductoQuitado -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = "Se quitó '${evento.item.producto.nombre}'",
                        actionLabel = "Deshacer",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restaurarItem(evento.item, evento.posicion)
                    }
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Punto de Venta",
                onBack = onNavigateBack
            )
        },
        bottomBar = {
            if (uiState.selectedTab == 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL A COBRAR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = formatoMoneda(uiState.total),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = viewModel::openCobroBottomSheet,
                            enabled = uiState.cart.isNotEmpty() && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("COBRAR (${formatoMoneda(uiState.total)})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Nueva Venta", style = MaterialTheme.typography.titleSmall) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Historial de Ventas", style = MaterialTheme.typography.titleSmall) }
                )
            }

            if (uiState.selectedTab == 0) {
                PosTabContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onNavigateToScanner = onNavigateToScanner
                )
            } else {
                HistorialTabContent(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }

    if (uiState.showCobroBottomSheet) {
        CobroBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = viewModel::closeCobroBottomSheet
        )
    }

    if (uiState.successSale != null) {
        ComprobanteVentaDialog(
            venta = uiState.successSale!!,
            cambio = uiState.successSaleCambio,
            onDismiss = viewModel::dismissSuccessDialog
        )
    }

    if (uiState.selectedVentaDetail != null) {
        DetalleVentaModal(
            venta = uiState.selectedVentaDetail!!,
            onDismiss = viewModel::closeVentaDetailModal
        )
    }

    if (uiState.unknownBarcodeScanned != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissUnknownBarcodeDialog,
            title = { Text("Producto no encontrado", fontWeight = FontWeight.Bold) },
            text = { Text("El código de barras '${uiState.unknownBarcodeScanned}' no corresponde a ningún producto en el catálogo.") },
            confirmButton = {
                if (uiState.userRole == "ADMIN") {
                    Button(onClick = {
                        viewModel.dismissUnknownBarcodeDialog()
                        onNavigateToProductos()
                    }) {
                        Text("Ir a registrar producto")
                    }
                } else {
                    Button(onClick = viewModel::dismissUnknownBarcodeDialog) {
                        Text("Entendido")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::dismissUnknownBarcodeDialog) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (uiState.showClearCartDialog) {
        ConfirmDialog(
            titulo = "Vaciar Carrito",
            mensaje = "¿Estás seguro de que deseas vaciar todos los productos del carrito?",
            textoConfirmar = "Vaciar",
            destructivo = true,
            onConfirmar = viewModel::confirmClearCart,
            onDismiss = viewModel::dismissClearCartDialog
        )
    }
}

@Composable
fun PosTabContent(
    uiState: VentaUiState,
    viewModel: VentaViewModel,
    onNavigateToScanner: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "Ocurrió un error en la venta.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BuscadorCampo(
                    valor = uiState.searchQuery,
                    onValorChange = viewModel::onSearchQueryChanged,
                    placeholder = "Nombre o código de barras...",
                    onImeDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )

                if (uiState.sugerencias.isNotEmpty()) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = viewModel::clearSearch,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        uiState.sugerencias.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.nombre, fontWeight = FontWeight.Bold)
                                            Text("Cód: ${prod.codigoBarras} · Stock: ${prod.stock}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(formatoMoneda(prod.precio), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.selectProductoDeSugerencia(prod)
                                }
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onNavigateToScanner,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear código",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalItems = uiState.cart.sumOf { it.cantidad }
            Text(
                text = "Carrito ($totalItems ${if (totalItems == 1) "producto" else "productos"})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (uiState.cart.isNotEmpty()) {
                TextButton(onClick = viewModel::promptClearCart) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vaciar", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.cart.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Default.ShoppingCart,
                    titulo = "El carrito está vacío",
                    mensaje = "Escanea un código de barras o busca un producto en la barra superior para iniciar la venta."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.cart,
                        key = { it.producto.id }
                    ) { item ->
                        SwipeToDismissItem(
                            item = item,
                            onDismiss = { viewModel.removeCartItemWithSnackbar(item) },
                            onIncrement = { viewModel.incrementQuantity(item.producto.id) },
                            onDecrement = { viewModel.decrementQuantity(item.producto.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    item: CartItem,
    onDismiss: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isDismissing = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val iconScale by animateFloatAsState(if (isDismissing) 1.2f else 0.8f, label = "scale")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar del carrito",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.scale(iconScale)
                )
            }
        }
    ) {
        CartItemRowCard(
            item = item,
            onIncrement = onIncrement,
            onDecrement = onDecrement
        )
    }
}

@Composable
fun CartItemRowCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Precio unitario: ${formatoMoneda(item.producto.precio)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Subtotal: ${formatoMoneda(item.subtotal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Restar 1")
                }
                Text(
                    text = "${item.cantidad}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    enabled = item.cantidad < item.producto.stock,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar 1")
                }
            }
        }
    }
}

@Composable
fun HistorialTabContent(
    uiState: VentaUiState,
    viewModel: VentaViewModel
) {
    val isAdmin = uiState.userRole == "ADMIN"
    var vendedorDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isAdmin && uiState.listaVendedores.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtrar por Vendedor:", style = MaterialTheme.typography.bodyMedium)
                Box {
                    OutlinedButton(onClick = { vendedorDropdownExpanded = true }) {
                        Text(uiState.filtroVendedor)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = vendedorDropdownExpanded,
                        onDismissRequest = { vendedorDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("TODOS") },
                            onClick = {
                                viewModel.selectFiltroVendedor("TODOS")
                                vendedorDropdownExpanded = false
                            }
                        )
                        uiState.listaVendedores.forEach { vend ->
                            DropdownMenuItem(
                                text = { Text(vend) },
                                onClick = {
                                    viewModel.selectFiltroVendedor(vend)
                                    vendedorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading) {
                EsqueletoCarga(filas = 5)
            } else if (uiState.ventasHistorial.isEmpty()) {
                EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.ReceiptLong,
                    titulo = "Sin historial de ventas",
                    mensaje = if (uiState.filtroVendedor != "TODOS") "Este vendedor no tiene ventas registradas." else "No hay registros de ventas que coincidan con los filtros seleccionados."
                )
            } else {
                val agrupadasPorFecha = remember(uiState.ventasHistorial) {
                    val zona = ZoneId.systemDefault()
                    uiState.ventasHistorial.groupBy { v ->
                        val zdt = DateTimeUtils.parseFechaVentaToZonedDateTime(v.fechaVenta, zona)
                        zdt.toLocalDate()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    agrupadasPorFecha.forEach { (fechaLocal, listaVentas) ->
                        val encabezadoTexto = DateTimeUtils.formatearEncabezadoFechaHistorial(fechaLocal)

                        item(key = "header_$fechaLocal") {
                            Text(
                                text = encabezadoTexto,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(
                            items = listaVentas,
                            key = { it.id }
                        ) { venta ->
                            VentaHistorialCardItem(
                                venta = venta,
                                onClick = { viewModel.openVentaDetailModal(venta) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VentaHistorialCardItem(
    venta: VentaResponseDto,
    onClick: () -> Unit
) {
    val horaTexto = remember(venta.fechaVenta) {
        DateTimeUtils.formatearHoraLocal(venta.fechaVenta)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Venta #${venta.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Vendedor: ${venta.usuarioNombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hora: $horaTexto · ${venta.detalles.size} ${if (venta.detalles.size == 1) "producto" else "productos"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatoMoneda(venta.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CobroBottomSheet(
    uiState: VentaUiState,
    viewModel: VentaViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Confirmar Cobro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL A PAGAR", fontWeight = FontWeight.Bold)
                    Text(formatoMoneda(uiState.total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = uiState.montoRecibidoText,
                onValueChange = viewModel::onMontoRecibidoChanged,
                label = { Text("Monto Recibido ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 20, 50).forEach { billete ->
                    OutlinedButton(
                        onClick = { viewModel.selectBilleteRapido(billete.toDouble()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$$billete")
                    }
                }
            }

            Surface(
                color = if (uiState.cambioCalculado >= 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (uiState.cambioCalculado >= 0) "CAMBIO A ENTREGAR" else "MONTO INSUFICIENTE", fontWeight = FontWeight.Bold)
                    Text(
                        text = if (uiState.cambioCalculado >= 0) formatoMoneda(uiState.cambioCalculado) else "Falta ${formatoMoneda(-uiState.cambioCalculado)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = viewModel::confirmarVenta,
                enabled = uiState.cambioCalculado >= 0 && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("FINALIZAR VENTA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ComprobanteVentaDialog(
    venta: VentaResponseDto,
    cambio: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("¡Venta Exitosa!", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ticket #${venta.id}", fontWeight = FontWeight.Bold)
                Text("Vendedor: ${venta.usuarioNombre}", style = MaterialTheme.typography.bodySmall)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                venta.detalles.forEach { det ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${det.cantidad}x ${det.productoNombre}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(formatoMoneda(det.subtotal), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL PAGADO", fontWeight = FontWeight.Bold)
                    Text(formatoMoneda(venta.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CAMBIO ENTREGADO", fontWeight = FontWeight.Bold)
                    Text(formatoMoneda(cambio), fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Nueva Venta")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { compartirComprobante(context, venta, cambio) }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Compartir")
            }
        }
    )
}

@Composable
fun DetalleVentaModal(
    venta: VentaResponseDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val horaTexto = remember(venta.fechaVenta) {
        DateTimeUtils.formatearHoraLocal(venta.fechaVenta)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de Venta #${venta.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Vendedor: ${venta.usuarioNombre}", style = MaterialTheme.typography.bodyMedium)
                Text("Hora: $horaTexto", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider()

                venta.detalles.forEach { det ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(det.productoNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${det.cantidad} x ${formatoMoneda(det.precioUnitario)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(formatoMoneda(det.subtotal), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL VENTA", fontWeight = FontWeight.Bold)
                    Text(formatoMoneda(venta.total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            TextButton(onClick = { compartirComprobante(context, venta, 0.0) }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Compartir")
            }
        }
    )
}

fun compartirComprobante(context: Context, venta: VentaResponseDto, cambio: Double) {
    val text = StringBuilder()
    text.append("--- INVENTARIO PYME ---\n")
    text.append("Comprobante de Venta #${venta.id}\n")
    text.append("Vendedor: ${venta.usuarioNombre}\n\n")
    text.append("Detalle:\n")
    venta.detalles.forEach { det ->
        text.append("${det.cantidad}x ${det.productoNombre} - ${formatoMoneda(det.subtotal)}\n")
    }
    text.append("\nTOTAL: ${formatoMoneda(venta.total)}\n")
    if (cambio > 0) {
        text.append("CAMBIO: ${formatoMoneda(cambio)}\n")
    }
    text.append("\n¡Gracias por su compra!")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Comprobante"))
}

@Preview(showBackground = true)
@Composable
fun CartItemRowCardPreview() {
    GestionInventariosTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CartItemRowCard(
                item = CartItem(
                    producto = ProductoDto(
                        id = 1,
                        codigoBarras = "750100000001",
                        nombre = "Coca-Cola 600ml",
                        precio = 18.50,
                        costo = 12.00,
                        stock = 50,
                        stockMinimo = 10,
                        categoriaId = 1,
                        proveedorId = 1,
                        activo = true
                    ),
                    cantidad = 2
                ),
                onIncrement = {},
                onDecrement = {}
            )
        }
    }
}
