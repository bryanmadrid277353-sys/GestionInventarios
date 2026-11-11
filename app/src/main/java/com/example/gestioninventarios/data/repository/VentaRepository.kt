package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.TokenStore
import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.dao.VentaDao
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import com.example.gestioninventarios.data.model.DetalleVentaCreateDto
import com.example.gestioninventarios.data.model.VentaResponseDto
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant

class VentaRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao,
    private val tokenStore: TokenStore
) {
    suspend fun crearVenta(detalles: List<DetalleVentaCreateDto>): Resource<VentaResponseDto> = withContext(Dispatchers.IO) {
        if (detalles.isEmpty()) {
            return@withContext Resource.Error("No se seleccionaron productos para la venta")
        }

        try {
            val uId = tokenStore.userId.first() ?: 1
            val uName = tokenStore.userName.first() ?: "Usuario"
            val fechaIso = Instant.now().toString()

            val lineas = mutableListOf<DetalleVentaEntity>()
            var totalVenta = 0.0

            for (det in detalles) {
                val prod = productoDao.getProductoById(det.productoId)
                    ?: return@withContext Resource.Error("Producto ID ${det.productoId} no encontrado")

                if (prod.stock < det.cantidad) {
                    return@withContext Resource.Error("Stock insuficiente para ${prod.nombre}. Disponible: ${prod.stock}")
                }

                val subtotal = det.precioUnitario * det.cantidad
                totalVenta += subtotal

                lineas.add(
                    DetalleVentaEntity(
                        ventaId = 0,
                        productoId = prod.id,
                        productoNombre = prod.nombre,
                        codigoBarras = prod.codigoBarras,
                        cantidad = det.cantidad,
                        precioUnitario = det.precioUnitario,
                        subtotal = subtotal
                    )
                )
            }

            val ventaEntity = VentaEntity(
                usuarioId = uId,
                usuarioNombre = uName,
                fechaVenta = fechaIso,
                total = totalVenta
            )
            val ventaIdLong = ventaDao.insertVenta(ventaEntity)
            val ventaId = ventaIdLong.toInt()

            val lineasConId = lineas.map { it.copy(ventaId = ventaId) }
            ventaDao.insertDetalles(lineasConId)

            for (det in detalles) {
                val prod = productoDao.getProductoById(det.productoId)!!
                val nuevoStock = prod.stock - det.cantidad
                productoDao.updateStock(prod.id, nuevoStock)
            }

            val result = ventaDao.getVentaWithDetallesById(ventaId)
            if (result != null) {
                Resource.Success(result.toDto())
            } else {
                Resource.Error("Error al recuperar la venta registrada")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al registrar la venta")
        }
    }

    suspend fun listarVentas(): Resource<List<VentaResponseDto>> = withContext(Dispatchers.IO) {
        try {
            val list = ventaDao.getVentasWithDetalles().map { it.toDto() }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al consultar las ventas")
        }
    }

    suspend fun obtenerVenta(id: Int): Resource<VentaResponseDto> = withContext(Dispatchers.IO) {
        try {
            val item = ventaDao.getVentaWithDetallesById(id)
            if (item != null) {
                Resource.Success(item.toDto())
            } else {
                Resource.Error("Venta no encontrada")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al consultar detalle de la venta")
        }
    }
}
