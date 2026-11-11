package com.example.gestioninventarios.ui.reportes

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioninventarios.data.model.ReporteStockBajoDto
import com.example.gestioninventarios.ui.components.AppScaffold
import com.example.gestioninventarios.ui.components.AppTopBar
import com.example.gestioninventarios.ui.components.EsqueletoCarga
import com.example.gestioninventarios.ui.components.EstadoVacio
import com.example.gestioninventarios.ui.components.KpiCard
import com.example.gestioninventarios.ui.components.StockBadge
import com.example.gestioninventarios.ui.components.TipoKpi
import com.example.gestioninventarios.ui.components.formatoMoneda
import com.example.gestioninventarios.ui.theme.GestionInventariosTheme
import com.example.gestioninventarios.ui.theme.Semantico
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    viewModel: ReportesViewModel,
    onNavigateToPos: () -> Unit = {},
    onNavigateToProductos: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var stockBajoYPosition by remember { mutableStateOf(0f) }

    AppScaffold(
        topBar = {
            AppTopBar(
                titulo = "Reportes y Estadísticas",
                onBack = onNavigateBack,
                acciones = {
                    IconButton(
                        onClick = { exportarReporteCSV(context, uiState) }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar CSV")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (uiState.isLoading) {
                EsqueletoCarga(filas = 5)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + paddingValues.calculateBottomPadding())
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (uiState.errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Ocurrió un error al cargar los reportes.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Período del Reporte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Periodo.entries.forEachIndexed { index, p ->
                                SegmentedButton(
                                    selected = uiState.periodo == p,
                                    onClick = { viewModel.selectPeriodo(p) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = Periodo.entries.size)
                                ) {
                                    Text(p.titulo)
                                }
                            }
                        }

                        if (uiState.rangoTexto.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.rangoTexto,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                icono = Icons.Default.AttachMoney,
                                valor = formatoMoneda(uiState.totalPeriodo),
                                etiqueta = "Ventas ${uiState.periodo.titulo}",
                                tipo = TipoKpi.Exito,
                                modifier = Modifier.weight(1f)
                            )

                            KpiCard(
                                icono = Icons.Default.Receipt,
                                valor = "${uiState.numeroVentas}",
                                etiqueta = "Nº Transacciones",
                                tipo = TipoKpi.Info,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCard(
                                icono = Icons.Default.PointOfSale,
                                valor = formatoMoneda(uiState.ticketPromedio),
                                etiqueta = "Ticket Promedio",
                                tipo = TipoKpi.Primario,
                                modifier = Modifier.weight(1f)
                            )

                            KpiCard(
                                icono = Icons.Default.Warning,
                                valor = "${uiState.cantidadAlertas}",
                                etiqueta = "Alertas Stock Bajo",
                                tipo = if (uiState.cantidadAlertas > 0) TipoKpi.Advertencia else TipoKpi.Exito,
                                onClick = {
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(stockBajoYPosition.toInt())
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Comparativa Período Previo:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.variacionTexto,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (uiState.esVariacionPositiva) {
                                        true -> Semantico.colores.exito
                                        false -> MaterialTheme.colorScheme.error
                                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Comportamiento de Ventas (${uiState.periodo.titulo})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.chartBars.isEmpty() || uiState.chartBars.all { it.montoTotal == 0.0 }) {
                                EstadoVacio(
                                    icono = Icons.Default.Assessment,
                                    titulo = "Sin ventas en este período",
                                    mensaje = "No hay registros de ventas para la vista seleccionada."
                                )
                            } else {
                                CustomBarChart(
                                    bars = uiState.chartBars,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                stockBajoYPosition = coordinates.positionInParent().y
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Semantico.colores.advertencia,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Alertas de Stock Bajo / Agotado",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.stockBajoList.isEmpty()) {
                                Text(
                                    text = "¡Excelente! No hay productos con stock crítico.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Semantico.colores.exito,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.stockBajoList.forEach { prod ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = prod.nombre,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Cód: ${prod.codigoBarras} · Cat: ${prod.categoriaNombre ?: "Sin cat."}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            StockBadge(stock = prod.stock, minimo = prod.stockMinimo)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBarChart(
    bars: List<ChartBarItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val isTodayColor = Semantico.colores.exito
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxMonto = remember(bars) { (bars.maxOfOrNull { it.montoTotal } ?: 1.0).coerceAtLeast(1.0) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(bars) {
                detectTapGestures { offset ->
                    val width = size.width
                    val paddingLeft = 40.dp.toPx()
                    val availableWidth = width - paddingLeft
                    val barWidth = availableWidth / bars.size

                    if (offset.x >= paddingLeft) {
                        val index = ((offset.x - paddingLeft) / barWidth).toInt().coerceIn(0, bars.size - 1)
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 20.dp.toPx()

        val chartHeight = height - paddingBottom - paddingTop
        val chartWidth = width - paddingLeft

        val barCount = bars.size
        val totalBarSpace = chartWidth / barCount
        val barWidth = (totalBarSpace * 0.55f).coerceAtMost(28.dp.toPx())

        val gridLevels = listOf(0.0, maxMonto / 2.0, maxMonto)
        gridLevels.forEach { level ->
            val y = height - paddingBottom - ((level / maxMonto) * chartHeight).toFloat()
            drawLine(
                color = axisColor,
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )

            val labelText = if (level >= 1000) String.format(Locale.US, "$%.0fk", level / 1000) else String.format(Locale.US, "$%.0f", level)
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                style = TextStyle(fontSize = 10.sp, color = labelColor),
                topLeft = Offset(4.dp.toPx(), y - 12.dp.toPx())
            )
        }

        bars.forEachIndexed { index, item ->
            val fraction = (item.montoTotal / maxMonto).toFloat()
            val barHeight = fraction * chartHeight

            val xCenter = paddingLeft + (index * totalBarSpace) + (totalBarSpace / 2)
            val left = xCenter - (barWidth / 2)
            val top = height - paddingBottom - barHeight

            val barColor = if (item.isToday) isTodayColor else primaryColor
            val isSelected = selectedIndex == index

            drawRoundRect(
                color = if (isSelected) barColor.copy(alpha = 0.7f) else barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            if (isSelected) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(left - 2.dp.toPx(), top - 2.dp.toPx()),
                    size = Size(barWidth + 4.dp.toPx(), barHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )

                val tooltipText = "${item.subtext.ifEmpty { item.label }}: ${formatoMoneda(item.montoTotal)}"
                val textLayoutResult = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                )

                val ttWidth = textLayoutResult.size.width + 16.dp.toPx()
                val ttHeight = textLayoutResult.size.height + 8.dp.toPx()
                var ttX = xCenter - (ttWidth / 2)
                ttX = ttX.coerceIn(8.dp.toPx(), width - ttWidth - 8.dp.toPx())
                val ttY = (top - ttHeight - 6.dp.toPx()).coerceAtLeast(4.dp.toPx())

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = Offset(ttX, ttY),
                    size = Size(ttWidth, ttHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(ttX + 8.dp.toPx(), ttY + 4.dp.toPx())
                )
            }

            if (item.showLabel) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = item.label,
                    style = TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal),
                    topLeft = Offset(xCenter - (textMeasurer.measure(item.label).size.width / 2), height - paddingBottom + 4.dp.toPx())
                )
            }
        }
    }
}

fun exportarReporteCSV(context: Context, uiState: ReportesUiState) {
    val csvData = StringBuilder()
    csvData.append("Reporte de Ventas - Inventario PyME\n")
    csvData.append("Periodo: ${uiState.periodo.titulo} (${uiState.rangoTexto})\n\n")

    csvData.append("Resumen\n")
    csvData.append("Ventas Totales,${uiState.totalPeriodo}\n")
    csvData.append("Numero de Transacciones,${uiState.numeroVentas}\n")
    csvData.append("Ticket Promedio,${uiState.ticketPromedio}\n\n")

    csvData.append("Productos Mas Vendidos\n")
    csvData.append("Producto,Cantidad,Total Venta\n")
    uiState.topProductos.forEach {
        csvData.append("\"${it.productoNombre}\",${it.cantidadVendida},${it.totalMonto}\n")
    }

    csvData.append("\nAlertas de Stock Bajo\n")
    csvData.append("Producto,Codigo,Stock Actual,Stock Minimo,Categoria,Proveedor\n")
    uiState.stockBajoList.forEach {
        csvData.append("\"${it.nombre}\",\"${it.codigoBarras}\",${it.stock},${it.stockMinimo},\"${it.categoriaNombre ?: ""}\",\"${it.proveedorNombre ?: ""}\"\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte_Inventario_PyME_${uiState.periodo.name}.csv")
        putExtra(Intent.EXTRA_TEXT, csvData.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartir Reporte CSV"))
}

@Preview(showBackground = true)
@Composable
fun ReportesScreenPreview() {
    GestionInventariosTheme {
        Text("Preview Reportes")
    }
}
