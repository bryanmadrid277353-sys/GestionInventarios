package com.example.gestioninventarios.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gestioninventarios.data.model.VentaResponseDto

data class VentaWithDetalles(
    @Embedded val venta: VentaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ventaId"
    )
    val detalles: List<DetalleVentaEntity>
) {
    fun toDto(): VentaResponseDto = VentaResponseDto(
        id = venta.id,
        usuarioId = venta.usuarioId,
        usuarioNombre = venta.usuarioNombre,
        fechaVenta = venta.fechaVenta,
        total = venta.total,
        detalles = detalles.map { it.toDto() }
    )
}
