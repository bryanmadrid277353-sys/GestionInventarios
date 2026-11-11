package com.example.gestioninventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestioninventarios.data.local.entity.ProveedorEntity

@Dao
interface ProveedorDao {
    @Query("SELECT * FROM proveedores WHERE activo = 1 ORDER BY nombre ASC")
    fun getProveedores(): List<ProveedorEntity>

    @Query("SELECT * FROM proveedores ORDER BY id ASC")
    fun getAllProveedores(): List<ProveedorEntity>

    @Query("SELECT * FROM proveedores WHERE id = :id LIMIT 1")
    fun getProveedorById(id: Int): ProveedorEntity?

    @Query("SELECT MAX(codigo) FROM proveedores")
    fun getMaxCodigo(): Int?

    @Query("SELECT MAX(id) FROM proveedores")
    fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProveedor(proveedor: ProveedorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(proveedores: List<ProveedorEntity>)

    @Update
    fun updateProveedor(proveedor: ProveedorEntity)

    @Query("UPDATE proveedores SET activo = 0 WHERE id = :id")
    fun softDeleteProveedor(id: Int)

    @Query("DELETE FROM proveedores")
    fun clearAll()
}
