package com.example.gestioninventarios.ui.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.StatusChip
import com.example.gestioninventarios.ui.components.TipoStatus
import com.example.gestioninventarios.util.CodigoInterno
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFormScreen(
    viewModel: ProductoFormViewModel,
    navController: NavController,
    productoId: Int? = null,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToEditProduct: (Int) -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productoId) {
        viewModel.initForm(productoId)
    }

    val currentBackStackEntry = navController.currentBackStackEntry
    val scannedCodeFlow = currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("codigo", null)
    val scannedCode by scannedCodeFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(scannedCode) {
        if (!scannedCode.isNullOrEmpty()) {
            val codeToApply = scannedCode!!
            currentBackStackEntry?.savedStateHandle?.remove<String>("codigo")
            viewModel.onCodigoScanned(codeToApply)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    var catExpanded by remember { mutableStateOf(false) }
    var provExpanded by remember { mutableStateOf(false) }

    val precioNum = uiState.precio.toDoubleOrNull() ?: 0.0
    val costoNum = uiState.costo.toDoubleOrNull() ?: 0.0
    val margenCalculado = if (costoNum > 0 && precioNum >= costoNum) {
        ((precioNum - costoNum) / costoNum) * 100
    } else 0.0

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = if (uiState.isEditing) "Editar Producto" else "Nuevo Producto",
                onBack = viewModel::promptCancel
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.categorias.isEmpty()) {
            EsqueletoCarga(filas = 5, modifier = Modifier.padding(top = paddingValues.calculateTopPadding()))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Ocurrió un error al procesar el producto.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (uiState.duplicateWarningMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.duplicateWarningMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            uiState.existingDuplicateProduct?.let { dupe ->
                                TextButton(onClick = { onNavigateToEditProduct(dupe.id) }) {
                                    Text("Ver / Editar", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Text(text = "Clasificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    val selectedCatName = uiState.categorias.find { it.id == uiState.selectedCategoriaId }?.nombre ?: "Seleccionar Categoría"
                    OutlinedTextField(
                        value = selectedCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        isError = uiState.categoriaError != null,
                        supportingText = { uiState.categoriaError?.let { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        uiState.categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.nombre} (Código ${String.format(Locale.US, "%02d", cat.codigo)})") },
                                onClick = {
                                    viewModel.onCategoriaSelected(cat.id)
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = provExpanded,
                    onExpandedChange = { provExpanded = !provExpanded }
                ) {
                    val selectedProvName = uiState.proveedores.find { it.id == uiState.selectedProveedorId }?.nombre ?: "Seleccionar Proveedor"
                    OutlinedTextField(
                        value = selectedProvName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Proveedor") },
                        isError = uiState.proveedorError != null,
                        supportingText = { uiState.proveedorError?.let { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = provExpanded,
                        onDismissRequest = { provExpanded = false }
                    ) {
                        uiState.proveedores.forEach { prov ->
                            DropdownMenuItem(
                                text = { Text("${prov.nombre} (Código ${String.format(Locale.US, "%02d", prov.codigo)})") },
                                onClick = {
                                    viewModel.onProveedorSelected(prov.id)
                                    provExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text(text = "Código de Barras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.codigoBarras,
                        onValueChange = viewModel::onCodigoBarrasChange,
                        label = { Text("Código de Barras EAN-13") },
                        isError = uiState.codigoBarrasError != null,
                        supportingText = { uiState.codigoBarrasError?.let { Text(it) } },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear Código",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = viewModel::generarCodigoInterno,
                    enabled = uiState.selectedCategoriaId != null && uiState.selectedProveedorId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar Código Interno EAN-13")
                }

                if (uiState.isAutoGenerated && uiState.codigoBarras.isNotEmpty()) {
                    val formateado = CodigoInterno.formatear(uiState.codigoBarras)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Código generado automáticamente:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formateado,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text(text = "Información del Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = { Text("Nombre del Producto") },
                    isError = uiState.nombreError != null,
                    supportingText = { uiState.nombreError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.precio,
                        onValueChange = viewModel::onPrecioChange,
                        label = { Text("Precio de Venta ($)") },
                        isError = uiState.precioError != null,
                        supportingText = { uiState.precioError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.costo,
                        onValueChange = viewModel::onCostoChange,
                        label = { Text("Costo ($)") },
                        isError = uiState.costoError != null,
                        supportingText = { uiState.costoError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (costoNum > 0 && precioNum >= costoNum) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Margen de Ganancia:", style = MaterialTheme.typography.bodyMedium)
                        StatusChip(
                            texto = String.format(Locale.US, "%.1f%%", margenCalculado),
                            tipo = TipoStatus.Exito
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.stock,
                        onValueChange = viewModel::onStockChange,
                        label = { Text("Stock Actual") },
                        isError = uiState.stockError != null,
                        supportingText = { uiState.stockError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = uiState.stockMinimo,
                        onValueChange = viewModel::onStockMinimoChange,
                        label = { Text("Stock Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::promptCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = viewModel::guardarProducto,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (uiState.isEditing) "Actualizar" else "Guardar")
                        }
                    }
                }
            }
        }
    }

    if (uiState.showUnsavedChangesDialog) {
        ConfirmDialog(
            titulo = "Cambios sin guardar",
            mensaje = "Tienes cambios sin guardar en el formulario. ¿Estás seguro de que deseas salir?",
            textoConfirmar = "Salir sin guardar",
            textoCancelar = "Continuar editando",
            destructivo = true,
            onConfirmar = {
                viewModel.dismissUnsavedChangesDialog()
                onNavigateBack()
            },
            onDismiss = viewModel::dismissUnsavedChangesDialog
        )
    }
}
