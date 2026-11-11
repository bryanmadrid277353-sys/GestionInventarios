package com.example.gestioninventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestioninventarios.data.local.entity.ProductoEntity

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getProductos(): List<ProductoEntity>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    fun getProductoById(id: Int): ProductoEntity?

    @Query("SELECT * FROM productos WHERE codigoBarras = :barcode AND activo = 1 LIMIT 1")
    fun getProductoByBarcode(barcode: String): ProductoEntity?

    @Query("SELECT COUNT(*) FROM productos WHERE categoriaId = :categoriaId AND activo = 1")
    fun getCountByCategoria(categoriaId: Int): Int

    @Query("SELECT COUNT(*) FROM productos WHERE proveedorId = :proveedorId AND activo = 1")
    fun getCountByProveedor(proveedorId: Int): Int

    @Query("SELECT MAX(CAST(SUBSTR(codigoBarras, 6, 7) AS INTEGER)) FROM productos WHERE LENGTH(codigoBarras) = 13 AND SUBSTR(codigoBarras, 1, 5) = :prefijo AND activo = 1")
    fun getMaxConsecutivoByPrefijo(prefijo: String): Int?

    @Query("SELECT MAX(id) FROM productos")
    fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducto(producto: ProductoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(productos: List<ProductoEntity>)

    @Update
    fun updateProducto(producto: ProductoEntity)

    @Query("UPDATE productos SET stock = :nuevoStock WHERE id = :productoId")
    fun updateStock(productoId: Int, nuevoStock: Int)

    @Query("UPDATE productos SET activo = 0 WHERE id = :id")
    fun softDeleteProducto(id: Int)

    @Query("DELETE FROM productos")
    fun clearAll()
}
