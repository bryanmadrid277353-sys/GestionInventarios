package com.example.gestioninventarios.ui.ajustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.gestioninventarios.data.local.AppDatabase
import com.example.gestioninventarios.data.local.PreferenciasManager
import com.example.gestioninventarios.data.local.TokenStore
import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import com.example.gestioninventarios.util.AlmacenRespaldos
import com.example.gestioninventarios.util.ErrorHumano
import com.example.gestioninventarios.util.ErrorTranslator
import com.example.gestioninventarios.util.EstadoAccesoArchivos
import com.example.gestioninventarios.util.EstadoPermisoAcceso
import com.example.gestioninventarios.util.RespaldoDatosExportar
import com.example.gestioninventarios.util.RespaldoJsonManager
import com.example.gestioninventarios.util.RespaldoResultado
import com.example.gestioninventarios.util.RespaldoResultadoImportacion
import com.example.gestioninventarios.util.RespaldoValidacionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AjustesAvanzadosUiState(
    val esAdmin: Boolean = false,
    val cargando: Boolean = false,
    val mensajeFaseProgreso: String? = null,
    val ultimoRespaldoFecha: String = "",
    val ultimoRespaldoUbicacion: String = "",
    val estadoAcceso: EstadoPermisoAcceso? = null,

    val mensajeExitoExportacion: RespaldoResultado? = null,
    val conteosUltimaExportacion: Map<String, Int>? = null,
    val errorHumano: ErrorHumano? = null,

    val mostrarAvisoImportar: Boolean = false,
    val respaldoValidado: RespaldoValidacionInfo? = null,
    val jsonImportarPendiente: String? = null,
    val resultadoImportacion: RespaldoResultadoImportacion? = null,

    val mostrarConfirmarEliminar: Boolean = false,
    val resultadoEliminacion: String? = null
)

class AjustesAvanzadosViewModel(
    private val appDatabase: AppDatabase,
    private val preferenciasManager: PreferenciasManager,
    private val almacenRespaldos: AlmacenRespaldos,
    private val tokenStore: TokenStore
) : ViewModel() {

    companion object {
        const val REQUERIR_ACCESO_PARA_IMPORTAR = true
    }

    private val _uiState = MutableStateFlow(
        AjustesAvanzadosUiState(
            ultimoRespaldoFecha = preferenciasManager.getUltimoRespaldoFecha(),
            ultimoRespaldoUbicacion = preferenciasManager.getUltimoRespaldoUbicacion()
        )
    )
    val uiState: StateFlow<AjustesAvanzadosUiState> = _uiState.asStateFlow()

    private val _eventoSnackbarAccesoConcedido = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val eventoSnackbarAccesoConcedido: SharedFlow<Unit> = _eventoSnackbarAccesoConcedido.asSharedFlow()

    private var esperandoRetornoDeAjustes: Boolean = false

    init {
        verificarRolAdmin()
    }

    fun marcarEsperandoRetornoDeAjustes() {
        esperandoRetornoDeAjustes = true
    }

    fun reevaluarEstadoAlmacenamiento(context: Context) {
        val nuevoEstado = EstadoAccesoArchivos.evaluarEstado(context)
        val estadoAnterior = _uiState.value.estadoAcceso

        if (estadoAnterior != null) {
            if (esperandoRetornoDeAjustes && estadoAnterior == EstadoPermisoAcceso.FALTA && nuevoEstado == EstadoPermisoAcceso.CONCEDIDO) {
                _eventoSnackbarAccesoConcedido.tryEmit(Unit)
            }
            esperandoRetornoDeAjustes = false
        }

        _uiState.update {
            it.copy(estadoAcceso = nuevoEstado)
        }
    }

    private fun verificarRolAdmin() {
        viewModelScope.launch {
            val role = tokenStore.userRole.first()
            val esAdmin = (role == "ADMIN")
            _uiState.update { it.copy(esAdmin = esAdmin) }
        }
    }

    private suspend fun esAdminActivo(): Boolean {
        val role = tokenStore.userRole.first()
        return role == "ADMIN"
    }

    private fun comprobarAccesoOTirarError(): Boolean {
        if (_uiState.value.estadoAcceso != EstadoPermisoAcceso.CONCEDIDO) {
            _uiState.update {
                it.copy(
                    cargando = false,
                    mensajeFaseProgreso = null,
                    errorHumano = ErrorHumano(
                        mensajeSimple = "Sin el permiso de acceso a archivos no se pueden crear ni importar respaldos. Actívalo para proteger tu información.",
                        detalleTecnico = "PermissionException: estadoAcceso is FALTA"
                    )
                )
            }
            return false
        }
        return true
    }

    private suspend fun obtenerDatosSnapshotAtómico(): Pair<RespaldoDatosExportar, Map<String, Int>> {
        return appDatabase.withTransaction {
            val catDao = appDatabase.categoriaDao()
            val provDao = appDatabase.proveedorDao()
            val usrDao = appDatabase.usuarioDao()
            val prodDao = appDatabase.productoDao()
            val vtaDao = appDatabase.ventaDao()

            val catList = catDao.exportarTodasCategorias()
            val catCount = catDao.contarCategorias()

            val provList = provDao.exportarTodosProveedores()
            val provCount = provDao.contarProveedores()

            val prodList = prodDao.exportarTodosProductos()
            val prodCount = prodDao.contarProductos()

            val vtaList = vtaDao.exportarTodasVentas()
            val vtaCount = vtaDao.contarVentas()

            val detList = vtaDao.exportarTodosDetallesVenta()
            val detCount = vtaDao.contarDetallesVenta()

            val usrList = usrDao.getUsuarios()
            val mapaEmails = usrList.associate { it.id to it.email }
            val mapaNombres = usrList.associate { it.id to it.nombre }

            val datos = RespaldoDatosExportar(
                categorias = catList,
                proveedores = provList,
                productos = prodList,
                ventas = vtaList,
                detallesVenta = detList,
                mapaUsuariosEmail = mapaEmails,
                mapaUsuariosNombre = mapaNombres
            )

            val counts = mapOf(
                "categorias" to catCount,
                "proveedores" to provCount,
                "productos" to prodCount,
                "ventas" to vtaCount,
                "detalle_venta" to detCount
            )

            Pair(datos, counts)
        }
    }

    private suspend fun generarYGuardarRespaldoVerificado(
        prefijoNombre: String,
        subcarpeta: String? = null,
        marcaTiempo: String? = null
    ): Result<Pair<RespaldoResultado, Map<String, Int>>> {
        val (datos, dbCounts) = obtenerDatosSnapshotAtómico()
        val resultado = almacenRespaldos.guardarRespaldoJsonVerificado(
            datos = datos,
            dbCounts = dbCounts,
            prefijoNombre = prefijoNombre,
            subcarpeta = subcarpeta,
            marcaTiempo = marcaTiempo
        )

        return if (resultado.isSuccess) {
            Result.success(Pair(resultado.getOrThrow(), dbCounts))
        } else {
            Result.failure(resultado.exceptionOrNull() ?: IllegalStateException("Error al generar respaldo"))
        }
    }

    // ================= 1. EXPORTACIÓN =================

    fun exportarRespaldo() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!esAdminActivo()) {
                _uiState.update {
                    it.copy(
                        errorHumano = ErrorHumano(
                            mensajeSimple = "Acceso denegado: se requieren permisos de Administrador.",
                            detalleTecnico = "SecurityException: User role is not ADMIN"
                        )
                    )
                }
                return@launch
            }

            if (!comprobarAccesoOTirarError()) return@launch

            _uiState.update { it.copy(cargando = true, mensajeFaseProgreso = "Exportando respaldo de datos...") }

            try {
                val resultado = generarYGuardarRespaldoVerificado("InventarioPYME")

                if (resultado.isSuccess) {
                    val (res, counts) = resultado.getOrThrow()
                    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(Date())
                    preferenciasManager.guardarUltimoRespaldo(fechaFormateada, res.rutaLegible)

                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeFaseProgreso = null,
                            ultimoRespaldoFecha = fechaFormateada,
                            ultimoRespaldoUbicacion = res.rutaLegible,
                            mensajeExitoExportacion = res,
                            conteosUltimaExportacion = counts
                        )
                    }
                } else {
                    val ex = resultado.exceptionOrNull() ?: IllegalStateException("Error al escribir archivo de respaldo")
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeFaseProgreso = null,
                            errorHumano = ErrorTranslator.traducir(ex)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(e)
                    )
                }
            }
        }
    }

    fun dismissExitoExportacionDialog() {
        _uiState.update { it.copy(mensajeExitoExportacion = null, conteosUltimaExportacion = null) }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(errorHumano = null) }
    }

    // ================= 2. IMPORTACIÓN =================

    fun solicitarImportacion() {
        if (_uiState.value.estadoAcceso == EstadoPermisoAcceso.CONCEDIDO) {
            almacenRespaldos.asegurarCarpetaExiste()
            _uiState.update { it.copy(mostrarAvisoImportar = true) }
        }
    }

    fun dismissAvisoImportar() {
        _uiState.update { it.copy(mostrarAvisoImportar = false) }
    }

    fun procesarArchivoSeleccionado(jsonStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!esAdminActivo()) {
                _uiState.update {
                    it.copy(
                        errorHumano = ErrorHumano(
                            mensajeSimple = "Acceso denegado: se requieren permisos de Administrador.",
                            detalleTecnico = "SecurityException: User role is not ADMIN"
                        )
                    )
                }
                return@launch
            }

            if (!comprobarAccesoOTirarError()) return@launch

            _uiState.update { it.copy(cargando = true, mensajeFaseProgreso = "Validando archivo de respaldo...") }

            val validacion = RespaldoJsonManager.validarJsonImportacion(jsonStr)
            if (validacion.isFailure) {
                val ex = validacion.exceptionOrNull() ?: IllegalArgumentException("Archivo de respaldo inválido")
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(ex)
                    )
                }
                return@launch
            }

            val info = validacion.getOrThrow()
            _uiState.update {
                it.copy(
                    cargando = false,
                    mensajeFaseProgreso = null,
                    respaldoValidado = info,
                    jsonImportarPendiente = jsonStr
                )
            }
        }
    }

    fun dismissResumenImportacion() {
        _uiState.update {
            it.copy(
                respaldoValidado = null,
                jsonImportarPendiente = null
            )
        }
    }

    fun confirmarEjecutarImportacion() {
        val jsonStr = uiState.value.jsonImportarPendiente ?: return
        _uiState.update {
            it.copy(
                respaldoValidado = null,
                jsonImportarPendiente = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (!esAdminActivo()) {
                _uiState.update {
                    it.copy(
                        errorHumano = ErrorHumano(
                            mensajeSimple = "Acceso denegado: se requieren permisos de Administrador.",
                            detalleTecnico = "SecurityException: User role is not ADMIN"
                        )
                    )
                }
                return@launch
            }

            if (!comprobarAccesoOTirarError()) return@launch

            _uiState.update { it.copy(cargando = true, mensajeFaseProgreso = "Generando respaldo previo a la importación...") }

            val marcaTiempo = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(Date())

            var rutaPrevio = ""
            var rutaPosterior = ""

            // 1. Respaldo previo automático obligatorio en "Otros"
            try {
                val resPrevio = generarYGuardarRespaldoVerificado(
                    prefijoNombre = "InventarioPYME_previo-a-importar",
                    subcarpeta = AlmacenRespaldos.CARPETA_OTROS,
                    marcaTiempo = marcaTiempo
                )
                if (resPrevio.isFailure) {
                    val ex = resPrevio.exceptionOrNull() ?: IllegalStateException("Error al crear respaldo previo")
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeFaseProgreso = null,
                            errorHumano = ErrorTranslator.traducir(ex)
                        )
                    }
                    return@launch
                }
                rutaPrevio = resPrevio.getOrThrow().first.rutaLegible
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(e)
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(mensajeFaseProgreso = "Importando registros en base de datos...") }

            // 2. Transacción de importación (SIN insertar ni modificar usuarios)
            try {
                val root = JSONObject(jsonStr)
                val catArray = root.getJSONArray("categorias")
                val provArray = root.getJSONArray("proveedores")
                val prodArray = root.getJSONArray("productos")
                val vtaArray = root.getJSONArray("ventas")
                val detArray = root.getJSONArray("detalle_venta")

                // Si es un archivo de formato 1, leer el mapa idViejo -> email para asociar ventas
                val mapaUsrViejoIdAEmail = mutableMapOf<Int, String>()
                if (root.has("usuarios")) {
                    val usrArray = root.optJSONArray("usuarios")
                    if (usrArray != null) {
                        for (i in 0 until usrArray.length()) {
                            val uItem = usrArray.getJSONObject(i)
                            val oldUId = uItem.optInt("id", -1)
                            val uEmail = uItem.optString("email", "")
                            if (oldUId != -1 && uEmail.isNotEmpty()) {
                                mapaUsrViejoIdAEmail[oldUId] = uEmail
                            }
                        }
                    }
                }

                var renamedCat = 0
                var renamedProv = 0
                var renamedProd = 0

                appDatabase.withTransaction {
                    val catDao = appDatabase.categoriaDao()
                    val provDao = appDatabase.proveedorDao()
                    val usrDao = appDatabase.usuarioDao()
                    val prodDao = appDatabase.productoDao()
                    val vtaDao = appDatabase.ventaDao()

                    val existingCatNames = catDao.getAllCategorias().map { it.nombre.lowercase() }.toMutableSet()
                    val existingCatCodes = catDao.getAllCategorias().map { it.codigo }.toMutableSet()

                    val existingProvNames = provDao.getAllProveedores().map { it.nombre.lowercase() }.toMutableSet()
                    val existingProvCodes = provDao.getAllProveedores().map { it.codigo }.toMutableSet()

                    val existingProductBarcodes = prodDao.getAllProductos().map { it.codigoBarras }.toMutableSet()
                    val existingProductNames = prodDao.getAllProductos().map { it.nombre.lowercase() }.toMutableSet()

                    // Mapa de usuarios existentes por correo (case-insensitive)
                    val usuariosExistentes = usrDao.getUsuarios()
                    val mapaUsrEmailAId = usuariosExistentes.associate { it.email.lowercase() to it.id }
                    val currentAdminId = tokenStore.userId.first() ?: (usuariosExistentes.firstOrNull()?.id ?: 1)

                    val mapCatId = mutableMapOf<Int, Int>()
                    val mapProvId = mutableMapOf<Int, Int>()
                    val mapProdId = mutableMapOf<Int, Int>()
                    val mapVtaId = mutableMapOf<Int, Int>()

                    // Categorías
                    for (i in 0 until catArray.length()) {
                        val item = catArray.getJSONObject(i)
                        val oldId = item.getInt("id")
                        val nombre = item.getString("nombre")
                        val desc = if (item.isNull("descripcion")) null else item.getString("descripcion")
                        val activo = item.getBoolean("activo")

                        val nombreUnico = RespaldoJsonManager.hacerNombreUnico(nombre, existingCatNames)
                        if (nombreUnico != nombre) renamedCat++
                        existingCatNames.add(nombreUnico.lowercase())

                        val codigoUnico = RespaldoJsonManager.obtenerSiguienteCodigoInterno(existingCatCodes)
                        existingCatCodes.add(codigoUnico)

                        val newId = catDao.insertCategoria(
                            CategoriaEntity(
                                id = 0,
                                nombre = nombreUnico,
                                descripcion = desc,
                                activo = activo,
                                codigo = codigoUnico
                            )
                        ).toInt()
                        mapCatId[oldId] = newId
                    }

                    // Proveedores
                    for (i in 0 until provArray.length()) {
                        val item = provArray.getJSONObject(i)
                        val oldId = item.getInt("id")
                        val nombre = item.getString("nombre")
                        val contacto = if (item.isNull("contacto")) null else item.getString("contacto")
                        val tel = if (item.isNull("telefono")) null else item.getString("telefono")
                        val email = if (item.isNull("email")) null else item.getString("email")
                        val activo = item.getBoolean("activo")

                        val nombreUnico = RespaldoJsonManager.hacerNombreUnico(nombre, existingProvNames)
                        if (nombreUnico != nombre) renamedProv++
                        existingProvNames.add(nombreUnico.lowercase())

                        val codigoUnico = RespaldoJsonManager.obtenerSiguienteCodigoInterno(existingProvCodes)
                        existingProvCodes.add(codigoUnico)

                        val newId = provDao.insertProveedor(
                            ProveedorEntity(
                                id = 0,
                                nombre = nombreUnico,
                                contacto = contacto,
                                telefono = tel,
                                email = email,
                                activo = activo,
                                codigo = codigoUnico
                            )
                        ).toInt()
                        mapProvId[oldId] = newId
                    }

                    // NO SE INSERTAN USUARIOS

                    // Productos
                    for (i in 0 until prodArray.length()) {
                        val item = prodArray.getJSONObject(i)
                        val oldId = item.getInt("id")
                        val cb = item.getString("codigoBarras")
                        val nombre = item.getString("nombre")
                        val precio = item.getDouble("precio")
                        val costo = item.getDouble("costo")
                        val stock = item.getInt("stock")
                        val stockMin = item.getInt("stockMinimo")
                        val oldCatId = item.getInt("categoriaId")
                        val oldProvId = item.getInt("proveedorId")
                        val activo = item.getBoolean("activo")
                        val catNom = if (item.isNull("categoriaNombre")) null else item.getString("categoriaNombre")
                        val provNom = if (item.isNull("proveedorNombre")) null else item.getString("proveedorNombre")

                        val cbUnico = RespaldoJsonManager.hacerCodigoBarrasUnico(cb, existingProductBarcodes)
                        val nombreFinal = if (cbUnico != cb) {
                            renamedProd++
                            "$nombre (importado)"
                        } else {
                            val nUnico = RespaldoJsonManager.hacerNombreUnico(nombre, existingProductNames)
                            if (nUnico != nombre) renamedProd++
                            nUnico
                        }
                        existingProductBarcodes.add(cbUnico)
                        existingProductNames.add(nombreFinal.lowercase())

                        val newCatId = mapCatId[oldCatId] ?: 0
                        val newProvId = mapProvId[oldProvId] ?: 0

                        val newId = prodDao.insertProducto(
                            ProductoEntity(
                                id = 0,
                                codigoBarras = cbUnico,
                                nombre = nombreFinal,
                                precio = precio,
                                costo = costo,
                                stock = stock,
                                stockMinimo = stockMin,
                                categoriaId = newCatId,
                                proveedorId = newProvId,
                                activo = activo,
                                categoriaNombre = catNom,
                                proveedorNombre = provNom
                            )
                        ).toInt()
                        mapProdId[oldId] = newId
                    }

                    // Ventas
                    for (i in 0 until vtaArray.length()) {
                        val item = vtaArray.getJSONObject(i)
                        val oldId = item.getInt("id")
                        val oldUsrId = item.optInt("usuarioId", -1)
                        val usrNombre = item.optString("usuarioNombre", "Vendedor")
                        val vCor = item.optString("vendedorCorreo", "").ifEmpty {
                            mapaUsrViejoIdAEmail[oldUsrId] ?: ""
                        }
                        val fecha = item.getString("fechaVenta")
                        val total = item.getDouble("total")

                        // Asignar venta a un usuario existente por correo o al admin actual
                        val newUsrId = if (vCor.isNotEmpty()) {
                            mapaUsrEmailAId[vCor.lowercase()] ?: currentAdminId
                        } else {
                            currentAdminId
                        }

                        val newVtaId = vtaDao.insertVenta(
                            VentaEntity(
                                id = 0,
                                usuarioId = newUsrId,
                                usuarioNombre = usrNombre,
                                fechaVenta = fecha,
                                total = total
                            )
                        ).toInt()
                        mapVtaId[oldId] = newVtaId
                    }

                    // Detalle Ventas
                    val listaDetalles = mutableListOf<DetalleVentaEntity>()
                    for (i in 0 until detArray.length()) {
                        val item = detArray.getJSONObject(i)
                        val oldVtaId = item.getInt("ventaId")
                        val oldProdId = item.getInt("productoId")
                        val prodNombre = item.getString("productoNombre")
                        val cb = item.getString("codigoBarras")
                        val cant = item.getInt("cantidad")
                        val pUnit = item.getDouble("precioUnitario")
                        val subtotal = item.getDouble("subtotal")

                        val newVtaId = mapVtaId[oldVtaId]
                            ?: throw IllegalStateException("Inconsistencia: detalle sin venta padre válida")
                        val newProdId = mapProdId[oldProdId]
                            ?: throw IllegalStateException("Inconsistencia: detalle sin producto válido")

                        listaDetalles.add(
                            DetalleVentaEntity(
                                id = 0,
                                ventaId = newVtaId,
                                productoId = newProdId,
                                productoNombre = prodNombre,
                                codigoBarras = cb,
                                cantidad = cant,
                                precioUnitario = pUnit,
                                subtotal = subtotal
                            )
                        )
                    }

                    if (listaDetalles.isNotEmpty()) {
                        vtaDao.insertDetalles(listaDetalles)
                    }
                }

                // 3. Respaldo posterior automático en "Otros"
                _uiState.update { it.copy(mensajeFaseProgreso = "Generando respaldo posterior a la importación...") }
                try {
                    val resPosterior = generarYGuardarRespaldoVerificado(
                        prefijoNombre = "InventarioPYME_posterior-a-importar",
                        subcarpeta = AlmacenRespaldos.CARPETA_OTROS,
                        marcaTiempo = marcaTiempo
                    )
                    if (resPosterior.isSuccess) {
                        rutaPosterior = resPosterior.getOrThrow().first.rutaLegible
                    } else {
                        rutaPosterior = "No se pudo crear el respaldo posterior: ${resPosterior.exceptionOrNull()?.message}"
                    }
                } catch (e: Exception) {
                    rutaPosterior = "No se pudo crear el respaldo posterior: ${e.localizedMessage}"
                }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        resultadoImportacion = RespaldoResultadoImportacion(
                            categoriasImportadas = catArray.length(),
                            categoriasRenombradas = renamedCat,
                            proveedoresImportados = provArray.length(),
                            proveedoresRenombrados = renamedProv,
                            usuariosImportados = 0,
                            usuariosRenombrados = 0,
                            productosImportados = prodArray.length(),
                            productosRenombrados = renamedProd,
                            ventasImportadas = vtaArray.length(),
                            detallesImportados = detArray.length(),
                            rutaRespaldoPrevio = rutaPrevio,
                            rutaRespaldoPosterior = rutaPosterior
                        )
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(e)
                    )
                }
            }
        }
    }

    fun dismissResultadoImportacion() {
        _uiState.update { it.copy(resultadoImportacion = null) }
    }

    // ================= 3. ELIMINAR TODOS LOS DATOS =================

    fun solicitarEliminacion() {
        if (_uiState.value.estadoAcceso == EstadoPermisoAcceso.CONCEDIDO) {
            _uiState.update { it.copy(mostrarConfirmarEliminar = true) }
        }
    }

    fun dismissConfirmarEliminar() {
        _uiState.update { it.copy(mostrarConfirmarEliminar = false) }
    }

    fun ejecutarEliminacionTotal() {
        _uiState.update { it.copy(mostrarConfirmarEliminar = false) }

        viewModelScope.launch(Dispatchers.IO) {
            if (!esAdminActivo()) {
                _uiState.update {
                    it.copy(
                        errorHumano = ErrorHumano(
                            mensajeSimple = "Acceso denegado: se requieren permisos de Administrador.",
                            detalleTecnico = "SecurityException: User role is not ADMIN"
                        )
                    )
                }
                return@launch
            }

            if (!comprobarAccesoOTirarError()) return@launch

            _uiState.update { it.copy(cargando = true, mensajeFaseProgreso = "Generando respaldo previo de seguridad...") }

            var resPrevioRuta = ""
            try {
                val resPrevio = generarYGuardarRespaldoVerificado(
                    prefijoNombre = "InventarioPYME_previo-a-eliminar",
                    subcarpeta = AlmacenRespaldos.CARPETA_OTROS
                )
                if (resPrevio.isSuccess) {
                    resPrevioRuta = resPrevio.getOrThrow().first.rutaLegible
                } else {
                    val ex = resPrevio.exceptionOrNull() ?: IllegalStateException("Error al crear respaldo previo")
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeFaseProgreso = null,
                            errorHumano = ErrorTranslator.traducir(ex)
                        )
                    }
                    return@launch
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(e)
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(mensajeFaseProgreso = "Eliminando datos del sistema (conservando usuarios)...") }

            try {
                appDatabase.withTransaction {
                    val db = appDatabase.openHelper.writableDatabase
                    db.execSQL("DELETE FROM detalle_ventas")
                    db.execSQL("DELETE FROM ventas")
                    db.execSQL("DELETE FROM productos")
                    db.execSQL("DELETE FROM categorias")
                    db.execSQL("DELETE FROM proveedores")
                    // NO se eliminan usuarios
                    db.execSQL("DELETE FROM sqlite_sequence WHERE name IN ('detalle_ventas', 'ventas', 'productos', 'categorias', 'proveedores')")
                }

                preferenciasManager.setDatosSembrados(true)

                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        resultadoEliminacion = "Todos los productos, categorías, proveedores y ventas han sido eliminados. Los usuarios no se eliminaron.\n\nRespaldo de seguridad previo guardado en:\n$resPrevioRuta"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        mensajeFaseProgreso = null,
                        errorHumano = ErrorTranslator.traducir(e)
                    )
                }
            }
        }
    }

    fun dismissResultadoEliminacion() {
        _uiState.update { it.copy(resultadoEliminacion = null) }
    }
}
