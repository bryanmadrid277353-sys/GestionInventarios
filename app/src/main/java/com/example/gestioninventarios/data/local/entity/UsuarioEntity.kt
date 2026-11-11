package com.example.gestioninventarios.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestioninventarios.data.model.UsuarioResponseDto

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true, name = "index_usuarios_email")]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val rol: String,
    val activo: Boolean = true
) {
    fun toDto(): UsuarioResponseDto = UsuarioResponseDto(
        id = id,
        nombre = nombre,
        email = email,
        rol = rol,
        activo = activo
    )
}
