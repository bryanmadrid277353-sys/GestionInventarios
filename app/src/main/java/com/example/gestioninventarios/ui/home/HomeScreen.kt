package com.example.gestioninventarios.ui.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.AvatarIniciales
import com.example.gestioninventarios.ui.components.ConfirmDialog
import com.example.gestioninventarios.ui.components.KpiCard
import com.example.gestioninventarios.ui.components.StatusChip
import com.example.gestioninventarios.ui.components.TipoKpi
import com.example.gestioninventarios.ui.components.TipoStatus
import com.example.gestioninventarios.ui.components.formatoMonedaCompacta
import com.example.gestioninventarios.ui.theme.GestionInventariosTheme
import com.example.gestioninventarios.ui.theme.Semantico

data class ModuloItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val badgeCount: Int = 0,
    val isFullWidth: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPos: () -> Unit = {},
    onNavigateToProductos: () -> Unit = {},
    onNavigateToCatalogos: () -> Unit = {},
    onNavigateToReportes: () -> Unit = {},
    onNavigateToUsuarios: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onPromptLogout = viewModel::promptLogout,
        onNavigateToPos = onNavigateToPos,
        onNavigateToProductos = onNavigateToProductos,
        onNavigateToCatalogos = onNavigateToCatalogos,
        onNavigateToReportes = onNavigateToReportes,
        onNavigateToUsuarios = onNavigateToUsuarios
    )

    if (uiState.showLogoutConfirmDialog) {
        ConfirmDialog(
            titulo = "Cerrar Sesión",
            mensaje = "¿Estás seguro de que deseas salir del sistema?",
            textoConfirmar = "Cerrar Sesión",
            textoCancelar = "Cancelar",
            destructivo = true,
            onConfirmar = viewModel::logout,
            onDismiss = viewModel::dismissLogoutDialog
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onPromptLogout: () -> Unit = {},
    onNavigateToPos: () -> Unit = {},
    onNavigateToProductos: () -> Unit = {},
    onNavigateToCatalogos: () -> Unit = {},
    onNavigateToReportes: () -> Unit = {},
    onNavigateToUsuarios: () -> Unit = {}
) {
    val isAdmin = uiState.userRole == "ADMIN"
    val scheme = MaterialTheme.colorScheme

    val modulos = if (isAdmin) {
        listOf(
            ModuloItem("pos", "Punto de Venta", "Vender / Escanear", Icons.Default.PointOfSale, scheme.primaryContainer, scheme.onPrimaryContainer, onClick = onNavigateToPos),
            ModuloItem("productos", "Productos", "Consultar / Stock", Icons.Default.Inventory, scheme.secondaryContainer, scheme.onSecondaryContainer, badgeCount = uiState.stockBajoCount, onClick = onNavigateToProductos),
            ModuloItem("catalogos", "Catálogos", "Categorías / Proveedores", Icons.Default.Category, scheme.tertiaryContainer, scheme.onTertiaryContainer, onClick = onNavigateToCatalogos),
            ModuloItem("reportes", "Reportes", "Ventas / Stock Bajo", Icons.Default.Assessment, scheme.surfaceContainerHigh, scheme.onSurface, badgeCount = uiState.stockBajoCount, onClick = onNavigateToReportes),
            ModuloItem("usuarios", "Usuarios", "Gestión de Personal", Icons.Default.Group, scheme.surfaceVariant, scheme.onSurfaceVariant, isFullWidth = true, onClick = onNavigateToUsuarios)
        )
    } else {
        listOf(
            ModuloItem("pos", "Punto de Venta", "Registrar ventas y escanear códigos de barras", Icons.Default.PointOfSale, scheme.primaryContainer, scheme.onPrimaryContainer, isFullWidth = true, onClick = onNavigateToPos),
            ModuloItem("productos", "Productos e Inventario", "Consultar precios, detalles y existencias", Icons.Default.Inventory, scheme.secondaryContainer, scheme.onSecondaryContainer, badgeCount = uiState.stockBajoCount, isFullWidth = true, onClick = onNavigateToProductos)
        )
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Inventario PyME",
                acciones = {
                    IconButton(onClick = onPromptLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Saludo
            item(span = { GridItemSpan(maxLineSpan) }) {
                GreetingCard(uiState = uiState)
            }

            // 2. Header Resumen del Día
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = if (isAdmin) "Resumen del Día" else "Mi Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Fila de KPIs
            item(span = { GridItemSpan(maxLineSpan) }) {
                KpisSection(uiState = uiState, isAdmin = isAdmin)
            }

            // 4. Banner de Stock Bajo
            if (uiState.stockBajoCount > 0) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    StockBajoBanner(
                        count = uiState.stockBajoCount,
                        onClick = if (isAdmin) onNavigateToReportes else onNavigateToProductos
                    )
                }
            }

            // 5. Header Módulos Principales
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Módulos Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 6. Tarjetas de Módulos
            itemsIndexed(
                items = modulos,
                key = { _, item -> item.id },
                span = { _, item ->
                    if (item.isFullWidth) GridItemSpan(maxLineSpan) else GridItemSpan(1)
                }
            ) { _, item ->
                ModuloCard(item = item)
            }
        }
    }
}

@Composable
fun GreetingCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarIniciales(
                nombre = uiState.userName.ifEmpty { "Usuario" },
                tamano = 52.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola, ${uiState.userName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (uiState.userEmail.isNotEmpty()) {
                    Text(
                        text = uiState.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            StatusChip(
                texto = uiState.userRole,
                tipo = if (uiState.userRole == "ADMIN") TipoStatus.Info else TipoStatus.Advertencia
            )
        }
    }
}

@Composable
fun KpisSection(
    uiState: HomeUiState,
    isAdmin: Boolean
) {
    if (isAdmin) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                icono = Icons.Default.AttachMoney,
                valor = formatoMonedaCompacta(uiState.ventasHoyTotal),
                etiqueta = "Ventas Hoy",
                tipo = TipoKpi.Exito,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icono = Icons.Default.Receipt,
                valor = "${uiState.numeroVentasHoy}",
                etiqueta = "Nº Ventas",
                tipo = TipoKpi.Info,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icono = Icons.Default.Warning,
                valor = "${uiState.stockBajoCount}",
                etiqueta = "Stock Bajo",
                tipo = if (uiState.stockBajoCount > 0) TipoKpi.Advertencia else TipoKpi.Exito,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                icono = Icons.Default.AttachMoney,
                valor = formatoMonedaCompacta(uiState.misVentasHoyTotal),
                etiqueta = "Mis Ventas Hoy",
                tipo = TipoKpi.Exito,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                icono = Icons.Default.PointOfSale,
                valor = formatoMonedaCompacta(uiState.ultimaVentaMonto),
                etiqueta = "Última Venta",
                tipo = TipoKpi.Info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StockBajoBanner(
    count: Int,
    onClick: () -> Unit
) {
    val semantico = Semantico.colores
    Surface(
        color = semantico.advertenciaContenedor,
        contentColor = semantico.onAdvertenciaContenedor,
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alerta stock bajo",
                    tint = semantico.advertencia,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Atención: Stock Bajo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hay $count ${if (count == 1) "producto" else "productos"} con stock bajo o agotado",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = "Ver detalles",
                tint = semantico.onAdvertenciaContenedor
            )
        }
    }
}

@Composable
fun ModuloCard(
    item: ModuloItem,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = item.containerColor,
            contentColor = item.contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = item.onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.contentColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold,
                    color = item.contentColor
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = item.contentColor.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ================= PREVIEWS DE VERIFICACIÓN =================

@Preview(name = "ADMIN - 360dp", widthDp = 360, showBackground = true)
@Preview(name = "ADMIN - 411dp", widthDp = 411, showBackground = true)
@Preview(name = "ADMIN - Font 1.3x", widthDp = 360, fontScale = 1.3f, showBackground = true)
@Composable
fun HomeAdminPreview() {
    GestionInventariosTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                userName = "Administrador",
                userRole = "ADMIN",
                userEmail = "admin@inventariopyme.com",
                ventasHoyTotal = 0.0,
                numeroVentasHoy = 0,
                stockBajoCount = 2
            )
        )
    }
}

@Preview(name = "VENDEDOR - 360dp", widthDp = 360, showBackground = true)
@Preview(name = "VENDEDOR - 411dp", widthDp = 411, showBackground = true)
@Preview(name = "VENDEDOR - Font 1.3x", widthDp = 360, fontScale = 1.3f, showBackground = true)
@Composable
fun HomeVendedorPreview() {
    GestionInventariosTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                userName = "Carlos Vendedor",
                userRole = "VENDEDOR",
                userEmail = "vendedor@inventariopyme.com",
                misVentasHoyTotal = 125.50,
                ultimaVentaMonto = 45.00,
                stockBajoCount = 0
            )
        )
    }
}
