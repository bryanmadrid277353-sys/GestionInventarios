package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.dao.CategoriaDao
import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.dao.ProveedorDao
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.model.ProductoDto
import com.example.gestioninventarios.util.CodigoInterno
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProductoCreateDto(
    val codigoBarras: String,
    val nombre: String,
    val precio: Double,
    val costo: Double,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaId: Int,
    val proveedorId: Int
)

data class ProductoUpdateDto(
    val codigoBarras: String,
    val nombre: String,
    val precio: Double,
    val costo: Double,
    val stock: Int,
    val stockMinimo: Int,
    val categoriaId: Int,
    val proveedorId: Int
)

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val categoriaDao: CategoriaDao,
    private val proveedorDao: ProveedorDao
) {
    suspend fun getProductosFromCache(search: String? = null, categoriaId: Int? = null): List<ProductoDto> = withContext(Dispatchers.IO) {
        val cached = productoDao.getProductos().map { it.toDto() }
        var filtered = cached
        if (categoriaId != null) {
            filtered = filtered.filter { it.categoriaId == categoriaId }
        }
        if (!search.isNullOrEmpty()) {
            val query = search.lowercase()
            filtered = filtered.filter {
                it.nombre.lowercase().contains(query) || it.codigoBarras.lowercase().contains(query)
            }
        }
        filtered
    }

    suspend fun generarCodigoInterno(categoriaId: Int?, proveedorId: Int?): String = withContext(Dispatchers.IO) {
        val catCodigo = if (categoriaId != null) {
            val cat = categoriaDao.getCategoriaById(categoriaId)
            cat?.codigo ?: categoriaId
        } else 0

        val provCodigo = if (proveedorId != null) {
            val prov = proveedorDao.getProveedorById(proveedorId)
            prov?.codigo ?: proveedorId
        } else 0

        val prefijo = CodigoInterno.prefijo(catCodigo, provCodigo)
        val maxCons = productoDao.getMaxConsecutivoByPrefijo(prefijo) ?: 0
        val siguienteCons = maxCons + 1

        CodigoInterno.generar(catCodigo, provCodigo, siguienteCons)
    }

    suspend fun listarProductos(search: String? = null, categoriaId: Int? = null): Resource<List<ProductoDto>> = withContext(Dispatchers.IO) {
        try {
            val list = getProductosFromCache(search, categoriaId)
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al cargar productos")
        }
    }

    suspend fun obtenerProducto(id: Int): Resource<ProductoDto> = withContext(Dispatchers.IO) {
        try {
            val prod = productoDao.getProductoById(id)
            if (prod != null && prod.activo) {
                Resource.Success(prod.toDto())
            } else {
                Resource.Error("Producto no encontrado")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al obtener producto")
        }
    }

    suspend fun obtenerPorCodigoBarras(codigo: String): Resource<ProductoDto> = withContext(Dispatchers.IO) {
        try {
            val prod = productoDao.getProductoByBarcode(codigo.trim())
            if (prod != null && prod.activo) {
                Resource.Success(prod.toDto())
            } else {
                Resource.Error("Producto no encontrado con el código especificado")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al buscar por código de barras")
        }
    }

    suspend fun crearProducto(createDto: ProductoCreateDto): Resource<ProductoDto> = withContext(Dispatchers.IO) {
        try {
            val cat = categoriaDao.getCategoriaById(createDto.categoriaId)
            val prov = proveedorDao.getProveedorById(createDto.proveedorId)
            val newId = (productoDao.getMaxId() ?: 0) + 1

            val entity = ProductoEntity(
                id = newId,
                codigoBarras = createDto.codigoBarras.trim(),
                nombre = createDto.nombre.trim(),
                precio = createDto.precio,
                costo = createDto.costo,
                stock = createDto.stock,
                stockMinimo = createDto.stockMinimo,
                categoriaId = createDto.categoriaId,
                proveedorId = createDto.proveedorId,
                activo = true,
                categoriaNombre = cat?.nombre,
                proveedorNombre = prov?.nombre
            )
            productoDao.insertProducto(entity)
            Resource.Success(entity.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al crear el producto")
        }
    }

    suspend fun actualizarProducto(id: Int, updateDto: ProductoUpdateDto): Resource<ProductoDto> = withContext(Dispatchers.IO) {
        try {
            val existing = productoDao.getProductoById(id)
                ?: return@withContext Resource.Error("Producto no encontrado")
            val cat = categoriaDao.getCategoriaById(updateDto.categoriaId)
            val prov = proveedorDao.getProveedorById(updateDto.proveedorId)

            val updated = existing.copy(
                codigoBarras = updateDto.codigoBarras.trim(),
                nombre = updateDto.nombre.trim(),
                precio = updateDto.precio,
                costo = updateDto.costo,
                stock = updateDto.stock,
                stockMinimo = updateDto.stockMinimo,
                categoriaId = updateDto.categoriaId,
                proveedorId = updateDto.proveedorId,
                categoriaNombre = cat?.nombre,
                proveedorNombre = prov?.nombre
            )
            productoDao.updateProducto(updated)
            Resource.Success(updated.toDto())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al actualizar el producto")
        }
    }

    suspend fun eliminarProducto(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            productoDao.softDeleteProducto(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al eliminar el producto")
        }
    }
}
