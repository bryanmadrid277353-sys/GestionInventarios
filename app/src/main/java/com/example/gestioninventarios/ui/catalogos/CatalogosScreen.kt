package com.example.gestioninventarios.ui.catalogos

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gestioninventarios.data.model.CategoriaDto
import com.example.gestioninventarios.data.model.ProveedorDto
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.AvatarIniciales
import com.example.gestioninventarios.ui.components.BuscadorCampo
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.EstadoVacio
import com.example.gestioninventarios.ui.components.StatusChip
import com.example.gestioninventarios.ui.components.TipoStatus
import com.example.gestioninventarios.ui.theme.GestionInventariosTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogosScreen(
    viewModel: CatalogosViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin = uiState.userRole == "ADMIN"

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Catálogos de Negocio",
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(if (uiState.selectedTab == 0) "Nueva categoría" else "Nuevo proveedor")
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        if (uiState.selectedTab == 0) viewModel.openCreateCategoriaBottomSheet()
                        else viewModel.openCreateProveedorBottomSheet()
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), start = 16.dp, end = 16.dp)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Categorías", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Proveedores", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalElementos = if (uiState.selectedTab == 0) uiState.categorias.size else uiState.proveedores.size
            if (totalElementos > 8) {
                BuscadorCampo(
                    valor = uiState.searchQuery,
                    onValorChange = viewModel::onSearchQueryChanged,
                    placeholder = if (uiState.selectedTab == 0) "Buscar categorías..." else "Buscar proveedores..."
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    EsqueletoCarga(filas = 4)
                } else if (uiState.selectedTab == 0) {
                    if (uiState.categoriasFiltradas.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Default.Category,
                            titulo = "Sin categorías",
                            mensaje = if (uiState.searchQuery.isEmpty()) "No hay categorías registradas." else "No se encontraron categorías coincidentes."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp + paddingValues.calculateBottomPadding()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.categoriasFiltradas) { cat ->
                                val count = uiState.categoriaProductCounts[cat.id] ?: 0
                                CategoriaItemCard(
                                    categoria = cat,
                                    productCount = count,
                                    isAdmin = isAdmin,
                                    onEdit = { viewModel.openEditCategoriaBottomSheet(cat) },
                                    onDelete = { viewModel.promptDelete("categoria", cat.id, cat.nombre) }
                                )
                            }
                        }
                    }
                } else {
                    if (uiState.proveedoresFiltrados.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Default.LocalShipping,
                            titulo = "Sin proveedores",
                            mensaje = if (uiState.searchQuery.isEmpty()) "No hay proveedores registrados." else "No se encontraron proveedores coincidentes."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 88.dp + paddingValues.calculateBottomPadding()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.proveedoresFiltrados) { prov ->
                                val count = uiState.proveedorProductCounts[prov.id] ?: 0
                                ProveedorItemCard(
                                    proveedor = prov,
                                    productCount = count,
                                    isAdmin = isAdmin,
                                    onEdit = { viewModel.openEditProveedorBottomSheet(prov) },
                                    onDelete = { viewModel.promptDelete("proveedor", prov.id, prov.nombre) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCategoriaBottomSheet) {
        FormularioCategoriaBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = viewModel::closeDialogs
        )
    }

    if (uiState.showProveedorBottomSheet) {
        FormularioProveedorBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = viewModel::closeDialogs
        )
    }

    if (uiState.showBlockedDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissBlockedDeleteDialog,
            title = { Text("No se puede eliminar", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.blockedDeleteMessage) },
            confirmButton = {
                Button(onClick = viewModel::dismissBlockedDeleteDialog) {
                    Text("Entendido")
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        ConfirmDialog(
            titulo = "Confirmar Eliminación",
            mensaje = "¿Estás seguro de que deseas eliminar '${uiState.deleteItemName}'? Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            destructivo = true,
            onConfirmar = viewModel::confirmDelete,
            onDismiss = viewModel::closeDialogs
        )
    }
}

@Composable
fun CategoriaItemCard(
    categoria: CategoriaDto,
    productCount: Int,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = categoria.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusChip(
                            texto = String.format(Locale.US, "Código %02d", categoria.codigo),
                            tipo = TipoStatus.Info
                        )
                    }
                    if (!categoria.descripcion.isNullOrEmpty()) {
                        Text(
                            text = categoria.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        texto = "$productCount ${if (productCount == 1) "producto" else "productos"}",
                        tipo = TipoStatus.Neutro
                    )
                }
            }

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
}

@Composable
fun ProveedorItemCard(
    proveedor: ProveedorDto,
    productCount: Int,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                AvatarIniciales(nombre = proveedor.nombre, tamano = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = proveedor.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusChip(
                            texto = String.format(Locale.US, "Código %02d", proveedor.codigo),
                            tipo = TipoStatus.Info
                        )
                    }

                    if (!proveedor.contacto.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = proveedor.contacto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (!proveedor.telefono.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${proveedor.telefono}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Llamar", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = proveedor.telefono, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!proveedor.email.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${proveedor.email}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Enviar correo", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = proveedor.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    StatusChip(
                        texto = "$productCount ${if (productCount == 1) "producto" else "productos"}",
                        tipo = TipoStatus.Neutro
                    )
                }
            }

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCategoriaBottomSheet(
    uiState: CatalogosUiState,
    viewModel: CatalogosViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = uiState.categoriaEditing != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) "Editar Categoría" else "Nueva Categoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (!isEditing) {
                Text(
                    text = String.format(Locale.US, "Se le asignará el código %02d", uiState.siguienteCodigoCategoria),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = String.format(Locale.US, "Código asignado: %02d", uiState.categoriaEditing.codigo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = uiState.catNombre,
                onValueChange = viewModel::onCatNombreChange,
                label = { Text("Nombre de la Categoría") },
                isError = uiState.catNombreError != null,
                supportingText = { uiState.catNombreError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.catDescripcion,
                onValueChange = viewModel::onCatDescripcionChange,
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::guardarCategoria,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Categoría")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioProveedorBottomSheet(
    uiState: CatalogosUiState,
    viewModel: CatalogosViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = uiState.proveedorEditing != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditing) "Editar Proveedor" else "Nuevo Proveedor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (!isEditing) {
                Text(
                    text = String.format(Locale.US, "Se le asignará el código %02d", uiState.siguienteCodigoProveedor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = String.format(Locale.US, "Código asignado: %02d", uiState.proveedorEditing.codigo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = uiState.provNombre,
                onValueChange = viewModel::onProvNombreChange,
                label = { Text("Nombre del Proveedor") },
                isError = uiState.provNombreError != null,
                supportingText = { uiState.provNombreError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.provContacto,
                onValueChange = viewModel::onProvContactoChange,
                label = { Text("Nombre de Contacto (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.provTelefono,
                onValueChange = viewModel::onProvTelefonoChange,
                label = { Text("Teléfono (opcional)") },
                isError = uiState.provTelefonoError != null,
                supportingText = { uiState.provTelefonoError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.provEmail,
                onValueChange = viewModel::onProvEmailChange,
                label = { Text("Correo Electrónico (opcional)") },
                isError = uiState.provEmailError != null,
                supportingText = { uiState.provEmailError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::guardarProveedor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Proveedor")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriaItemCardPreview() {
    GestionInventariosTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CategoriaItemCard(
                categoria = CategoriaDto(1, "Bebidas", "Refrescos y jugos", true, 1),
                productCount = 12,
                isAdmin = true,
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
