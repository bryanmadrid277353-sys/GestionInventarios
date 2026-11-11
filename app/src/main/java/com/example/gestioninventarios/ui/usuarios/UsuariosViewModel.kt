package com.example.gestioninventarios.ui.usuarios

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.model.UsuarioCreateDto
import com.example.gestioninventarios.data.model.UsuarioResponseDto
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.UsuarioRepository
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuariosUiState(
    val usuarios: List<UsuarioResponseDto> = emptyList(),
    val usuariosFiltrados: List<UsuarioResponseDto> = emptyList(),
    val filtroActivosSolo: Boolean = false,
    val currentEmail: String = "",
    val currentRole: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val showUserBottomSheet: Boolean = false,
    val usuarioEditing: UsuarioResponseDto? = null,
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val rol: String = "VENDEDOR",

    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,

    val showToggleStatusConfirmDialog: Boolean = false,
    val targetUserForToggle: UsuarioResponseDto? = null,

    val showBlockedDialog: Boolean = false,
    val blockedDialogMessage: String = ""
)

class UsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.userEmail.collectLatest { email ->
                _uiState.update { it.copy(currentEmail = email ?: "") }
            }
        }
        viewModelScope.launch {
            authRepository.userRole.collectLatest { role ->
                _uiState.update { it.copy(currentRole = role ?: "") }
            }
        }
        cargarUsuarios()
    }

    fun toggleFiltroActivosSolo() {
        _uiState.update { it.copy(filtroActivosSolo = !it.filtroActivosSolo) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val list = _uiState.value.usuarios
        val filtered = if (_uiState.value.filtroActivosSolo) {
            list.filter { it.activo }
        } else list

        _uiState.update { it.copy(usuariosFiltrados = filtered) }
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val res = usuarioRepository.listarUsuarios()
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            usuarios = res.data ?: emptyList(),
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

    fun openCreateUserBottomSheet() {
        _uiState.update {
            it.copy(
                showUserBottomSheet = true,
                usuarioEditing = null,
                nombre = "",
                email = "",
                password = "",
                rol = "VENDEDOR",
                nombreError = null,
                emailError = null,
                passwordError = null
            )
        }
    }

    fun onNombreChange(v: String) { _uiState.update { it.copy(nombre = v, nombreError = null) } }
    fun onEmailChange(v: String) { _uiState.update { it.copy(email = v, emailError = null) } }
    fun onPasswordChange(v: String) { _uiState.update { it.copy(password = v, passwordError = null) } }
    fun onRolChange(v: String) { _uiState.update { it.copy(rol = v) } }

    fun guardarUsuario() {
        val nom = _uiState.value.nombre.trim()
        val em = _uiState.value.email.trim()
        val pass = _uiState.value.password.trim()
        val r = _uiState.value.rol

        var hasErr = false
        var nomErr: String? = null
        var emErr: String? = null
        var passErr: String? = null

        if (nom.isEmpty()) { nomErr = "El nombre es requerido"; hasErr = true }

        if (em.isEmpty()) {
            emErr = "El correo electrónico es requerido"
            hasErr = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            emErr = "Ingresa un correo electrónico válido"
            hasErr = true
        }

        if (pass.isEmpty()) {
            passErr = "La contraseña es requerida"
            hasErr = true
        } else if (pass.length < 6) {
            passErr = "La contraseña debe tener al menos 6 caracteres"
            hasErr = true
        }

        if (hasErr) {
            _uiState.update {
                it.copy(nombreError = nomErr, emailError = emErr, passwordError = passErr)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dto = UsuarioCreateDto(nombre = nom, email = em, password = pass, rol = r)
            val res = usuarioRepository.crearUsuario(dto)
            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(showUserBottomSheet = false, isLoading = false) }
                    cargarUsuarios()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun promptToggleStatus(usr: UsuarioResponseDto) {
        if (usr.email.equals(_uiState.value.currentEmail, ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    showBlockedDialog = true,
                    blockedDialogMessage = "No puedes cambiar el estado de tu propia cuenta de usuario."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showToggleStatusConfirmDialog = true,
                targetUserForToggle = usr
            )
        }
    }

    fun confirmToggleStatus() {
        val target = _uiState.value.targetUserForToggle ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showToggleStatusConfirmDialog = false) }
            val res = usuarioRepository.desactivarUsuario(target.id)
            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    cargarUsuarios()
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
                showUserBottomSheet = false,
                showToggleStatusConfirmDialog = false,
                showBlockedDialog = false,
                errorMessage = null
            )
        }
    }
}
