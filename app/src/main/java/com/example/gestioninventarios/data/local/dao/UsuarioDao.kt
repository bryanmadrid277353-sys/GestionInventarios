package com.example.gestioninventarios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestioninventarios.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY id ASC")
    fun getUsuarios(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    fun getUsuarioById(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    fun getUsuarioByEmail(email: String): UsuarioEntity?

    @Query("SELECT MAX(id) FROM usuarios")
    fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(usuarios: List<UsuarioEntity>)

    @Update
    fun updateUsuario(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET activo = :activo WHERE id = :id")
    fun updateEstado(id: Int, activo: Boolean)

    @Query("DELETE FROM usuarios")
    fun clearAll()
}
