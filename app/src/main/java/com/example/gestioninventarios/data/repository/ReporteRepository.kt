package com.example.gestioninventarios.data.repository

import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.dao.VentaDao
import com.example.gestioninventarios.data.model.ReporteStockBajoDto
import com.example.gestioninventarios.data.model.ReporteVentasDiaDto
import com.example.gestioninventarios.util.DateTimeUtils
import com.example.gestioninventarios.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ReporteRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao
) {
    suspend fun obtenerVentasPorDia(desde: String? = null): Resource<List<ReporteVentasDiaDto>> = withContext(Dispatchers.IO) {
        try {
            val ventas = ventaDao.getVentasWithDetalles()
            var filtered = ventas

            if (!desde.isNullOrBlank()) {
                val desdeMs = DateTimeUtils.parseFechaVentaToEpochMilli(desde)
                filtered = ventas.filter {
                    DateTimeUtils.parseFechaVentaToEpochMilli(it.venta.fechaVenta) >= desdeMs
                }
            }

            val agrupadas = filtered.groupBy {
                val zdt = DateTimeUtils.parseFechaVentaToZonedDateTime(it.venta.fechaVenta)
                zdt.toLocalDate().toString()
            }

            val reporte = agrupadas.map { (fecha, listaVentas) ->
                ReporteVentasDiaDto(
                    fecha = fecha,
                    totalVentas = listaVentas.sumOf { it.venta.total },
                    numeroVentas = listaVentas.size
                )
            }.sortedByDescending { it.fecha }

            Resource.Success(reporte)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al obtener ventas por día")
        }
    }

    suspend fun obtenerStockBajo(): Resource<List<ReporteStockBajoDto>> = withContext(Dispatchers.IO) {
        try {
            val prods = productoDao.getProductos().filter { it.stock <= it.stockMinimo }
            val reporte = prods.map { prod ->
                ReporteStockBajoDto(
                    id = prod.id,
                    nombre = prod.nombre,
                    codigoBarras = prod.codigoBarras,
                    stock = prod.stock,
                    stockMinimo = prod.stockMinimo,
                    categoriaNombre = prod.categoriaNombre,
                    proveedorNombre = prod.proveedorNombre
                )
            }
            Resource.Success(reporte)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al obtener productos con stock bajo")
        }
    }
}
