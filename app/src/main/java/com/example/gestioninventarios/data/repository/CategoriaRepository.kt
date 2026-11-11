package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.dao.CategoriaDao
import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.model.CategoriaDto
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoriaRepository(
    private val categoriaDao: CategoriaDao,
    private val productoDao: ProductoDao
) {
    suspend fun getCategoriasFromCache(): List<CategoriaDto> = withContext(Dispatchers.IO) {
        categoriaDao.getCategorias().map { it.toDto() }
    }

    suspend fun obtenerSiguienteCodigo(): Int = withContext(Dispatchers.IO) {
        val maxCod = categoriaDao.getMaxCodigo() ?: 0
        maxCod + 1
    }

    suspend fun listarCategorias(): Resource<List<CategoriaDto>> = withContext(Dispatchers.IO) {
        try {
            val list = categoriaDao.getCategorias().map { it.toDto() }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al listar categorías")
        }
    }

    suspend fun crearCategoria(nombre: String, descripcion: String?): Resource<CategoriaDto> = withContext(Dispatchers.IO) {
        val siguienteCod = obtenerSiguienteCodigo()
        if (siguienteCod > 99) {
            return@withContext Resource.Error("Se alcanzó el límite de 99 categorías")
        }

        try {
            val newId = (categoriaDao.getMaxId() ?: 0) + 1
            val entity = CategoriaEntity(
                id = newId,
                nombre = nombre.trim(),
                descripcion = descripcion?.trim(),
                activo = true,
                codigo = siguienteCod
            )
            categoriaDao.insertCategoria(entity)
            Resource.Success(entity.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al crear la categoría")
        }
    }

    suspend fun actualizarCategoria(id: Int, nombre: String, descripcion: String?): Resource<CategoriaDto> = withContext(Dispatchers.IO) {
        try {
            val existing = categoriaDao.getCategoriaById(id)
                ?: return@withContext Resource.Error("Categoría no encontrada")
            val updated = existing.copy(
                nombre = nombre.trim(),
                descripcion = descripcion?.trim()
            )
            categoriaDao.updateCategoria(updated)
            Resource.Success(updated.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al actualizar la categoría")
        }
    }

    suspend fun eliminarCategoria(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val prodCount = productoDao.getCountByCategoria(id)
            if (prodCount > 0) {
                return@withContext Resource.Error("No se puede eliminar la categoría porque tiene productos asociados")
            }
            categoriaDao.softDeleteCategoria(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al eliminar la categoría")
        }
    }
}
