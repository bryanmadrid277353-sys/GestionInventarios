package com.example.gestioninventarios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gestioninventarios.data.model.ProveedorDto

@Entity(
    tableName = "proveedores",
    indices = [Index(value = ["codigo"], unique = true, name = "index_proveedores_codigo")]
)
data class ProveedorEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val contacto: String?,
    val telefono: String?,
    val email: String?,
    val activo: Boolean,
    @ColumnInfo(defaultValue = "0") val codigo: Int = 0
) {
    fun toDto(): ProveedorDto = ProveedorDto(
        id = id,
        nombre = nombre,
        contacto = contacto,
        telefono = telefono,
        email = email,
        activo = activo,
        codigo = codigo
    )
}

fun ProveedorDto.toEntity(): ProveedorEntity = ProveedorEntity(
    id = id,
    nombre = nombre,
    contacto = contacto,
    telefono = telefono,
    email = email,
    activo = activo,
    codigo = codigo
)
