package com.example.gestioninventarios.ui.productos

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioninventarios.R
import com.example.gestioninventarios.data.model.ProductoDto
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.BuscadorCampo
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.EstadoVacio
import com.example.gestioninventarios.ui.components.StockBadge
import com.example.gestioninventarios.ui.components.formatoMoneda
import com.example.gestioninventarios.ui.theme.GestionInventariosTheme
import com.example.gestioninventarios.util.BarcodeGenerator
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    viewModel: ProductosViewModel,
    onNavigateToCreateProduct: () -> Unit = {},
    onNavigateToEditProduct: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin = uiState.userRole == "ADMIN"

    var selectedBarcodeProduct by remember { mutableStateOf<ProductoDto?>(null) }
    var ordenMenuExpanded by remember { mutableStateOf(false) }

    val filterLazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val categoriasOrdenadas = remember(uiState.categorias) {
        uiState.categorias.sortedBy { it.nombre.lowercase() }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Inventario de Productos",
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    text = { Text("Nuevo producto") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onNavigateToCreateProduct
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                BuscadorCampo(
                    valor = uiState.searchQuery,
                    onValorChange = viewModel::onSearchQueryChanged,
                    placeholder = "Buscar por nombre o código de barras..."
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                state = filterLazyListState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item(key = "orden") {
                    Box {
                        OutlinedButton(
                            onClick = { ordenMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                when (uiState.ordenFilter) {
                                    OrdenProducto.NOMBRE -> "Orden: Nombre"
                                    OrdenProducto.STOCK_ASC -> "Orden: Stock asc"
                                    OrdenProducto.PRECIO_ASC -> "Orden: Precio asc"
                                    OrdenProducto.PRECIO_DESC -> "Orden: Precio desc"
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Criterio de ordenación"
                            )
                        }

                        DropdownMenu(
                            expanded = ordenMenuExpanded,
                            onDismissRequest = { ordenMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nombre (A-Z)") },
                                onClick = {
                                    viewModel.selectOrdenFilter(OrdenProducto.NOMBRE)
                                    ordenMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Stock (Menor a Mayor)") },
                                onClick = {
                                    viewModel.selectOrdenFilter(OrdenProducto.STOCK_ASC)
                                    ordenMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Precio (Menor a Mayor)") },
                                onClick = {
                                    viewModel.selectOrdenFilter(OrdenProducto.PRECIO_ASC)
                                    ordenMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Precio (Mayor a Menor)") },
                                onClick = {
                                    viewModel.selectOrdenFilter(OrdenProducto.PRECIO_DESC)
                                    ordenMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                item(key = "stock_bajo") {
                    FilterChip(
                        selected = uiState.soloStockBajoFilter,
                        onClick = viewModel::toggleSoloStockBajoFilter,
                        label = { Text("Solo stock bajo") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }

                item(key = "divisor") {
                    Surface(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    ) {}
                }

                item(key = "cat_todas") {
                    FilterChip(
                        selected = uiState.selectedCategoriaFilterId == null,
                        onClick = {
                            viewModel.selectCategoriaFilter(null)
                            coroutineScope.launch {
                                filterLazyListState.animateScrollToItem(3)
                            }
                        },
                        label = { Text("Todas") },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }

                items(
                    items = categoriasOrdenadas,
                    key = { cat -> "cat_${cat.id}" }
                ) { cat ->
                    val isSelected = uiState.selectedCategoriaFilterId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectCategoriaFilter(cat.id)
                            val catIndex = categoriasOrdenadas.indexOfFirst { it.id == cat.id }
                            if (catIndex != -1) {
                                val itemPosition = 4 + catIndex
                                coroutineScope.launch {
                                    filterLazyListState.animateScrollToItem(itemPosition)
                                }
                            }
                        },
                        label = { Text(cat.nombre) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    EsqueletoCarga(filas = 5, modifier = Modifier.padding(horizontal = 16.dp))
                } else if (uiState.productosFiltrados.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.Inventory,
                        titulo = "Sin productos",
                        mensaje = if (uiState.searchQuery.isEmpty()) "No hay productos que coincidan con los filtros." else "No se encontraron productos coincidentes con tu búsqueda."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 0.dp,
                            bottom = 88.dp + paddingValues.calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.productosFiltrados,
                            key = { it.id }
                        ) { prod ->
                            ProductoCardItem(
                                producto = prod,
                                isAdmin = isAdmin,
                                onShowBarcode = { selectedBarcodeProduct = prod },
                                onEdit = { onNavigateToEditProduct(prod.id) },
                                onDelete = { viewModel.promptDelete(prod.id, prod.nombre) }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedBarcodeProduct?.let { prod ->
        VistaPreviaCodigoBarrasDialog(
            producto = prod,
            onDismiss = { selectedBarcodeProduct = null }
        )
    }

    if (uiState.showDeleteDialog) {
        ConfirmDialog(
            titulo = "Eliminar Producto",
            mensaje = "¿Deseas eliminar el producto '${uiState.deleteProductName}'? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            destructivo = true,
            onConfirmar = viewModel::confirmDelete,
            onDismiss = viewModel::closeDialogs
        )
    }
}

@Composable
fun ProductoCardItem(
    producto: ProductoDto,
    isAdmin: Boolean,
    onShowBarcode: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val margenPorcentaje = remember(producto.precio, producto.costo) {
        if (producto.costo > 0) ((producto.precio - producto.costo) / producto.costo) * 100 else 0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Código: ${producto.codigoBarras}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onShowBarcode,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_barcode),
                                contentDescription = "Ver código de barras de ${producto.nombre}",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(stock = producto.stock, minimo = producto.stockMinimo)

                    if (isAdmin) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar") },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cat: ${producto.categoriaNombre ?: "Sin cat."}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Prov: ${producto.proveedorNombre ?: "Sin prov."}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Precio: ${formatoMoneda(producto.precio)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Costo: ${formatoMoneda(producto.costo)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "· Margen: ${String.format(Locale.US, "%.1f", margenPorcentaje)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VistaPreviaCodigoBarrasDialog(
    producto: ProductoDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val bitmap = remember(producto.codigoBarras) {
        BarcodeGenerator.generateBarcodeBitmap(producto.codigoBarras)
    }

    fun copiarAlPortapapeles() {
        clipboardManager.setText(AnnotatedString(producto.codigoBarras))
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(producto.nombre, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color.White,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Código de barras de ${producto.nombre}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                        } else {
                            Text("No se pudo generar la vista previa")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = producto.codigoBarras,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { copiarAlPortapapeles() }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { copiarAlPortapapeles() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar código")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProductoCardItemPreview() {
    GestionInventariosTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductoCardItem(
                producto = ProductoDto(
                    id = 1,
                    codigoBarras = "750100000001",
                    nombre = "Coca-Cola 600ml",
                    precio = 18.50,
                    costo = 12.00,
                    stock = 3,
                    stockMinimo = 10,
                    categoriaId = 1,
                    proveedorId = 1,
                    activo = true,
                    categoriaNombre = "Bebidas",
                    proveedorNombre = "Distribuidora del Norte"
                ),
                isAdmin = true,
                onShowBarcode = {},
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
