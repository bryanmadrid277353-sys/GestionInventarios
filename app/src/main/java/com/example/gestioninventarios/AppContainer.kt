package com.example.gestioninventarios

import android.content.Context
import com.example.gestioninventarios.data.local.AppDatabase
import com.example.gestioninventarios.data.local.PreferenciasManager
import com.example.gestioninventarios.data.local.TokenStore
import com.example.gestioninventarios.data.repository.AuthRepository
import com.example.gestioninventarios.data.repository.CategoriaRepository
import com.example.gestioninventarios.data.repository.ProductoRepository
import com.example.gestioninventarios.data.repository.ProveedorRepository
import com.example.gestioninventarios.data.repository.ReporteRepository
import com.example.gestioninventarios.data.repository.UsuarioRepository
import com.example.gestioninventarios.data.repository.VentaRepository
import com.example.gestioninventarios.util.AlmacenRespaldos
import com.example.gestioninventarios.util.SesionManager

class AppContainer(private val context: Context) {

    val preferenciasManager: PreferenciasManager by lazy {
        PreferenciasManager(context)
    }

    val almacenRespaldos: AlmacenRespaldos by lazy {
        AlmacenRespaldos(context)
    }

    val tokenStore: TokenStore by lazy {
        TokenStore(context)
    }

    val sesionManager: SesionManager by lazy {
        SesionManager(context, tokenStore)
    }

    val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(appDatabase.usuarioDao(), tokenStore)
    }

    val categoriaRepository: CategoriaRepository by lazy {
        CategoriaRepository(appDatabase.categoriaDao(), appDatabase.productoDao())
    }

    val proveedorRepository: ProveedorRepository by lazy {
        ProveedorRepository(appDatabase.proveedorDao(), appDatabase.productoDao())
    }

    val productoRepository: ProductoRepository by lazy {
        ProductoRepository(appDatabase.productoDao(), appDatabase.categoriaDao(), appDatabase.proveedorDao())
    }

    val ventaRepository: VentaRepository by lazy {
        VentaRepository(appDatabase.ventaDao(), appDatabase.productoDao(), tokenStore)
    }

    val reporteRepository: ReporteRepository by lazy {
        ReporteRepository(appDatabase.ventaDao(), appDatabase.productoDao())
    }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(appDatabase.usuarioDao())
    }
}
