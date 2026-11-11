package com.example.gestioninventarios.data.model

data class ReporteVentasDiaDto(
    val fecha: String,
    val totalVentas: Double,
    val numeroVentas: Int
)

data class ReporteStockBajoDto(
    val id: Int,
    val nombre: String,
    val codigoBarras: String,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaNombre: String?,
    val proveedorNombre: String?
)
