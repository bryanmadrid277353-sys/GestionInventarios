package com.example.gestioninventarios.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.model.ReporteStockBajoDto
import com.example.gestioninventarios.data.model.VentaResponseDto
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.ReporteRepository
import com.example.gestioninventarios.data.repository.VentaRepository
import com.example.gestioninventarios.util.DateTimeUtils
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

enum class Periodo(val dias: Int, val titulo: String) {
    SEMANA(7, "Semana"),
    MES(30, "Mes"),
    TRES_MESES(90, "3 meses")
}

data class TopProductoItem(
    val productoNombre: String,
    val cantidadVendida: Int,
    val totalMonto: Double
)

data class ChartBarItem(
    val label: String,
    val montoTotal: Double,
    val showLabel: Boolean = true,
    val isToday: Boolean = false,
    val subtext: String = ""
)

data class VentaParsed(
    val original: VentaResponseDto,
    val epochMilli: Long,
    val zdt: ZonedDateTime,
    val total: Double
)

data class ReportesUiState(
    val periodo: Periodo = Periodo.SEMANA,
    val rangoTexto: String = "",
    val chartBars: List<ChartBarItem> = emptyList(),
    val topProductos: List<TopProductoItem> = emptyList(),
    val stockBajoList: List<ReporteStockBajoDto> = emptyList(),
    val ventasHoy: Double = 0.0,
    val totalPeriodo: Double = 0.0,
    val variacionTexto: String = "Sin datos previos",
    val esVariacionPositiva: Boolean? = null,
    val numeroVentas: Int = 0,
    val ticketPromedio: Double = 0.0,
    val cantidadAlertas: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReportesViewModel(
    private val reporteRepository: ReporteRepository,
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init {
        cargarReportes()
    }

    fun selectPeriodo(periodo: Periodo) {
        if (_uiState.value.periodo == periodo) return
        _uiState.update { it.copy(periodo = periodo) }
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val zona = ZoneId.systemDefault()
            val hoyLocal = LocalDate.now(zona)
            val periodo = _uiState.value.periodo

            val rangoActual = DateTimeUtils.rangoDe(periodo, zona, hoyLocal)
            val rangoPrevio = DateTimeUtils.rangoPrevioDe(periodo, zona, hoyLocal)

            val sdfRango = DateTimeFormatter.ofPattern("d MMM", Locale("es", "SV"))
            val rangoTexto = "${rangoActual.fechaInicioLocal.format(sdfRango)} - ${rangoActual.fechaFinLocal.format(sdfRango)}"

            val stockRes = reporteRepository.obtenerStockBajo()
            val stockList = if (stockRes is Resource.Success) stockRes.data ?: emptyList() else emptyList()

            val ventasRes = ventaRepository.listarVentas()
            val todasVentas = if (ventasRes is Resource.Success) ventasRes.data ?: emptyList() else emptyList()

            val ventasParsed = todasVentas.mapNotNull { v ->
                val ms = DateTimeUtils.parseFechaVentaToEpochMilli(v.fechaVenta)
                if (ms == 0L) null
                else VentaParsed(v, ms, DateTimeUtils.parseFechaVentaToZonedDateTime(v.fechaVenta, zona), v.total)
            }

            val ventasPeriodoActual = ventasParsed.filter {
                it.epochMilli >= rangoActual.desdeMs && it.epochMilli < rangoActual.hastaMs
            }

            val ventasPeriodoPrevio = ventasParsed.filter {
                it.epochMilli >= rangoPrevio.desdeMs && it.epochMilli < rangoPrevio.hastaMs
            }

            val totalPeriodo = ventasPeriodoActual.sumOf { it.total }
            val numVentas = ventasPeriodoActual.size
            val ticketProm = if (numVentas > 0) totalPeriodo / numVentas else 0.0

            val totalPrevio = ventasPeriodoPrevio.sumOf { it.total }
            var varTexto = "Sin datos previos"
            var esPositiva: Boolean? = null

            if (totalPrevio > 0) {
                val diff = totalPeriodo - totalPrevio
                val pct = (diff / totalPrevio) * 100
                if (pct >= 0) {
                    varTexto = String.format(Locale.US, "+%.1f%% vs período previo", pct)
                    esPositiva = true
                } else {
                    varTexto = String.format(Locale.US, "-%.1f%% vs período previo", abs(pct))
                    esPositiva = false
                }
            } else if (totalPeriodo > 0) {
                varTexto = "+100% vs período previo"
                esPositiva = true
            }

            val bars = buildChartBars(periodo, ventasPeriodoActual, hoyLocal)

            val topProds = ventasPeriodoActual
                .flatMap { it.original.detalles }
                .groupBy { it.productoNombre }
                .map { (nombre, detalles) ->
                    TopProductoItem(
                        productoNombre = nombre,
                        cantidadVendida = detalles.sumOf { it.cantidad },
                        totalMonto = detalles.sumOf { it.subtotal }
                    )
                }
                .sortedByDescending { it.cantidadVendida }
                .take(5)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    periodo = periodo,
                    rangoTexto = rangoTexto,
                    chartBars = bars,
                    topProductos = topProds,
                    stockBajoList = stockList,
                    totalPeriodo = totalPeriodo,
                    numeroVentas = numVentas,
                    ticketPromedio = ticketProm,
                    variacionTexto = varTexto,
                    esVariacionPositiva = esPositiva,
                    cantidadAlertas = stockList.size
                )
            }
        }
    }

    private fun buildChartBars(
        periodo: Periodo,
        ventasActuales: List<VentaParsed>,
        hoyLocal: LocalDate
    ): List<ChartBarItem> {
        val sdfBarLabel = DateTimeFormatter.ofPattern("d MMM", Locale("es", "SV"))
        val sdfDayShort = DateTimeFormatter.ofPattern("EEE", Locale("es", "SV"))

        return when (periodo) {
            Periodo.SEMANA -> {
                (6 downTo 0).map { diasAtras ->
                    val dia = hoyLocal.minusDays(diasAtras.toLong())
                    val ventasDelDia = ventasActuales.filter { it.zdt.toLocalDate() == dia }
                    val totalDia = ventasDelDia.sumOf { it.total }
                    val label = dia.format(sdfDayShort).replace(".", "").replaceFirstChar { it.uppercase() }
                    ChartBarItem(
                        label = label,
                        montoTotal = totalDia,
                        showLabel = true,
                        isToday = dia == hoyLocal,
                        subtext = dia.format(sdfBarLabel)
                    )
                }
            }
            Periodo.MES -> {
                (29 downTo 0).map { diasAtras ->
                    val dia = hoyLocal.minusDays(diasAtras.toLong())
                    val ventasDelDia = ventasActuales.filter { it.zdt.toLocalDate() == dia }
                    val totalDia = ventasDelDia.sumOf { it.total }
                    ChartBarItem(
                        label = "${dia.dayOfMonth}",
                        montoTotal = totalDia,
                        showLabel = (diasAtras % 5 == 0 || dia == hoyLocal),
                        isToday = dia == hoyLocal,
                        subtext = dia.format(sdfBarLabel)
                    )
                }
            }
            Periodo.TRES_MESES -> {
                (11 downTo 0).map { semanasAtras ->
                    val inicioSemana = hoyLocal.minusWeeks(semanasAtras.toLong())
                    val finSemana = inicioSemana.plusDays(6)
                    val ventasSemana = ventasActuales.filter {
                        val fecha = it.zdt.toLocalDate()
                        !fecha.isBefore(inicioSemana) && !fecha.isAfter(finSemana)
                    }
                    val totalSemana = ventasSemana.sumOf { it.total }
                    ChartBarItem(
                        label = "S${12 - semanasAtras}",
                        montoTotal = totalSemana,
                        showLabel = true,
                        isToday = (semanasAtras == 0),
                        subtext = "${inicioSemana.format(sdfBarLabel)} - ${finSemana.format(sdfBarLabel)}"
                    )
                }
            }
        }
    }
}
