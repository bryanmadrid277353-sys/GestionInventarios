package com.example.gestioninventarios.ui.usuarios

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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gestioninventarios.data.model.UsuarioResponseDto
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.AvatarIniciales
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.EstadoVacio
import com.example.gestioninventarios.ui.components.StatusChip
import com.example.gestioninventarios.ui.components.TipoStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Gestión de Usuarios",
                onBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo usuario") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = viewModel::openCreateUserBottomSheet
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), start = 16.dp, end = 16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Ocurrió un error al cargar usuarios.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !uiState.filtroActivosSolo,
                    onClick = { if (uiState.filtroActivosSolo) viewModel.toggleFiltroActivosSolo() },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = uiState.filtroActivosSolo,
                    onClick = { if (!uiState.filtroActivosSolo) viewModel.toggleFiltroActivosSolo() },
                    label = { Text("Solo activos") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    EsqueletoCarga(filas = 4)
                } else if (uiState.usuariosFiltrados.isEmpty()) {
                    EstadoVacio(
                        icono = Icons.Default.Group,
                        titulo = "Sin usuarios",
                        mensaje = "No hay usuarios registrados con los filtros seleccionados."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp + paddingValues.calculateBottomPadding()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.usuariosFiltrados) { usr ->
                            UsuarioItemCard(
                                usuario = usr,
                                isSelf = usr.email.equals(uiState.currentEmail, ignoreCase = true),
                                onToggleStatus = { viewModel.promptToggleStatus(usr) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showUserBottomSheet) {
        FormularioUsuarioBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = viewModel::closeDialogs
        )
    }

    if (uiState.showToggleStatusConfirmDialog) {
        val target = uiState.targetUserForToggle
        val actionText = if (target?.activo == true) "Desactivar" else "Activar"
        ConfirmDialog(
            titulo = "Confirmar $actionText",
            mensaje = "¿Estás seguro de que deseas $actionText al usuario '${target?.nombre}'?",
            textoConfirmar = actionText,
            destructivo = target?.activo == true,
            onConfirmar = viewModel::confirmToggleStatus,
            onDismiss = viewModel::closeDialogs
        )
    }

    if (uiState.showBlockedDialog) {
        AlertDialog(
            onDismissRequest = viewModel::closeDialogs,
            title = { Text("Acción no permitida", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.blockedDialogMessage) },
            confirmButton = {
                Button(onClick = viewModel::closeDialogs) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun UsuarioItemCard(
    usuario: UsuarioResponseDto,
    isSelf: Boolean,
    onToggleStatus: () -> Unit
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
                AvatarIniciales(nombre = usuario.nombre, tamano = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isSelf) {
                            StatusChip(texto = "Tú", tipo = TipoStatus.Info)
                        }
                    }
                    Text(text = usuario.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusChip(
                            texto = usuario.rol,
                            tipo = if (usuario.rol == "ADMIN") TipoStatus.Info else TipoStatus.Advertencia
                        )
                        StatusChip(
                            texto = if (usuario.activo) "Activo" else "Inactivo",
                            tipo = if (usuario.activo) TipoStatus.Exito else TipoStatus.Error
                        )
                    }
                }
            }

            if (!isSelf) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (usuario.activo) "Desactivar usuario" else "Activar usuario") },
                            onClick = {
                                menuExpanded = false
                                onToggleStatus()
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
fun FormularioUsuarioBottomSheet(
    uiState: UsuariosUiState,
    viewModel: UsuariosViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var passwordVisible by remember { mutableStateOf(false) }

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
                text = "Nuevo Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre Completo") },
                isError = uiState.nombreError != null,
                supportingText = { uiState.nombreError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo Electrónico") },
                isError = uiState.emailError != null,
                supportingText = { uiState.emailError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                isError = uiState.passwordError != null,
                supportingText = { uiState.passwordError?.let { Text(it) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Rol en el Sistema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.rol == "VENDEDOR",
                    onClick = { viewModel.onRolChange("VENDEDOR") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("VENDEDOR")
                }
                SegmentedButton(
                    selected = uiState.rol == "ADMIN",
                    onClick = { viewModel.onRolChange("ADMIN") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("ADMIN")
                }
            }

            Button(
                onClick = viewModel::guardarUsuario,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Usuario")
            }
        }
    }
}
