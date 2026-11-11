package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.dao.ProveedorDao
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.model.ProveedorDto
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProveedorRepository(
    private val proveedorDao: ProveedorDao,
    private val productoDao: ProductoDao
) {
    suspend fun getProveedoresFromCache(): List<ProveedorDto> = withContext(Dispatchers.IO) {
        proveedorDao.getProveedores().map { it.toDto() }
    }

    suspend fun obtenerSiguienteCodigo(): Int = withContext(Dispatchers.IO) {
        val maxCod = proveedorDao.getMaxCodigo() ?: 0
        maxCod + 1
    }

    suspend fun listarProveedores(): Resource<List<ProveedorDto>> = withContext(Dispatchers.IO) {
        try {
            val list = proveedorDao.getProveedores().map { it.toDto() }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al listar proveedores")
        }
    }

    suspend fun crearProveedor(nombre: String, contacto: String?, telefono: String?, email: String?): Resource<ProveedorDto> = withContext(Dispatchers.IO) {
        val siguienteCod = obtenerSiguienteCodigo()
        if (siguienteCod > 99) {
            return@withContext Resource.Error("Se alcanzó el límite de 99 proveedores")
        }

        try {
            val newId = (proveedorDao.getMaxId() ?: 0) + 1
            val entity = ProveedorEntity(
                id = newId,
                nombre = nombre.trim(),
                contacto = contacto?.trim(),
                telefono = telefono?.trim(),
                email = email?.trim(),
                activo = true,
                codigo = siguienteCod
            )
            proveedorDao.insertProveedor(entity)
            Resource.Success(entity.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al crear el proveedor")
        }
    }

    suspend fun actualizarProveedor(id: Int, nombre: String, contacto: String?, telefono: String?, email: String?): Resource<ProveedorDto> = withContext(Dispatchers.IO) {
        try {
            val existing = proveedorDao.getProveedorById(id)
                ?: return@withContext Resource.Error("Proveedor no encontrado")
            val updated = existing.copy(
                nombre = nombre.trim(),
                contacto = contacto?.trim(),
                telefono = telefono?.trim(),
                email = email?.trim()
            )
            proveedorDao.updateProveedor(updated)
            Resource.Success(updated.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al actualizar el proveedor")
        }
    }

    suspend fun eliminarProveedor(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val prodCount = productoDao.getCountByProveedor(id)
            if (prodCount > 0) {
                return@withContext Resource.Error("No se puede eliminar el proveedor porque tiene productos asociados")
            }
            proveedorDao.softDeleteProveedor(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al eliminar el proveedor")
        }
    }
}
