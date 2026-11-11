package com.example.gestioninventarios.data.model

data class DetalleVentaCreateDto(
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double
)

data class DetalleVentaResponseDto(
    val id: Int,
    val productoId: Int,
    val productoNombre: String,
    val codigoBarras: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)

data class VentaResponseDto(
    val id: Int,
    val usuarioId: Int,
    val usuarioNombre: String,
    val fechaVenta: String,
    val total: Double,
    val detalles: List<DetalleVentaResponseDto> = emptyList()
)
