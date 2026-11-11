package com.example.gestioninventarios.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun resetForm() {
        _uiState.update {
            LoginUiState()
        }
    }

    fun setErrorMessage(msg: String?) {
        _uiState.update { it.copy(errorMessage = msg) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun login() {
        val currentEmail = _uiState.value.email.trim()
        val currentPassword = _uiState.value.password

        var hasError = false
        var emailErr: String? = null
        var passwordErr: String? = null

        if (currentEmail.isEmpty()) {
            emailErr = "El correo electrónico es requerido"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            emailErr = "Ingresa un correo electrónico válido"
            hasError = true
        }

        if (currentPassword.isEmpty()) {
            passwordErr = "La contraseña es requerida"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(emailError = emailErr, passwordError = passwordErr)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(currentEmail, currentPassword)
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Error al iniciar sesión"
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
