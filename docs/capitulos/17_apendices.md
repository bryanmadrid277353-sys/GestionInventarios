[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [17.a Mapeo completo de archivos, clases y capítulos](#c17a)
- [17.b Tabla detallada de permisos Android requeridos](#c17b)
- [17.c Tabla de constantes de configuración del sistema](#c17c)
- [17.d Glosario de términos técnicos y conceptos del dominio](#c17d)

---

<a id="c17"></a>
## 17. Apéndices

<a id="c17a"></a>
### 17.a Mapeo completo de archivos, clases y capítulos

La siguiente tabla audita y mapea **TODOS** los archivos de código fuente (`.kt`) pertenecientes a los conjuntos de fuentes `main`, `test` y `androidTest`, garantizando una cobertura de documentación del 100 %:

| Ruta del Archivo en el Proyecto | Clases / Interfaces / Objetos Contenidos | Capítulo y Sección donde se Documenta |
| :--- | :--- | :--- |
| `app/src/main/java/com/example/gestioninventarios/AppContainer.kt` | `AppContainer` | [Capítulo 2.b](02_arquitectura.md#c2b) |
| `app/src/main/java/com/example/gestioninventarios/GestionInventariosApp.kt` | `GestionInventariosApp` | [Capítulo 1.e](01_vision_general.md#c1e), [Capítulo 2.b](02_arquitectura.md#c2b) |
| `app/src/main/java/com/example/gestioninventarios/MainActivity.kt` | `MainActivity` | [Capítulo 1.e](01_vision_general.md#c1e), [Capítulo 13.b](13_interfaz_usuario.md#c13b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/AppDatabase.kt` | `AppDatabase`, `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4` | [Capítulo 3.a](03_base_de_datos.md#c3a), [Capítulo 3.d](03_base_de_datos.md#c3d), [Capítulo 3.e](03_base_de_datos.md#c3e) |
| `app/src/main/java/com/example/gestioninventarios/data/local/PreferenciasManager.kt` | `PreferenciasManager` | [Capítulo 11.c](11_usuarios_ajustes.md#c11c), [Capítulo 14.e](14_ciclo_vida_datos.md#c14e) |
| `app/src/main/java/com/example/gestioninventarios/data/local/TokenStore.kt` | `TokenStore` | [Capítulo 5.c](05_seguridad_sesion_roles.md#c5c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/dao/CategoriaDao.kt` | `CategoriaDao` | [Capítulo 3.c](03_base_de_datos.md#c3c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/dao/ProductoDao.kt` | `ProductoDao`, `CategoriaCount`, `ProveedorCount` | [Capítulo 3.c](03_base_de_datos.md#c3c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/dao/ProveedorDao.kt` | `ProveedorDao` | [Capítulo 3.c](03_base_de_datos.md#c3c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/dao/UsuarioDao.kt` | `UsuarioDao` | [Capítulo 3.c](03_base_de_datos.md#c3c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/dao/VentaDao.kt` | `VentaDao` | [Capítulo 3.c](03_base_de_datos.md#c3c) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/CategoriaEntity.kt` | `CategoriaEntity`, `toDto()`, `toEntity()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/ProductoEntity.kt` | `ProductoEntity`, `toDto()`, `toEntity()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/ProveedorEntity.kt` | `ProveedorEntity`, `toDto()`, `toEntity()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/UsuarioEntity.kt` | `UsuarioEntity`, `toDto()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/VentaEntity.kt` | `VentaEntity`, `DetalleVentaEntity`, `toDto()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/local/entity/VentaWithDetalles.kt` | `VentaWithDetalles`, `toDto()` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/model/CategoriaDto.kt` | `CategoriaDto` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/model/ProductoDto.kt` | `ProductoDto` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/model/ProveedorDto.kt` | `ProveedorDto` | [Capítulo 3.b](03_base_de_datos.md#c3b) |
| `app/src/main/java/com/example/gestioninventarios/data/model/ReporteDto.kt` | `ReporteVentasDiaDto`, `ReporteStockBajoDto` | [Capítulo 10.a](10_reportes.md#c10a) |
| `app/src/main/java/com/example/gestioninventarios/data/model/UsuarioDto.kt` | `UsuarioResponseDto`, `UsuarioCreateDto` | [Capítulo 11.a](11_usuarios_ajustes.md#c11a) |
| `app/src/main/java/com/example/gestioninventarios/data/model/VentaDto.kt` | `DetalleVentaCreateDto`, `DetalleVentaResponseDto`, `VentaResponseDto` | [Capítulo 6.a](06_punto_de_venta.md#c6a) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/AuthRepository.kt` | `AuthRepository` | [Capítulo 4.a](04_repositorios_reglas.md#c4a) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/CategoriaRepository.kt` | `CategoriaRepository` | [Capítulo 4.c](04_repositorios_reglas.md#c4c) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/ProductoRepository.kt` | `ProductoRepository`, `ProductoCreateDto`, `ProductoUpdateDto` | [Capítulo 4.b](04_repositorios_reglas.md#c4b) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/ProveedorRepository.kt` | `ProveedorRepository` | [Capítulo 4.d](04_repositorios_reglas.md#c4d) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/ReporteRepository.kt` | `ReporteRepository` | [Capítulo 4.f](04_repositorios_reglas.md#c4f) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/UsuarioRepository.kt` | `UsuarioRepository` | [Capítulo 4.g](04_repositorios_reglas.md#c4g) |
| `app/src/main/java/com/example/gestioninventarios/data/repository/VentaRepository.kt` | `VentaRepository` | [Capítulo 4.e](04_repositorios_reglas.md#c4e) |
| `app/src/main/java/com/example/gestioninventarios/ui/ViewModelFactory.kt` | `ViewModelFactory` | [Capítulo 2.b](02_arquitectura.md#c2b) |
| `app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesAvanzadosScreen.kt` | `AjustesAvanzadosScreen`, `AjustesAvanzadosScreenContent` | [Capítulo 12.b](12_respaldos_gestion_datos.md#c12b) |
| `app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesAvanzadosViewModel.kt` | `AjustesAvanzadosViewModel`, `AjustesAvanzadosUiState` | [Capítulo 12.b](12_respaldos_gestion_datos.md#c12b) |
| `app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesScreen.kt` | `AjustesScreen`, `AjustesScreenContent`, `OpcionTemaRow` | [Capítulo 11.c](11_usuarios_ajustes.md#c11c) |
| `app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesViewModel.kt` | `AjustesViewModel`, `AjustesUiState` | [Capítulo 11.c](11_usuarios_ajustes.md#c11c) |
| `app/src/main/java/com/example/gestioninventarios/ui/catalogos/CatalogosScreen.kt` | `CatalogosScreen`, `CategoriaItemCard`, `ProveedorItemCard`, etc. | [Capítulo 8.a](08_catalogos.md#c8a) |
| `app/src/main/java/com/example/gestioninventarios/ui/catalogos/CatalogosViewModel.kt` | `CatalogosViewModel`, `CatalogosUiState` | [Capítulo 8.a](08_catalogos.md#c8a) |
| `app/src/main/java/com/example/gestioninventarios/ui/components/Componentes.kt` | `AppScaffold`, `AppTopBar`, `StatusChip`, `StockBadge`, `KpiCard`, etc. | [Capítulo 13.c](13_interfaz_usuario.md#c13c) |
| `app/src/main/java/com/example/gestioninventarios/ui/home/HomeScreen.kt` | `HomeScreen`, `HomeScreenContent`, `GreetingCard`, `KpisSection`, etc. | [Capítulo 2.d](02_arquitectura.md#c2d) |
| `app/src/main/java/com/example/gestioninventarios/ui/home/HomeViewModel.kt` | `HomeViewModel`, `HomeUiState`, `ModuloItem` | [Capítulo 2.d](02_arquitectura.md#c2d) |
| `app/src/main/java/com/example/gestioninventarios/ui/login/LoginScreen.kt` | `LoginScreen`, `LoginContent` | [Capítulo 5.b](05_seguridad_sesion_roles.md#c5b) |
| `app/src/main/java/com/example/gestioninventarios/ui/login/LoginViewModel.kt` | `LoginViewModel`, `LoginUiState` | [Capítulo 5.b](05_seguridad_sesion_roles.md#c5b) |
| `app/src/main/java/com/example/gestioninventarios/ui/navigation/NavGraph.kt` | `NavGraph` | [Capítulo 2.d](02_arquitectura.md#c2d) |
| `app/src/main/java/com/example/gestioninventarios/ui/navigation/Screen.kt` | `Screen` | [Capítulo 2.d](02_arquitectura.md#c2d) |
| `app/src/main/java/com/example/gestioninventarios/ui/productos/ProductoFormScreen.kt` | `ProductoFormScreen` | [Capítulo 7.b](07_productos.md#c7b) |
| `app/src/main/java/com/example/gestioninventarios/ui/productos/ProductoFormViewModel.kt` | `ProductoFormViewModel`, `ProductoFormUiState` | [Capítulo 7.b](07_productos.md#c7b) |
| `app/src/main/java/com/example/gestioninventarios/ui/productos/ProductosScreen.kt` | `ProductosScreen`, `ProductoCardItem`, `VistaPreviaCodigoBarrasDialog`, etc. | [Capítulo 7.a](07_productos.md#c7a) |
| `app/src/main/java/com/example/gestioninventarios/ui/productos/ProductosViewModel.kt` | `ProductosViewModel`, `ProductosUiState`, `OrdenProducto` | [Capítulo 7.a](07_productos.md#c7a) |
| `app/src/main/java/com/example/gestioninventarios/ui/reportes/ReportesScreen.kt` | `ReportesScreen`, `CustomBarChart`, `exportarReporteCSV` | [Capítulo 10.c](10_reportes.md#c10c) |
| `app/src/main/java/com/example/gestioninventarios/ui/reportes/ReportesViewModel.kt` | `ReportesViewModel`, `ReportesUiState`, `Periodo`, `ChartBarItem`, etc. | [Capítulo 10.b](10_reportes.md#c10b) |
| `app/src/main/java/com/example/gestioninventarios/ui/scanner/ScannerScreen.kt` | `ScannerScreen`, `MarcoVisorEscaneo`, `vibrarAlEscanear` | [Capítulo 9.a](09_escaner_barras.md#c9a) |
| `app/src/main/java/com/example/gestioninventarios/ui/theme/Color.kt` | Definición de colores | [Capítulo 13.a](13_interfaz_usuario.md#c13a) |
| `app/src/main/java/com/example/gestioninventarios/ui/theme/Theme.kt` | `InventarioTheme`, `GestionInventariosTheme`, `Semantico` | [Capítulo 13.a](13_interfaz_usuario.md#c13a) |
| `app/src/main/java/com/example/gestioninventarios/ui/theme/Type.kt` | `Typography` | [Capítulo 13.a](13_interfaz_usuario.md#c13a) |
| `app/src/main/java/com/example/gestioninventarios/ui/usuarios/UsuariosScreen.kt` | `UsuariosScreen`, `UsuarioItemCard`, `FormularioUsuarioBottomSheet` | [Capítulo 11.a](11_usuarios_ajustes.md#c11a) |
| `app/src/main/java/com/example/gestioninventarios/ui/usuarios/UsuariosViewModel.kt` | `UsuariosViewModel`, `UsuariosUiState` | [Capítulo 11.a](11_usuarios_ajustes.md#c11a) |
| `app/src/main/java/com/example/gestioninventarios/ui/venta/VentaScreen.kt` | `VentaScreen`, `PosTabContent`, `HistorialTabContent`, `CobroBottomSheet`, etc. | [Capítulo 6.a](06_punto_de_venta.md#c6a) |
| `app/src/main/java/com/example/gestioninventarios/ui/venta/VentaViewModel.kt` | `VentaViewModel`, `VentaUiState`, `CartItem`, `VentaEvento` | [Capítulo 6.a](06_punto_de_venta.md#c6a) |
| `app/src/main/java/com/example/gestioninventarios/util/AlmacenRespaldos.kt` | `AlmacenRespaldos`, `RespaldoResultado` | [Capítulo 12.b](12_respaldos_gestion_datos.md#c12b) |
| `app/src/main/java/com/example/gestioninventarios/util/BarcodeGenerator.kt` | `BarcodeGenerator` | [Capítulo 7.d](07_productos.md#c7d) |
| `app/src/main/java/com/example/gestioninventarios/util/CodigoInterno.kt` | `CodigoInterno` | [Capítulo 7.c](07_productos.md#c7c) |
| `app/src/main/java/com/example/gestioninventarios/util/DateTimeUtils.kt` | `DateTimeUtils`, `RangoPeriodo` | [Capítulo 10.b](10_reportes.md#c10b) |
| `app/src/main/java/com/example/gestioninventarios/util/Ean13Validator.kt` | `Ean13Validator` | [Capítulo 7.b](07_productos.md#c7b) |
| `app/src/main/java/com/example/gestioninventarios/util/ErrorTranslator.kt` | `ErrorTranslator`, `ErrorHumano` | [Capítulo 12.b](12_respaldos_gestion_datos.md#c12b) |
| `app/src/main/java/com/example/gestioninventarios/util/EstadoAccesoArchivos.kt` | `EstadoAccesoArchivos`, `EstadoPermisoAcceso` | [Capítulo 12.e](12_respaldos_gestion_datos.md#c12e) |
| `app/src/main/java/com/example/gestioninventarios/util/OpenDocumentInBackupFolderContract.kt` | `OpenDocumentInBackupFolderContract` | [Capítulo 12.e](12_respaldos_gestion_datos.md#c12e) |
| `app/src/main/java/com/example/gestioninventarios/util/Resource.kt` | `Resource` (`Success`, `Error`, `Loading`) | [Capítulo 2.a](02_arquitectura.md#c2a) |
| `app/src/main/java/com/example/gestioninventarios/util/RespaldoJsonManager.kt` | `RespaldoJsonManager`, `RespaldoDatosExportar`, `RespaldoValidacionInfo`, etc. | [Capítulo 12.a](12_respaldos_gestion_datos.md#c12a) |
| `app/src/main/java/com/example/gestioninventarios/util/SesionConfig.kt` | `SesionConfig` | [Capítulo 5.d](05_seguridad_sesion_roles.md#c5d) |
| `app/src/main/java/com/example/gestioninventarios/util/SesionManager.kt` | `SesionManager` | [Capítulo 5.d](05_seguridad_sesion_roles.md#c5d) |
| `app/src/test/java/com/example/gestioninventarios/ExampleUnitTest.kt` | `ExampleUnitTest` | [Capítulo 15.a](15_pruebas.md#c15a) |
| `app/src/test/java/com/example/gestioninventarios/ui/login/LoginViewModelTest.kt` | `LoginViewModelTest` | [Capítulo 15.a](15_pruebas.md#c15a) |
| `app/src/test/java/com/example/gestioninventarios/util/CodigoInternoTest.kt` | `CodigoInternoTest` | [Capítulo 15.b](15_pruebas.md#c15b) |
| `app/src/test/java/com/example/gestioninventarios/util/Ean13ValidatorTest.kt` | `Ean13ValidatorTest` | [Capítulo 15.b](15_pruebas.md#c15b) |
| `app/src/test/java/com/example/gestioninventarios/util/ReportesRangoTest.kt` | `ReportesRangoTest` | [Capítulo 15.d](15_pruebas.md#c15d) |
| `app/src/test/java/com/example/gestioninventarios/util/RespaldoJsonManagerTest.kt` | `RespaldoJsonManagerTest` | [Capítulo 15.c](15_pruebas.md#c15c) |
| `app/src/androidTest/java/com/example/gestioninventarios/ExampleInstrumentedTest.kt` | `ExampleInstrumentedTest` | [Capítulo 15.e](15_pruebas.md#c15e) |

---

<a id="c17b"></a>
### 17.b Tabla detallada de permisos Android requeridos

| Permiso Android | Nivel / Nivel de Riesgo | Versión de SDK Afectada | Propósito Técnico en la App |
| :--- | :--- | :--- | :--- |
| `android.permission.CAMERA` | Peligroso (Runtime) | Todas (API 24+) | Captura de fotogramas mediante CameraX para detección de códigos de barras con ML Kit. |
| `android.permission.WRITE_EXTERNAL_STORAGE` | Peligroso (Runtime) | SDK 24 a 29 (`maxSdkVersion=29`) | Escritura directa de archivos JSON en el almacenamiento secundario público. |
| `android.permission.READ_EXTERNAL_STORAGE` | Peligroso (Runtime) | SDK 24 a 32 (`maxSdkVersion=32`) | Lectura de archivos JSON desde la carpeta de respaldos. |
| `android.permission.MANAGE_EXTERNAL_STORAGE` | Especial (All Files Access) | API 30+ (Android 11+) | Acceso total al almacenamiento para crear y gestionar la carpeta `/Inventario PYME/Respaldos de Datos/`. |

---

<a id="c17c"></a>
### 17.c Tabla de constantes de configuración del sistema

| Constante | Valor Real | Archivo Fuente | Descripción |
| :--- | :--- | :--- | :--- |
| `TIEMPO_SESION_INACTIVA_MIN` | `10L` | [SesionConfig.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/SesionConfig.kt) | Minutos máximos permitidos de inactividad antes de expirar la sesión. |
| `TIEMPO_SESION_INACTIVA_MS` | `600000L` | [SesionConfig.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/SesionConfig.kt) | Equivalente en milisegundos del tiempo máximo de inactividad. |
| `CARPETA_APP` | `"Inventario PYME"` | [AlmacenRespaldos.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/AlmacenRespaldos.kt) | Carpeta raíz creada en el almacenamiento externo. |
| `CARPETA_RESPALDOS` | `"Respaldos de Datos"` | [AlmacenRespaldos.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/AlmacenRespaldos.kt) | Subcarpeta destino para archivos de respaldo. |
| `CARPETA_OTROS` | `"Otros"` | [AlmacenRespaldos.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/AlmacenRespaldos.kt) | Subcarpeta para respaldos automáticos (previos/posteriores). |
| `APP_IDENTIFIER` | `"InventarioPYME"` | [RespaldoJsonManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/RespaldoJsonManager.kt) | Sello de validación de encabezado en archivos JSON. |
| `FORMATO_VERSION` | `2` | [RespaldoJsonManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/RespaldoJsonManager.kt) | Versión del esquema de importación/exportación JSON. |
| `VERSION_BD` | `3` | [RespaldoJsonManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/RespaldoJsonManager.kt) | Versión de base de datos declarada en el JSON. |
| `TAMANO_MAX_BYTES` | `52428800` (50 MB) | [RespaldoJsonManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/RespaldoJsonManager.kt) | Límite máximo de tamaño para importar un archivo JSON. |
| `SEGUNDOS_CONFIRMACION_ELIMINAR` | `3` | [AjustesAvanzadosScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesAvanzadosScreen.kt) | Segundos de retardo para habilitar el botón de eliminación total. |

---

<a id="c17d"></a>
### 17.d Glosario de términos técnicos y conceptos del dominio

- **Atomicidad (Transacción Database)**: Propiedad que garantiza que una serie de operaciones en la base de datos (como restar stock e insertar ticket de venta) se ejecuten en su totalidad o no se ejecute ninguna en caso de falla.
- **Borrado Lógico (Soft Delete)**: Técnica donde un registro no se elimina físicamente de la base de datos (`DELETE FROM`), sino que se marca como inactivo (`activo = 0`) para preservar referencias históricas.
- **CameraX**: Biblioteca de Jetpack para simplificar el desarrollo de aplicaciones de cámara en Android.
- **DAO (Data Access Object)**: Componente que proporciona una interfaz limpia para mapear llamadas a funciones con consultas SQL sobre SQLite.
- **DataStore Preferences**: Solución moderna de almacenamiento de clave-valor que reemplaza a `SharedPreferences`, ofreciendo consistencia asíncrona mediante Kotlin Coroutines y Flow.
- **Desnormalización**: Técnica de diseño de bases de datos que duplica ciertos datos (ej. guardar el nombre del producto en el detalle de la venta) para agilizar las lecturas sin necesidad de JOINs.
- **EAN-13 (European Article Numbering)**: Estándar internacional de códigos de barras de 13 dígitos numéricos, compuesto por país/bandera, empresa, código de producto y dígito verificador.
- **Inyección de Dependencias (DI)**: Patrón de diseño donde las dependencias de una clase se suministran externamente en lugar de ser instanciadas internamente por la misma.
- **KPI (Key Performance Indicator)**: Indicador clave de rendimiento utilizado para medir el éxito o estado comercial de un negocio.
- **ML Kit**: Kit de desarrollo de Google que proporciona capacidades de aprendizaje automático en el dispositivo (*on-device*), como el reconocimiento de códigos de barras.
- **MVVM (Model-View-ViewModel)**: Patrón arquitectónico que separa la interfaz gráfica de usuario de la lógica de negocio y los datos.
- **Offline-First (Serverless)**: Filosofía de diseño de software donde la aplicación opera con capacidad completa de forma local sin requerir conexión a un servidor remoto.
- **Recomposición**: Proceso por el cual Jetpack Compose vuelve a ejecutar los composables cuando cambian los datos de estado observados.
- **Room**: Biblioteca de persistencia oficial de Jetpack que proporciona una capa de abstracción sobre SQLite.
- **Scoped Storage**: Modelo de almacenamiento introducido en Android 10+ que restringe el acceso de las aplicaciones únicamente a sus propios directorios o a carpetas públicas autorizadas.
- **StateFlow**: Flujo de datos observable y reactivo de Kotlin Coroutines que mantiene el estado actual emitido.
- **UDF (Unidirectional Data Flow)**: Patrón donde el estado fluye hacia abajo (de ViewModel a Composable) y los eventos fluyen hacia arriba (de Composable a ViewModel).

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
