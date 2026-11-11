package com.example.gestioninventarios.data.model

data class ProveedorDto(
    val id: Int = 0,
    val nombre: String,
    val contacto: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val activo: Boolean = true,
    val codigo: Int = 0
)
