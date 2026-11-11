package com.example.gestioninventarios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestioninventarios.AppContainer
import com.example.gestioninventarios.ui.ajustes.AjustesAvanzadosViewModel
import com.example.gestioninventarios.ui.ajustes.AjustesViewModel
import com.example.gestioninventarios.ui.catalogos.CatalogosViewModel
import com.example.gestioninventarios.ui.home.HomeViewModel
import com.example.gestioninventarios.ui.login.LoginViewModel
import com.example.gestioninventarios.ui.productos.ProductoFormViewModel
import com.example.gestioninventarios.ui.productos.ProductosViewModel
import com.example.gestioninventarios.ui.reportes.ReportesViewModel
import com.example.gestioninventarios.ui.usuarios.UsuariosViewModel
import com.example.gestioninventarios.ui.venta.VentaViewModel

class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(appContainer.authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    appContainer.authRepository,
                    appContainer.reporteRepository,
                    appContainer.ventaRepository,
                    appContainer.productoRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProductosViewModel::class.java) -> {
                ProductosViewModel(
                    appContainer.productoRepository,
                    appContainer.categoriaRepository,
                    appContainer.proveedorRepository,
                    appContainer.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProductoFormViewModel::class.java) -> {
                ProductoFormViewModel(
                    appContainer.productoRepository,
                    appContainer.categoriaRepository,
                    appContainer.proveedorRepository
                ) as T
            }
            modelClass.isAssignableFrom(CatalogosViewModel::class.java) -> {
                CatalogosViewModel(
                    appContainer.categoriaRepository,
                    appContainer.proveedorRepository,
                    appContainer.productoRepository,
                    appContainer.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(VentaViewModel::class.java) -> {
                VentaViewModel(
                    appContainer.productoRepository,
                    appContainer.ventaRepository,
                    appContainer.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(ReportesViewModel::class.java) -> {
                ReportesViewModel(
                    appContainer.reporteRepository,
                    appContainer.ventaRepository,
                    appContainer.productoRepository
                ) as T
            }
            modelClass.isAssignableFrom(UsuariosViewModel::class.java) -> {
                UsuariosViewModel(
                    appContainer.usuarioRepository,
                    appContainer.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(AjustesViewModel::class.java) -> {
                AjustesViewModel(
                    appContainer.preferenciasManager,
                    appContainer.tokenStore
                ) as T
            }
            modelClass.isAssignableFrom(AjustesAvanzadosViewModel::class.java) -> {
                AjustesAvanzadosViewModel(
                    appContainer.appDatabase,
                    appContainer.preferenciasManager,
                    appContainer.almacenRespaldos,
                    appContainer.tokenStore
                ) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocida: ${modelClass.name}")
        }
    }
}
