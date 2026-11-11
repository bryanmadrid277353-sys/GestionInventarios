package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.TokenStore
import com.example.gestioninventarios.data.local.dao.UsuarioDao
import com.example.gestioninventarios.data.model.UsuarioResponseDto
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val tokenStore: TokenStore
) {

    val isLoggedIn: Flow<Boolean> = tokenStore.isLoggedIn
    val userRole: Flow<String?> = tokenStore.userRole
    val userName: Flow<String?> = tokenStore.userName
    val userEmail: Flow<String?> = tokenStore.userEmail

    suspend fun login(email: String, password: String): Resource<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        val user = usuarioDao.getUsuarioByEmail(email.trim())
        if (user == null || !user.activo) {
            return@withContext Resource.Error("Usuario no encontrado o inactivo")
        }

        if (user.passwordHash == password) {
            tokenStore.saveSession(user.id, user.nombre, user.email, user.rol)
            Resource.Success(user.toDto())
        } else {
            Resource.Error("Contraseña incorrecta")
        }
    }

    suspend fun logout(): Resource<Unit> {
        tokenStore.clearSession()
        return Resource.Success(Unit)
    }

    suspend fun cleanupLegacyData() {
        tokenStore.cleanupLegacyData()
    }
}
