package com.example.gestioninventarios.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestioninventarios.data.model.DetalleVentaResponseDto
import com.example.gestioninventarios.data.model.VentaResponseDto

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val usuarioNombre: String,
    val fechaVenta: String,
    val total: Double
)

@Entity(
    tableName = "detalle_ventas",
    foreignKeys = [
        ForeignKey(
            entity = VentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ventaId"], name = "index_detalle_ventas_ventaId")]
)
data class DetalleVentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ventaId: Int,
    val productoId: Int,
    val productoNombre: String,
    val codigoBarras: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
) {
    fun toDto(): DetalleVentaResponseDto = DetalleVentaResponseDto(
        id = id,
        productoId = productoId,
        productoNombre = productoNombre,
        codigoBarras = codigoBarras,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        subtotal = subtotal
    )
}
