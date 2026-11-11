package com.example.gestioninventarios.ui.venta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.model.DetalleVentaCreateDto
import com.example.gestioninventarios.data.model.ProductoDto
import com.example.gestioninventarios.data.model.VentaResponseDto
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.VentaRepository
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface VentaEvento {
    data class ProductoQuitado(
        val item: CartItem,
        val posicion: Int
    ) : VentaEvento
}

data class CartItem(
    val producto: ProductoDto,
    val cantidad: Int
) {
    val subtotal: Double
        get() = producto.precio * cantidad
}

data class VentaUiState(
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val sugerencias: List<ProductoDto> = emptyList(),
    val isSearchingSuggestions: Boolean = false,
    val cart: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showClearCartDialog: Boolean = false,

    val showCobroBottomSheet: Boolean = false,
    val montoRecibidoText: String = "",
    val cambioCalculado: Double = 0.0,

    val successSale: VentaResponseDto? = null,
    val successSaleCambio: Double = 0.0,

    val userRole: String = "",
    val userName: String = "",
    val userEmail: String = "",

    val ventasHistorial: List<VentaResponseDto> = emptyList(),
    val filtroVendedor: String = "TODOS",
    val listaVendedores: List<String> = emptyList(),
    val selectedVentaDetail: VentaResponseDto? = null,

    val unknownBarcodeScanned: String? = null
)

class VentaViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaUiState())
    val uiState: StateFlow<VentaUiState> = _uiState.asStateFlow()

    private val _eventos = Channel<VentaEvento>(Channel.BUFFERED)
    val eventos: Flow<VentaEvento> = _eventos.receiveAsFlow()

    private var todasVentasHistorial: List<VentaResponseDto> = emptyList()

    init {
        viewModelScope.launch {
            authRepository.userRole.collectLatest { r ->
                _uiState.update { it.copy(userRole = r ?: "VENDEDOR") }
                aplicarFiltroHistorial()
            }
        }
        viewModelScope.launch {
            authRepository.userName.collectLatest { n ->
                _uiState.update { it.copy(userName = n ?: "Usuario") }
                aplicarFiltroHistorial()
            }
        }
        viewModelScope.launch {
            authRepository.userEmail.collectLatest { e ->
                _uiState.update { it.copy(userEmail = e ?: "") }
                aplicarFiltroHistorial()
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == 1) {
            cargarHistorialVentas()
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        if (q.trim().isEmpty()) {
            _uiState.update { it.copy(sugerencias = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingSuggestions = true) }
            val prods = productoRepository.getProductosFromCache(search = q.trim())
            _uiState.update {
                it.copy(
                    isSearchingSuggestions = false,
                    sugerencias = prods.take(6)
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", sugerencias = emptyList()) }
    }

    fun selectProductoDeSugerencia(prod: ProductoDto) {
        agregarAlCarrito(prod)
        clearSearch()
    }

    fun onBarcodeScanned(codigo: String) {
        val cleanCode = codigo.trim()
        if (cleanCode.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = productoRepository.obtenerPorCodigoBarras(cleanCode)
            _uiState.update { it.copy(isLoading = false) }

            if (res is Resource.Success && res.data != null) {
                agregarAlCarrito(res.data)
            } else {
                _uiState.update { it.copy(unknownBarcodeScanned = cleanCode) }
            }
        }
    }

    fun dismissUnknownBarcodeDialog() {
        _uiState.update { it.copy(unknownBarcodeScanned = null) }
    }

    fun agregarAlCarrito(prod: ProductoDto) {
        val currentCart = _uiState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.producto.id == prod.id }

        if (index >= 0) {
            val existing = currentCart[index]
            if (existing.cantidad < prod.stock) {
                currentCart[index] = existing.copy(cantidad = existing.cantidad + 1)
            } else {
                _uiState.update { it.copy(errorMessage = "Stock máximo alcanzado para ${prod.nombre}") }
                return
            }
        } else {
            if (prod.stock > 0) {
                currentCart.add(CartItem(prod, 1))
            } else {
                _uiState.update { it.copy(errorMessage = "El producto ${prod.nombre} está agotado") }
                return
            }
        }

        recalcularTotales(currentCart)
    }

    fun incrementQuantity(prodId: Int) {
        val currentCart = _uiState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.producto.id == prodId }
        if (index >= 0) {
            val item = currentCart[index]
            if (item.cantidad < item.producto.stock) {
                currentCart[index] = item.copy(cantidad = item.cantidad + 1)
                recalcularTotales(currentCart)
            }
        }
    }

    fun decrementQuantity(prodId: Int) {
        val currentCart = _uiState.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.producto.id == prodId }
        if (index >= 0) {
            val item = currentCart[index]
            if (item.cantidad > 1) {
                currentCart[index] = item.copy(cantidad = item.cantidad - 1)
                recalcularTotales(currentCart)
            } else {
                removeCartItemWithSnackbar(item)
            }
        }
    }

    fun removeCartItemWithSnackbar(item: CartItem) {
        val currentCart = _uiState.value.cart.toMutableList()
        val pos = currentCart.indexOfFirst { it.producto.id == item.producto.id }
        if (pos >= 0) {
            currentCart.removeAt(pos)
            recalcularTotales(currentCart)
            _eventos.trySend(VentaEvento.ProductoQuitado(item, pos))
        }
    }

    fun restaurarItem(item: CartItem, posicion: Int) {
        val currentCart = _uiState.value.cart.toMutableList()
        val safePos = posicion.coerceIn(0, currentCart.size)
        currentCart.add(safePos, item)
        recalcularTotales(currentCart)
    }

    fun promptClearCart() {
        _uiState.update { it.copy(showClearCartDialog = true) }
    }

    fun dismissClearCartDialog() {
        _uiState.update { it.copy(showClearCartDialog = false) }
    }

    fun confirmClearCart() {
        recalcularTotales(emptyList())
        _uiState.update { it.copy(showClearCartDialog = false) }
    }

    private fun recalcularTotales(cartList: List<CartItem>) {
        val totalCalculado = cartList.sumOf { it.subtotal }
        _uiState.update {
            it.copy(
                cart = cartList,
                total = totalCalculado,
                errorMessage = null
            )
        }
    }

    fun openCobroBottomSheet() {
        if (_uiState.value.cart.isEmpty()) return
        val totalVal = _uiState.value.total
        _uiState.update {
            it.copy(
                showCobroBottomSheet = true,
                montoRecibidoText = String.format(Locale.US, "%.2f", totalVal),
                cambioCalculado = 0.0
            )
        }
    }

    fun closeCobroBottomSheet() {
        _uiState.update { it.copy(showCobroBottomSheet = false) }
    }

    fun onMontoRecibidoChanged(text: String) {
        val totalVal = _uiState.value.total
        val recibidoVal = text.toDoubleOrNull() ?: 0.0
        val cambio = recibidoVal - totalVal
        _uiState.update {
            it.copy(
                montoRecibidoText = text,
                cambioCalculado = cambio
            )
        }
    }

    fun selectBilleteRapido(monto: Double) {
        onMontoRecibidoChanged(monto.toString())
    }

    fun confirmarVenta() {
        val cartItems = _uiState.value.cart
        if (cartItems.isEmpty()) return

        val detallesDto = cartItems.map {
            DetalleVentaCreateDto(
                productoId = it.producto.id,
                cantidad = it.cantidad,
                precioUnitario = it.producto.precio
            )
        }

        val cambio = _uiState.value.cambioCalculado

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = ventaRepository.crearVenta(detallesDto)
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showCobroBottomSheet = false,
                            cart = emptyList(),
                            total = 0.0,
                            successSale = res.data,
                            successSaleCambio = cambio
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message ?: "Error al registrar la venta"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(successSale = null, successSaleCambio = 0.0) }
    }

    // --- HISTORIAL ---
    fun cargarHistorialVentas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = ventaRepository.listarVentas()
            when (res) {
                is Resource.Success -> {
                    todasVentasHistorial = res.data ?: emptyList()
                    val vendedores = todasVentasHistorial.map { it.usuarioNombre }.distinct().sorted()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            listaVendedores = vendedores
                        )
                    }
                    aplicarFiltroHistorial()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun selectFiltroVendedor(vend: String) {
        _uiState.update { it.copy(filtroVendedor = vend) }
        aplicarFiltroHistorial()
    }

    private fun aplicarFiltroHistorial() {
        val state = _uiState.value
        var list = todasVentasHistorial

        if (state.userRole == "VENDEDOR") {
            list = list.filter {
                it.usuarioNombre.equals(state.userName, ignoreCase = true) ||
                it.usuarioNombre.equals(state.userEmail, ignoreCase = true)
            }
        } else if (state.filtroVendedor != "TODOS") {
            list = list.filter {
                it.usuarioNombre.equals(state.filtroVendedor, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(ventasHistorial = list) }
    }

    fun openVentaDetailModal(venta: VentaResponseDto) {
        _uiState.update { it.copy(selectedVentaDetail = venta) }
    }

    fun closeVentaDetailModal() {
        _uiState.update { it.copy(selectedVentaDetail = null) }
    }
}
