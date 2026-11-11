package com.example.gestioninventarios.data.model

data class CategoriaDto(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true,
    val codigo: Int = 0
)
