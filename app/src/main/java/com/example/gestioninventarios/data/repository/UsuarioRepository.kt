package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.dao.UsuarioDao
import com.example.gestioninventarios.data.local.entity.UsuarioEntity
import com.example.gestioninventarios.data.model.UsuarioCreateDto
import com.example.gestioninventarios.data.model.UsuarioResponseDto
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun listarUsuarios(): Resource<List<UsuarioResponseDto>> = withContext(Dispatchers.IO) {
        try {
            val users = usuarioDao.getUsuarios().map { it.toDto() }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al listar usuarios")
        }
    }

    suspend fun crearUsuario(createDto: UsuarioCreateDto): Resource<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        try {
            val existing = usuarioDao.getUsuarioByEmail(createDto.email.trim())
            if (existing != null) {
                return@withContext Resource.Error("El correo electrónico ya está registrado")
            }

            val newId = (usuarioDao.getMaxId() ?: 0) + 1
            val entity = UsuarioEntity(
                id = newId,
                nombre = createDto.nombre.trim(),
                email = createDto.email.trim(),
                passwordHash = createDto.password,
                rol = createDto.rol,
                activo = true
            )
            usuarioDao.insertUsuario(entity)
            Resource.Success(entity.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al registrar el usuario")
        }
    }

    suspend fun desactivarUsuario(id: Int): Resource<UsuarioResponseDto> = withContext(Dispatchers.IO) {
        try {
            val user = usuarioDao.getUsuarioById(id)
                ?: return@withContext Resource.Error("Usuario no encontrado")
            val nuevoEstado = !user.activo
            val updated = user.copy(activo = nuevoEstado)
            usuarioDao.updateUsuario(updated)
            Resource.Success(updated.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al cambiar estado del usuario")
        }
    }
}
