package com.example.gestioninventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestioninventarios.data.model.CategoriaDto

@Entity(
    tableName = "categorias",
    indices = [Index(value = ["codigo"], unique = true, name = "index_categorias_codigo")]
)
data class CategoriaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String?,
    val activo: Boolean,
    @ColumnInfo(defaultValue = "0") val codigo: Int = 0
) {
    fun toDto(): CategoriaDto = CategoriaDto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        activo = activo,
        codigo = codigo
    )
}

fun CategoriaDto.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    activo = activo,
    codigo = codigo
)
