package com.example.gestioninventarios.data.model

data class UsuarioResponseDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)

data class UsuarioCreateDto(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
)
