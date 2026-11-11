package com.example.gestioninventarios.ui.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.model.CategoriaDto
import com.example.gestioninventarios.data.model.ProductoDto
import com.example.gestioninventarios.data.model.ProveedorDto
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.CategoriaRepository
import com.example.gestioninventarios.data.repository.ProductoCreateDto
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.ProductoUpdateDto
import com.example.gestioninventarios.data.repository.ProveedorRepository
import com.example.gestioninventarios.util.Ean13Validator
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrdenProducto {
    NOMBRE,
    STOCK_ASC,
    PRECIO_ASC,
    PRECIO_DESC
}

data class ProductosUiState(
    val searchQuery: String = "",
    val selectedCategoriaFilterId: Int? = null,
    val soloStockBajoFilter: Boolean = false,
    val ordenFilter: OrdenProducto = OrdenProducto.NOMBRE,

    val todosProductos: List<ProductoDto> = emptyList(),
    val productosFiltrados: List<ProductoDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val proveedores: List<ProveedorDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val userRole: String = "VENDEDOR",

    val showProductBottomSheet: Boolean = false,
    val productoEditing: ProductoDto? = null,
    val codigoBarras: String = "",
    val nombre: String = "",
    val precio: String = "",
    val costo: String = "",
    val stock: String = "",
    val stockMinimo: String = "5",
    val selectedCategoriaId: Int? = null,
    val selectedProveedorId: Int? = null,

    val codigoBarrasError: String? = null,
    val nombreError: String? = null,
    val precioError: String? = null,
    val costoError: String? = null,
    val stockError: String? = null,
    val categoriaError: String? = null,
    val proveedorError: String? = null,

    val showDeleteDialog: Boolean = false,
    val deleteProductId: Int? = null,
    val deleteProductName: String = "",
    val showUnsavedChangesDialog: Boolean = false
)

class ProductosViewModel(
    private val productoRepository: ProductoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val proveedorRepository: ProveedorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userRole.collectLatest { role ->
                _uiState.update { it.copy(userRole = role ?: "VENDEDOR") }
            }
        }
        cargarAuxiliares()
        cargarProductos()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        aplicarFiltrosYOrden()
    }

    fun selectCategoriaFilter(catId: Int?) {
        _uiState.update { it.copy(selectedCategoriaFilterId = catId) }
        aplicarFiltrosYOrden()
    }

    fun toggleSoloStockBajoFilter() {
        _uiState.update { it.copy(soloStockBajoFilter = !it.soloStockBajoFilter) }
        aplicarFiltrosYOrden()
    }

    fun selectOrdenFilter(orden: OrdenProducto) {
        _uiState.update { it.copy(ordenFilter = orden) }
        aplicarFiltrosYOrden()
    }

    private fun aplicarFiltrosYOrden() {
        val state = _uiState.value
        var lista = state.todosProductos

        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.lowercase()
            lista = lista.filter {
                it.nombre.lowercase().contains(q) || it.codigoBarras.lowercase().contains(q)
            }
        }

        if (state.selectedCategoriaFilterId != null) {
            lista = lista.filter { it.categoriaId == state.selectedCategoriaFilterId }
        }

        if (state.soloStockBajoFilter) {
            lista = lista.filter { it.stock <= it.stockMinimo }
        }

        lista = when (state.ordenFilter) {
            OrdenProducto.NOMBRE -> lista.sortedBy { it.nombre.lowercase() }
            OrdenProducto.STOCK_ASC -> lista.sortedBy { it.stock }
            OrdenProducto.PRECIO_ASC -> lista.sortedBy { it.precio }
            OrdenProducto.PRECIO_DESC -> lista.sortedByDescending { it.precio }
        }

        _uiState.update { it.copy(productosFiltrados = lista) }
    }

    fun cargarAuxiliares() {
        viewModelScope.launch {
            val catRes = categoriaRepository.listarCategorias()
            if (catRes is Resource.Success) {
                _uiState.update { it.copy(categorias = catRes.data ?: emptyList()) }
            }
            val provRes = proveedorRepository.listarProveedores()
            if (provRes is Resource.Success) {
                _uiState.update { it.copy(proveedores = provRes.data ?: emptyList()) }
            }
        }
    }

    fun cargarProductos(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = productoRepository.listarProductos()
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            todosProductos = res.data ?: emptyList(),
                            errorMessage = null
                        )
                    }
                    aplicarFiltrosYOrden()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = res.message
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCreateDialog() {
        cargarAuxiliares()
        _uiState.update {
            it.copy(
                showProductBottomSheet = true,
                productoEditing = null,
                codigoBarras = "",
                nombre = "",
                precio = "",
                costo = "",
                stock = "0",
                stockMinimo = "5",
                selectedCategoriaId = it.categorias.firstOrNull()?.id,
                selectedProveedorId = it.proveedores.firstOrNull()?.id,
                codigoBarrasError = null,
                nombreError = null,
                precioError = null,
                costoError = null,
                stockError = null,
                categoriaError = null,
                proveedorError = null
            )
        }
    }

    fun openEditDialog(prod: ProductoDto) {
        cargarAuxiliares()
        _uiState.update {
            it.copy(
                showProductBottomSheet = true,
                productoEditing = prod,
                codigoBarras = prod.codigoBarras,
                nombre = prod.nombre,
                precio = prod.precio.toString(),
                costo = prod.costo.toString(),
                stock = prod.stock.toString(),
                stockMinimo = prod.stockMinimo.toString(),
                selectedCategoriaId = prod.categoriaId,
                selectedProveedorId = prod.proveedorId,
                codigoBarrasError = null,
                nombreError = null,
                precioError = null,
                costoError = null,
                stockError = null,
                categoriaError = null,
                proveedorError = null
            )
        }
    }

    fun onCodigoBarrasChange(v: String) { _uiState.update { it.copy(codigoBarras = v, codigoBarrasError = null) } }
    fun onNombreChange(v: String) { _uiState.update { it.copy(nombre = v, nombreError = null) } }
    fun onPrecioChange(v: String) { _uiState.update { it.copy(precio = v, precioError = null) } }
    fun onCostoChange(v: String) { _uiState.update { it.copy(costo = v, costoError = null) } }
    fun onStockChange(v: String) { _uiState.update { it.copy(stock = v, stockError = null) } }
    fun onStockMinimoChange(v: String) { _uiState.update { it.copy(stockMinimo = v) } }
    fun onCategoriaSelected(id: Int) { _uiState.update { it.copy(selectedCategoriaId = id, categoriaError = null) } }
    fun onProveedorSelected(id: Int) { _uiState.update { it.copy(selectedProveedorId = id, proveedorError = null) } }

    fun isFormModified(): Boolean {
        val state = _uiState.value
        val prod = state.productoEditing
        if (prod == null) {
            return state.codigoBarras.isNotBlank() || state.nombre.isNotBlank() || state.precio.isNotBlank()
        }
        return state.codigoBarras != prod.codigoBarras ||
               state.nombre != prod.nombre ||
               state.precio != prod.precio.toString() ||
               state.costo != prod.costo.toString() ||
               state.stock != prod.stock.toString() ||
               state.stockMinimo != prod.stockMinimo.toString() ||
               state.selectedCategoriaId != prod.categoriaId ||
               state.selectedProveedorId != prod.proveedorId
    }

    fun requestCloseForm() {
        if (isFormModified()) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            closeFormDirectly()
        }
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    fun closeFormDirectly() {
        _uiState.update {
            it.copy(
                showProductBottomSheet = false,
                showUnsavedChangesDialog = false,
                productoEditing = null
            )
        }
    }

    fun guardarProducto() {
        val code = _uiState.value.codigoBarras.trim()
        val nombre = _uiState.value.nombre.trim()
        val precioVal = _uiState.value.precio.toDoubleOrNull()
        val costoVal = _uiState.value.costo.toDoubleOrNull()
        val stockVal = _uiState.value.stock.toIntOrNull()
        val stockMinVal = _uiState.value.stockMinimo.toIntOrNull() ?: 5
        val catId = _uiState.value.selectedCategoriaId
        val provId = _uiState.value.selectedProveedorId

        var hasError = false
        var codeErr: String? = null
        var nombreErr: String? = null
        var precioErr: String? = null
        var costoErr: String? = null
        var stockErr: String? = null
        var catErr: String? = null
        var provErr: String? = null

        if (code.isEmpty()) {
            codeErr = "El código de barras es requerido"
            hasError = true
        } else if (!Ean13Validator.isValidEan13(code)) {
            codeErr = "Código no válido. Si es de 13 dígitos debe cumplir con EAN-13"
            hasError = true
        }

        if (nombre.isEmpty()) { nombreErr = "El nombre es requerido"; hasError = true }

        if (precioVal == null || precioVal <= 0) {
            precioErr = "El precio debe ser mayor a 0"
            hasError = true
        }

        if (costoVal == null || costoVal < 0) {
            costoErr = "El costo debe ser mayor o igual a 0"
            hasError = true
        } else if (precioVal != null && precioVal < costoVal) {
            precioErr = "El precio no puede ser menor al costo"
            hasError = true
        }

        if (stockVal == null || stockVal < 0) {
            stockErr = "El stock debe ser entero >= 0"
            hasError = true
        }

        if (catId == null) { catErr = "Selecciona una categoría"; hasError = true }
        if (provId == null) { provErr = "Selecciona un proveedor"; hasError = true }

        if (hasError) {
            _uiState.update {
                it.copy(
                    codigoBarrasError = codeErr,
                    nombreError = nombreErr,
                    precioError = precioErr,
                    costoError = costoErr,
                    stockError = stockErr,
                    categoriaError = catErr,
                    proveedorError = provErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val editing = _uiState.value.productoEditing

            val res = if (editing == null) {
                val createDto = ProductoCreateDto(
                    codigoBarras = code,
                    nombre = nombre,
                    precio = precioVal!!,
                    costo = costoVal!!,
                    stock = stockVal!!,
                    stockMinimo = stockMinVal,
                    categoriaId = catId!!,
                    proveedorId = provId!!
                )
                productoRepository.crearProducto(createDto)
            } else {
                val updateDto = ProductoUpdateDto(
                    codigoBarras = code,
                    nombre = nombre,
                    precio = precioVal!!,
                    costo = costoVal!!,
                    stock = stockVal!!,
                    stockMinimo = stockMinVal,
                    categoriaId = catId!!,
                    proveedorId = provId!!
                )
                productoRepository.actualizarProducto(editing.id, updateDto)
            }

            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(showProductBottomSheet = false, isLoading = false) }
                    cargarProductos()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun promptDelete(id: Int, name: String) {
        _uiState.update {
            it.copy(showDeleteDialog = true, deleteProductId = id, deleteProductName = name)
        }
    }

    fun confirmDelete() {
        val id = _uiState.value.deleteProductId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            val res = productoRepository.eliminarProducto(id)
            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    cargarProductos()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun closeDialogs() {
        _uiState.update {
            it.copy(showProductBottomSheet = false, showDeleteDialog = false, showUnsavedChangesDialog = false, errorMessage = null)
        }
    }
}
