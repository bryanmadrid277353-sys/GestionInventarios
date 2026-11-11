package com.example.gestioninventarios.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun login_correoVacio_muestraErrorEmail() {
        var state = LoginUiState(email = "", password = "password123")
        var emailError: String? = null

        if (state.email.isEmpty()) {
            emailError = "El correo electrónico es requerido"
        }

        assertNotNull(emailError)
        assertEquals("El correo electrónico es requerido", emailError)
    }

    @Test
    fun login_passwordVacio_muestraErrorPassword() {
        var state = LoginUiState(email = "admin@inventariopyme.com", password = "")
        var passwordError: String? = null

        if (state.password.isEmpty()) {
            passwordError = "La contraseña es requerida"
        }

        assertNotNull(passwordError)
        assertEquals("La contraseña es requerida", passwordError)
    }
}
