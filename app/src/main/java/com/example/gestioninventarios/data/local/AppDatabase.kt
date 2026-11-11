package com.example.gestioninventarios.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gestioninventarios.data.local.dao.CategoriaDao
import com.example.gestioninventarios.data.local.dao.ProductoDao
import com.example.gestioninventarios.data.local.dao.ProveedorDao
import com.example.gestioninventarios.data.local.dao.UsuarioDao
import com.example.gestioninventarios.data.local.dao.VentaDao
import com.example.gestioninventarios.data.local.entity.CategoriaEntity
import com.example.gestioninventarios.data.local.entity.DetalleVentaEntity
import com.example.gestioninventarios.data.local.entity.ProductoEntity
import com.example.gestioninventarios.data.local.entity.ProveedorEntity
import com.example.gestioninventarios.data.local.entity.UsuarioEntity
import com.example.gestioninventarios.data.local.entity.VentaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categorias ADD COLUMN codigo INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE categorias SET codigo = id WHERE codigo = 0")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categorias_codigo ON categorias (codigo)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS proveedores (
                id INTEGER NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL,
                contacto TEXT,
                telefono TEXT,
                email TEXT,
                activo INTEGER NOT NULL,
                codigo INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_proveedores_codigo ON proveedores (codigo)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                email TEXT NOT NULL,
                passwordHash TEXT NOT NULL,
                rol TEXT NOT NULL,
                activo INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_email ON usuarios (email)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ventas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                usuarioId INTEGER NOT NULL,
                usuarioNombre TEXT NOT NULL,
                fechaVenta TEXT NOT NULL,
                total REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS detalle_ventas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ventaId INTEGER NOT NULL,
                productoId INTEGER NOT NULL,
                productoNombre TEXT NOT NULL,
                codigoBarras TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                precioUnitario REAL NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY(ventaId) REFERENCES ventas(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_detalle_ventas_ventaId ON detalle_ventas (ventaId)")
    }
}

@Database(
    entities = [
        ProductoEntity::class,
        CategoriaEntity::class,
        ProveedorEntity::class,
        UsuarioEntity::class,
        VentaEntity::class,
        DetalleVentaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_pyme_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                    }
                })
                .build()

                INSTANCE = instance
                seedInitialData(instance, context.applicationContext)
                instance
            }
        }

        private fun seedInitialData(db: AppDatabase, context: Context) {
            val prefManager = PreferenciasManager(context)
            if (prefManager.isDatosSembrados()) {
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val usrDao = db.usuarioDao()
                    if (usrDao.getUsuarios().isEmpty()) {
                        usrDao.insertAll(
                            listOf(
                                UsuarioEntity(1, "Administrador", "admin@inventariopyme.com", "admin123", "ADMIN", true),
                                UsuarioEntity(2, "Carlos Vendedor", "vendedor@inventariopyme.com", "vendedor123", "VENDEDOR", true)
                            )
                        )
                    }

                    val catDao = db.categoriaDao()
                    if (catDao.getCategorias().isEmpty()) {
                        catDao.insertAll(
                            listOf(
                                CategoriaEntity(1, "Bebidas", "Refrescos, jugos, aguas y bebidas preparadas", true, 1),
                                CategoriaEntity(2, "Abarrotes", "Productos de primera necesidad, granos y enlatados", true, 2),
                                CategoriaEntity(3, "Limpieza", "Productos de limpieza para el hogar y negocio", true, 3)
                            )
                        )
                    }

                    val provDao = db.proveedorDao()
                    if (provDao.getProveedores().isEmpty()) {
                        provDao.insertAll(
                            listOf(
                                ProveedorEntity(1, "Distribuidora del Norte", "Juan Pérez", "555-0101", "contacto@distnorte.com", true, 1),
                                ProveedorEntity(2, "Comercializadora Central", "María López", "555-0202", "ventas@comcentral.com", true, 2)
                            )
                        )
                    }

                    val prodDao = db.productoDao()
                    if (prodDao.getProductos().isEmpty()) {
                        prodDao.insertAll(
                            listOf(
                                ProductoEntity(1, "2010100000013", "Coca-Cola 600ml", 1.25, 0.85, 50, 10, 1, 1, true, "Bebidas", "Distribuidora del Norte"),
                                ProductoEntity(2, "2010100000020", "Agua Ciel 1.5L", 0.80, 0.45, 30, 10, 1, 1, true, "Bebidas", "Distribuidora del Norte"),
                                ProductoEntity(3, "2010100000037", "Jugo del Valle Naranja 1L", 1.50, 1.00, 20, 5, 1, 1, true, "Bebidas", "Distribuidora del Norte"),
                                ProductoEntity(4, "2020200000016", "Arroz Extra 1kg", 1.40, 0.95, 40, 10, 2, 2, true, "Abarrotes", "Comercializadora Central"),
                                ProductoEntity(5, "2020200000023", "Frijol Negro 1kg", 1.75, 1.20, 3, 10, 2, 2, true, "Abarrotes", "Comercializadora Central"),
                                ProductoEntity(6, "2020200000030", "Aceite Vegetal 900ml", 2.25, 1.60, 15, 5, 2, 2, true, "Abarrotes", "Comercializadora Central"),
                                ProductoEntity(7, "2020200000047", "Azúcar Estándar 1kg", 1.50, 1.10, 25, 5, 2, 2, true, "Abarrotes", "Comercializadora Central"),
                                ProductoEntity(8, "2030100000011", "Detergente Multiusos 1kg", 1.90, 1.35, 2, 8, 3, 1, true, "Limpieza", "Distribuidora del Norte"),
                                ProductoEntity(9, "2030100000028", "Jabón para Trastes 750ml", 1.60, 1.10, 18, 5, 3, 1, true, "Limpieza", "Distribuidora del Norte"),
                                ProductoEntity(10, "2030100000035", "Cloro Limpiador 1L", 0.85, 0.50, 22, 5, 3, 1, true, "Limpieza", "Distribuidora del Norte")
                            )
                        )
                    }

                    val vtaDao = db.ventaDao()
                    if (vtaDao.getVentasWithDetalles().isEmpty()) {
                        val fechaVenta1 = Instant.now().minusSeconds(3600 * 3).toString()
                        val vtaId1 = vtaDao.insertVenta(
                            VentaEntity(
                                id = 1,
                                usuarioId = 2,
                                usuarioNombre = "Carlos Vendedor",
                                fechaVenta = fechaVenta1,
                                total = 3.30
                            )
                        ).toInt()

                        vtaDao.insertDetalles(
                            listOf(
                                DetalleVentaEntity(id = 1, ventaId = vtaId1, productoId = 1, productoNombre = "Coca-Cola 600ml", codigoBarras = "2010100000013", cantidad = 2, precioUnitario = 1.25, subtotal = 2.50),
                                DetalleVentaEntity(id = 2, ventaId = vtaId1, productoId = 2, productoNombre = "Agua Ciel 1.5L", codigoBarras = "2010100000020", cantidad = 1, precioUnitario = 0.80, subtotal = 0.80)
                            )
                        )

                        val fechaVenta2 = Instant.now().minusSeconds(3600 * 24).toString()
                        val vtaId2 = vtaDao.insertVenta(
                            VentaEntity(
                                id = 2,
                                usuarioId = 1,
                                usuarioNombre = "Administrador",
                                fechaVenta = fechaVenta2,
                                total = 4.50
                            )
                        ).toInt()

                        vtaDao.insertDetalles(
                            listOf(
                                DetalleVentaEntity(id = 3, ventaId = vtaId2, productoId = 4, productoNombre = "Arroz Extra 1kg", codigoBarras = "2020200000016", cantidad = 1, precioUnitario = 1.40, subtotal = 1.40),
                                DetalleVentaEntity(id = 4, ventaId = vtaId2, productoId = 6, productoNombre = "Aceite Vegetal 900ml", codigoBarras = "2020200000030", cantidad = 1, precioUnitario = 2.25, subtotal = 2.25),
                                DetalleVentaEntity(id = 5, ventaId = vtaId2, productoId = 10, productoNombre = "Cloro Limpiador 1L", codigoBarras = "2030100000035", cantidad = 1, precioUnitario = 0.85, subtotal = 0.85)
                            )
                        )
                    }
                    prefManager.setDatosSembrados(true)
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
