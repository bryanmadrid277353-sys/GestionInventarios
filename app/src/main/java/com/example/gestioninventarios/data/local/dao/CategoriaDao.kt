package com.example.gestioninventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestioninventarios.data.local.entity.CategoriaEntity

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias WHERE activo = 1 ORDER BY nombre ASC")
    fun getCategorias(): List<CategoriaEntity>

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    fun getCategoriaById(id: Int): CategoriaEntity?

    @Query("SELECT MAX(codigo) FROM categorias")
    fun getMaxCodigo(): Int?

    @Query("SELECT MAX(id) FROM categorias")
    fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoria(categoria: CategoriaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(categorias: List<CategoriaEntity>)

    @Update
    fun updateCategoria(categoria: CategoriaEntity)

    @Query("UPDATE categorias SET activo = 0 WHERE id = :id")
    fun softDeleteCategoria(id: Int)

    @Query("DELETE FROM categorias")
    fun clearAll()
}
