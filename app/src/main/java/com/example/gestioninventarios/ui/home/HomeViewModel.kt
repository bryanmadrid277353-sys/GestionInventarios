package com.example.gestioninventarios.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.ReporteRepository
import com.example.gestioninventarios.data.repository.VentaRepository
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val userName: String = "",
    val userRole: String = "",
    val userEmail: String = "",
    // ADMIN KPIs
    val ventasHoyTotal: Double = 0.0,
    val numeroVentasHoy: Int = 0,
    val stockBajoCount: Int = 0,
    // VENDEDOR KPIs
    val misVentasHoyTotal: Double = 0.0,
    val ultimaVentaMonto: Double = 0.0,
    // Diálogos
    val showLogoutConfirmDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val reporteRepository: ReporteRepository,
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userName.collectLatest { name ->
                _uiState.update { it.copy(userName = name ?: "Usuario") }
            }
        }
        viewModelScope.launch {
            authRepository.userRole.collectLatest { role ->
                _uiState.update { it.copy(userRole = role ?: "VENDEDOR") }
                cargarKpis()
            }
        }
        viewModelScope.launch {
            authRepository.userEmail.collectLatest { email ->
                _uiState.update { it.copy(userEmail = email ?: "") }
            }
        }
        cargarKpis()
    }

    fun cargarKpis() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val stockBajoRes = reporteRepository.obtenerStockBajo()
            var countStockBajo = 0
            if (stockBajoRes is Resource.Success && stockBajoRes.data != null) {
                countStockBajo = stockBajoRes.data.size
            } else {
                val cached = productoRepository.getProductosFromCache()
                countStockBajo = cached.count { it.stock <= it.stockMinimo }
            }

            val ventasRes = ventaRepository.listarVentas()
            var ventasHoyTotal = 0.0
            var numVentasHoy = 0
            var misVentasHoy = 0.0
            var ultimaVenta = 0.0

            if (ventasRes is Resource.Success && ventasRes.data != null) {
                val ventas = ventasRes.data
                val hoyVentas = ventas.filter { it.fechaVenta.startsWith(todayStr) }
                ventasHoyTotal = hoyVentas.sumOf { it.total }
                numVentasHoy = hoyVentas.size

                val currentEmail = _uiState.value.userEmail
                val currentName = _uiState.value.userName
                val misVentas = hoyVentas.filter {
                    it.usuarioNombre.equals(currentName, ignoreCase = true) ||
                    it.usuarioNombre.equals(currentEmail, ignoreCase = true)
                }
                misVentasHoy = if (misVentas.isNotEmpty()) misVentas.sumOf { it.total } else ventasHoyTotal

                val ultima = ventas.maxByOrNull { it.fechaVenta }
                ultimaVenta = ultima?.total ?: 0.0
            }

            _uiState.update {
                it.copy(
                    ventasHoyTotal = ventasHoyTotal,
                    numeroVentasHoy = numVentasHoy,
                    stockBajoCount = countStockBajo,
                    misVentasHoyTotal = misVentasHoy,
                    ultimaVentaMonto = ultimaVenta
                )
            }
        }
    }

    fun promptLogout() {
        _uiState.update { it.copy(showLogoutConfirmDialog = true) }
    }

    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutConfirmDialog = false) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLogoutConfirmDialog = false) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false, isLoggedOut = true) }
        }
    }
}
