package com.example.gestioninventarios.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestioninventarios.data.model.ProductoDto

@Entity(
    tableName = "productos",
    indices = [
        Index(value = ["codigoBarras"], name = "index_productos_codigoBarras"),
        Index(value = ["categoriaId"], name = "index_productos_categoriaId"),
        Index(value = ["proveedorId"], name = "index_productos_proveedorId"),
        Index(value = ["activo"], name = "index_productos_activo")
    ]
)
data class ProductoEntity(
    @PrimaryKey val id: Int,
    val codigoBarras: String,
    val nombre: String,
    val precio: Double,
    val costo: Double,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaId: Int,
    val proveedorId: Int,
    val activo: Boolean,
    val categoriaNombre: String?,
    val proveedorNombre: String?
) {
    fun toDto(): ProductoDto = ProductoDto(
        id = id,
        codigoBarras = codigoBarras,
        nombre = nombre,
        precio = precio,
        costo = costo,
        stock = stock,
        stockMinimo = stockMinimo,
        categoriaId = categoriaId,
        proveedorId = proveedorId,
        activo = activo,
        categoriaNombre = categoriaNombre,
        proveedorNombre = proveedorNombre
    )
}

fun ProductoDto.toEntity(): ProductoEntity = ProductoEntity(
    id = id,
    codigoBarras = codigoBarras,
    nombre = nombre,
    precio = precio,
    costo = costo,
    stock = stock,
    stockMinimo = stockMinimo,
    categoriaId = categoriaId,
    proveedorId = proveedorId,
    activo = activo,
    categoriaNombre = categoriaNombre,
    proveedorNombre = proveedorNombre
)
