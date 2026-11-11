package com.example.gestioninventarios.ui.catalogos

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.model.CategoriaDto
import com.example.gestioninventarios.data.model.ProveedorDto
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.CategoriaRepository
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.ProveedorRepository
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogosUiState(
    val selectedTab: Int = 0, // 0 = Categorías, 1 = Proveedores
    val searchQuery: String = "",
    val categorias: List<CategoriaDto> = emptyList(),
    val proveedores: List<ProveedorDto> = emptyList(),
    val categoriasFiltradas: List<CategoriaDto> = emptyList(),
    val proveedoresFiltrados: List<ProveedorDto> = emptyList(),
    val categoriaProductCounts: Map<Int, Int> = emptyMap(),
    val proveedorProductCounts: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userRole: String = "VENDEDOR",

    // BottomSheet Categoría
    val showCategoriaBottomSheet: Boolean = false,
    val categoriaEditing: CategoriaDto? = null,
    val catNombre: String = "",
    val catDescripcion: String = "",
    val catNombreError: String? = null,
    val siguienteCodigoCategoria: Int = 1,

    // BottomSheet Proveedor
    val showProveedorBottomSheet: Boolean = false,
    val proveedorEditing: ProveedorDto? = null,
    val provNombre: String = "",
    val provContacto: String = "",
    val provTelefono: String = "",
    val provEmail: String = "",
    val provNombreError: String? = null,
    val provTelefonoError: String? = null,
    val provEmailError: String? = null,
    val siguienteCodigoProveedor: Int = 1,

    // Eliminación
    val showDeleteDialog: Boolean = false,
    val deleteItemType: String = "",
    val deleteItemId: Int? = null,
    val deleteItemName: String = "",
    val showBlockedDeleteDialog: Boolean = false,
    val blockedDeleteMessage: String = ""
)

class CatalogosViewModel(
    private val categoriaRepository: CategoriaRepository,
    private val proveedorRepository: ProveedorRepository,
    private val productoRepository: ProductoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogosUiState())
    val uiState: StateFlow<CatalogosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userRole.collectLatest { role ->
                _uiState.update { it.copy(userRole = role ?: "VENDEDOR") }
            }
        }
        cargarDatos()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index, searchQuery = "", errorMessage = null) }
        cargarDatos()
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val q = _uiState.value.searchQuery.trim().lowercase()
        val cats = _uiState.value.categorias
        val provs = _uiState.value.proveedores

        val filteredCats = if (q.isEmpty()) cats else cats.filter {
            it.nombre.lowercase().contains(q) || (it.descripcion?.lowercase()?.contains(q) == true)
        }

        val filteredProvs = if (q.isEmpty()) provs else provs.filter {
            it.nombre.lowercase().contains(q) ||
            (it.contacto?.lowercase()?.contains(q) == true) ||
            (it.telefono?.lowercase()?.contains(q) == true) ||
            (it.email?.lowercase()?.contains(q) == true)
        }

        _uiState.update {
            it.copy(
                categoriasFiltradas = filteredCats,
                proveedoresFiltrados = filteredProvs
            )
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            cargarConteosProductos()

            if (_uiState.value.selectedTab == 0) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val res = categoriaRepository.listarCategorias()
                when (res) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categorias = res.data ?: emptyList(),
                                errorMessage = null
                            )
                        }
                        aplicarFiltros()
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
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val res = proveedorRepository.listarProveedores()
                when (res) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                proveedores = res.data ?: emptyList(),
                                errorMessage = null
                            )
                        }
                        aplicarFiltros()
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
    }

    private suspend fun cargarConteosProductos() {
        val prodsRes = productoRepository.listarProductos()
        val prods = if (prodsRes is Resource.Success && prodsRes.data != null) {
            prodsRes.data
        } else {
            productoRepository.getProductosFromCache()
        }

        val catCounts = prods.groupBy { it.categoriaId }.mapValues { it.value.size }
        val provCounts = prods.groupBy { it.proveedorId }.mapValues { it.value.size }

        _uiState.update {
            it.copy(
                categoriaProductCounts = catCounts,
                proveedorProductCounts = provCounts
            )
        }
    }

    fun openCreateCategoriaBottomSheet() {
        val nextCod = (_uiState.value.categorias.maxOfOrNull { it.codigo } ?: 0) + 1
        _uiState.update {
            it.copy(
                showCategoriaBottomSheet = true,
                categoriaEditing = null,
                catNombre = "",
                catDescripcion = "",
                catNombreError = null,
                siguienteCodigoCategoria = nextCod
            )
        }
    }

    fun openEditCategoriaBottomSheet(cat: CategoriaDto) {
        _uiState.update {
            it.copy(
                showCategoriaBottomSheet = true,
                categoriaEditing = cat,
                catNombre = cat.nombre,
                catDescripcion = cat.descripcion ?: "",
                catNombreError = null
            )
        }
    }

    fun onCatNombreChange(v: String) {
        _uiState.update { it.copy(catNombre = v, catNombreError = null) }
    }

    fun onCatDescripcionChange(v: String) {
        _uiState.update { it.copy(catDescripcion = v) }
    }

    fun guardarCategoria() {
        val nombre = _uiState.value.catNombre.trim()
        val desc = _uiState.value.catDescripcion.trim().ifEmpty { null }
        val editing = _uiState.value.categoriaEditing

        if (nombre.isEmpty()) {
            _uiState.update { it.copy(catNombreError = "El nombre de la categoría es requerido") }
            return
        }

        val existeNombre = _uiState.value.categorias.any {
            it.nombre.equals(nombre, ignoreCase = true) && it.id != editing?.id
        }
        if (existeNombre) {
            _uiState.update { it.copy(catNombreError = "Ya existe una categoría con este nombre") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = if (editing == null) {
                categoriaRepository.crearCategoria(nombre, desc)
            } else {
                categoriaRepository.actualizarCategoria(editing.id, nombre, desc)
            }

            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(showCategoriaBottomSheet = false, isLoading = false) }
                    cargarDatos()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCreateProveedorBottomSheet() {
        val nextCod = (_uiState.value.proveedores.maxOfOrNull { it.codigo } ?: 0) + 1
        _uiState.update {
            it.copy(
                showProveedorBottomSheet = true,
                proveedorEditing = null,
                provNombre = "",
                provContacto = "",
                provTelefono = "",
                provEmail = "",
                provNombreError = null,
                provTelefonoError = null,
                provEmailError = null,
                siguienteCodigoProveedor = nextCod
            )
        }
    }

    fun openEditProveedorBottomSheet(prov: ProveedorDto) {
        _uiState.update {
            it.copy(
                showProveedorBottomSheet = true,
                proveedorEditing = prov,
                provNombre = prov.nombre,
                provContacto = prov.contacto ?: "",
                provTelefono = prov.telefono ?: "",
                provEmail = prov.email ?: "",
                provNombreError = null,
                provTelefonoError = null,
                provEmailError = null
            )
        }
    }

    fun onProvNombreChange(v: String) { _uiState.update { it.copy(provNombre = v, provNombreError = null) } }
    fun onProvContactoChange(v: String) { _uiState.update { it.copy(provContacto = v) } }
    fun onProvTelefonoChange(v: String) { _uiState.update { it.copy(provTelefono = v, provTelefonoError = null) } }
    fun onProvEmailChange(v: String) { _uiState.update { it.copy(provEmail = v, provEmailError = null) } }

    fun guardarProveedor() {
        val nombre = _uiState.value.provNombre.trim()
        val contacto = _uiState.value.provContacto.trim().ifEmpty { null }
        val tel = _uiState.value.provTelefono.trim().ifEmpty { null }
        val email = _uiState.value.provEmail.trim().ifEmpty { null }
        val editing = _uiState.value.proveedorEditing

        var error = false
        var nombreErr: String? = null
        var telErr: String? = null
        var emailErr: String? = null

        if (nombre.isEmpty()) {
            nombreErr = "El nombre del proveedor es requerido"
            error = true
        } else {
            val existeNombre = _uiState.value.proveedores.any {
                it.nombre.equals(nombre, ignoreCase = true) && it.id != editing?.id
            }
            if (existeNombre) {
                nombreErr = "Ya existe un proveedor con este nombre"
                error = true
            }
        }

        if (tel != null && !tel.matches(Regex("^[+0-9\\s\\-]{7,15}$"))) {
            telErr = "Ingresa un número de teléfono válido (ej: +503 7000-0000)"
            error = true
        }

        if (email != null && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Ingresa un correo electrónico válido"
            error = true
        }

        if (error) {
            _uiState.update {
                it.copy(
                    provNombreError = nombreErr,
                    provTelefonoError = telErr,
                    provEmailError = emailErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = if (editing == null) {
                proveedorRepository.crearProveedor(nombre, contacto, tel, email)
            } else {
                proveedorRepository.actualizarProveedor(editing.id, nombre, contacto, tel, email)
            }

            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(showProveedorBottomSheet = false, isLoading = false) }
                    cargarDatos()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun promptDelete(type: String, id: Int, name: String) {
        val count = if (type == "categoria") {
            _uiState.value.categoriaProductCounts[id] ?: 0
        } else {
            _uiState.value.proveedorProductCounts[id] ?: 0
        }

        if (count > 0) {
            _uiState.update {
                it.copy(
                    showBlockedDeleteDialog = true,
                    blockedDeleteMessage = "No se puede eliminar ${if (type == "categoria") "la categoría" else "el proveedor"} '$name' porque tiene $count ${if (count == 1) "producto asociado" else "productos asociados"}. Reasigna o elimina los productos primero."
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    showDeleteDialog = true,
                    deleteItemType = type,
                    deleteItemId = id,
                    deleteItemName = name
                )
            }
        }
    }

    fun dismissBlockedDeleteDialog() {
        _uiState.update { it.copy(showBlockedDeleteDialog = false, blockedDeleteMessage = "") }
    }

    fun confirmDelete() {
        val type = _uiState.value.deleteItemType
        val id = _uiState.value.deleteItemId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }
            val res = if (type == "categoria") {
                categoriaRepository.eliminarCategoria(id)
            } else {
                proveedorRepository.eliminarProveedor(id)
            }

            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    cargarDatos()
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
            it.copy(
                showCategoriaBottomSheet = false,
                showProveedorBottomSheet = false,
                showDeleteDialog = false,
                showBlockedDeleteDialog = false,
                errorMessage = null
            )
        }
    }
}
