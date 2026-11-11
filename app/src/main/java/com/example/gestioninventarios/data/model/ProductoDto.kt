package com.example.gestioninventarios.data.model

data class ProductoDto(
    val id: Int = 0,
    val codigoBarras: String,
    val nombre: String,
    val precio: Double,
    val costo: Double,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaId: Int,
    val proveedorId: Int,
    val activo: Boolean = true,
    val categoriaNombre: String? = null,
    val proveedorNombre: String? = null
)
