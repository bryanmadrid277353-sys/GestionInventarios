package com.example.gestioninventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import com.example.gestioninventarios.data.local.entity.VentaWithDetalles

@Dao
interface VentaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVenta(venta: VentaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDetalles(detalles: List<DetalleVentaEntity>)

    @Transaction
    @Query("SELECT * FROM ventas ORDER BY fechaVenta DESC")
    fun getVentasWithDetalles(): List<VentaWithDetalles>

    @Query("SELECT * FROM ventas ORDER BY id ASC")
    fun getAllVentas(): List<VentaEntity>

    @Query("SELECT * FROM detalle_ventas ORDER BY id ASC")
    fun getAllDetalles(): List<DetalleVentaEntity>

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :id LIMIT 1")
    fun getVentaWithDetallesById(id: Int): VentaWithDetalles?

    @Query("DELETE FROM ventas")
    fun clearAll()
}
