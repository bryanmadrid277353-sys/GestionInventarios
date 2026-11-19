[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [1.a Propósito y alcance](#c1a)
- [1.b Requisitos del sistema y entorno de desarrollo](#c1b)
- [1.c Permisos y características del sistema (AndroidManifest)](#c1c)
- [1.d Dependencias del proyecto y sus funciones](#c1d)
- [1.e Estructura de carpetas y organización del código](#c1e)
- [1.f Guía de compilación y ejecución](#c1f)

---

<a id="c1"></a>
## 1. Visión general y entorno

<a id="c1a"></a>
### 1.a Propósito y alcance

**Inventario PyME** es una solución informática móvil diseñada específicamente para Pequeñas y Medianas Empresas (PyMEs) del sector comercial (tiendas de abarrotes, misceláneas, ferreterías, librerías, entre otros). Su objetivo principal es automatizar y simplificar la gestión integral de inventarios, el registro transaccional en Punto de Venta (POS), el control de existencias, la administración de catálogos de productos/categorías/proveedores y la generación de métricas de rendimiento comercial.

La característica arquitectónica fundamental de **Inventario PyME** es su operación **100 % local (Offline-First, Serverless)**. La aplicación no requiere conectividad a Internet, servidores remotos, API REST ni servicios en la nube para ninguna de sus funciones operativas cotidianas. Toda la persistencia relacional se ejecuta localmente en el dispositivo utilizando la base de datos SQLite gestionada a través del ORM **Room Database**, y la sesión se almacena mediante **Jetpack DataStore Preferences**.

#### Características Clave:
- **Punto de Venta Integrado**: Escaneo de códigos de barras en tiempo real con cámara del dispositivo, carrito reactivo con validación de stock y cobro transaccional.
- **Administración de Inventario**: Altas, bajas, modificaciones y consultas de productos con cálculo automático de márgenes de ganancia e indicadores de stock bajo.
- **Códigos de Barras Inteligentes**: Generación automática de códigos EAN-13 internos estructurados a partir de la categoría y el proveedor, así como generación visual de Bitmap para su lectura física.
- **Control de Acceso Basado en Roles (RBAC)**: Distinción operativa entre usuarios `ADMIN` (acceso total a catálogos, reportes, creación de usuarios y respaldos) y `VENDEDOR` (restringido a operaciones de venta y consulta de productos).
- **Gestión Avanzada de Datos**: Exportación e importación de respaldos en formato JSON estandarizado, verificación estricta de checksums/conteos e historial de copias automáticas sin sobrescribir información previa.

---

<a id="c1b"></a>
### 1.b Requisitos del sistema y entorno de desarrollo

El proyecto está configurado mediante Gradle Kotlin DSL (`build.gradle.kts`) y la biblioteca de versiones centralizada `gradle/libs.versions.toml`.

| Parámetro / Herramienta | Valor / Versión Requerida | Ubicación en Código |
| :--- | :--- | :--- |
| **Android SDK Compilación (`compileSdk`)** | 34 (Android 14) | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |
| **Android SDK Mínimo (`minSdk`)** | 24 (Android 7.0 Nougat) | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |
| **Android SDK Objetivo (`targetSdk`)** | 34 (Android 14) | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |
| **Versión del Proyecto (`versionCode` / `versionName`)** | `1` / `"1.0"` | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |
| **Lenguaje de Programación** | Kotlin `2.0.21` | [build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/build.gradle.kts) |
| **Android Gradle Plugin (AGP)** | `9.5.1` | [gradle/libs.versions.toml](file:///C:/Users/Administrador/Desktop/GestionInventarios/gradle/libs.versions.toml) |
| **Kotlin Symbol Processing (KSP)** | `2.0.21-1.0.27` | [gradle/libs.versions.toml](file:///C:/Users/Administrador/Desktop/GestionInventarios/gradle/libs.versions.toml) |
| **Compatibilidad Java / JDK** | Java 17 | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |
| **Desugaring de Librerías Core** | `com.android.tools:desugar_jdk_libs:2.0.4` | [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts) |

---

<a id="c1c"></a>
### 1.c Permisos y características del sistema (AndroidManifest)

El manifiesto de la aplicación se encuentra en [app/src/main/AndroidManifest.xml](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/AndroidManifest.xml). Declara los permisos y características de hardware requeridos para la operación del escáner y la gestión de archivos de respaldo.

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

#### Descripción de Permisos:
1. `android.permission.CAMERA`: Permite acceder al hardware de la cámara trasera para el escaneo de códigos de barras mediante CameraX y ML Kit.
2. `android.permission.WRITE_EXTERNAL_STORAGE`: Permite guardar archivos JSON de respaldo en el almacenamiento externo en dispositivos con Android 10 (API 29) o inferior.
3. `android.permission.READ_EXTERNAL_STORAGE`: Permite leer archivos de respaldo del almacenamiento externo en dispositivos con Android 12 (API 32) o inferior.
4. `android.permission.MANAGE_EXTERNAL_STORAGE`: Requerido en Android 11+ (API 30+) para acceder a la carpeta pública del almacenamiento externo sin restricciones de Scoped Storage.
5. `android.hardware.camera`: Marca la característica de cámara como opcional (`android:required="false"`), permitiendo que la aplicación se instale en dispositivos sin cámara (donde el ingreso de códigos se realiza por teclado o lector USB/Bluetooth).

#### Configuración de Application y Activity:
- **Clase Application**: `com.example.gestioninventarios.GestionInventariosApp`
- **Actividad Principal**: `com.example.gestioninventarios.MainActivity` (`exported="true"` con filtro `android.intent.action.MAIN` y categoría `LAUNCHER`).
- **Ajuste de Ventana**: `android:windowSoftInputMode="adjustResize"` para garantizar que el teclado virtual no solape los campos de texto ni la barra de cobro.
- **Almacenamiento Legacy**: `android:requestLegacyExternalStorage="true"` activado para mantener compatibilidad de escritura directa en Android 10 (API 29).

---

<a id="c1d"></a>
### 1.d Dependencias del proyecto y sus funciones

Todas las dependencias se gestionan desde el catálogo de versiones [gradle/libs.versions.toml](file:///C:/Users/Administrador/Desktop/GestionInventarios/gradle/libs.versions.toml) e implementadas en [app/build.gradle.kts](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/build.gradle.kts).

| Grupo / Librería | Versión | Función en la Aplicación |
| :--- | :--- | :--- |
| `androidx.core:core-ktx` | `1.13.1` | Extensiones de Kotlin para las APIs fundamentales de Android. |
| `com.google.android.material:material` | `1.12.0` | Componentes clásicos de Material Design y temas. |
| `androidx.compose.bom` | `2024.10.00` | Compose Bill of Materials que unifica las versiones de Jetpack Compose. |
| `androidx.compose.ui:ui` | BOM | Núcleo del motor de renderizado declarativo UI en Jetpack Compose. |
| `androidx.compose.material3:material3` | BOM | Componentes de diseño Material Design 3 (Cards, Scaffolds, TopBars, Buttons, Chips). |
| `androidx.compose.material:material-icons-extended` | BOM | Conjunto extendido de íconos vectoriales de Material Design. |
| `androidx.navigation:navigation-compose` | `2.8.3` | Gestión de la navegación por grafo entre composables (`NavHost`). |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.6` | Integración de `ViewModel` con el ciclo de vida de Jetpack Compose. |
| `androidx.lifecycle:lifecycle-process` | `2.8.6` | Observación del ciclo de vida global del proceso de la app (`ProcessLifecycleOwner`). |
| `androidx.datastore:datastore-preferences` | `1.1.1` | Almacenamiento asíncrono y reactivo de la sesión de usuario activa (`TokenStore`). |
| `androidx.room:room-runtime` / `room-ktx` | `2.6.1` | ORM para la base de datos local SQLite con soporte para Corrutinas y Flows. |
| `androidx.room:room-compiler` (KSP) | `2.6.1` | Procesador de anotaciones para la generación de código de Room con KSP. |
| `androidx.camera:camera-core` / `camera2` / `lifecycle` / `view` | `1.3.4` | API de cámara CameraX para captura de fotogramas y control de linterna. |
| `com.google.mlkit:barcode-scanning` | `17.3.0` | Motor local de visión por computadora para detección de códigos de barras (EAN-13, CODE 128). |
| `com.google.zxing:core` | `3.5.3` | Generación de matrices BitMatrix para renderizar códigos de barras en Bitmap. |
| `junit:junit` | `4.13.2` | Framework para pruebas unitarias locales en JVM. |
| `org.json:json` | `20240303` | Manipulación de estructuras JSON en pruebas unitarias locales. |
| `androidx.test.ext:junit` / `espresso-core` | `1.2.1` / `3.6.1` | Framework para pruebas instrumentadas en emulador o dispositivo real. |

---

<a id="c1e"></a>
### 1.e Estructura de carpetas y organización del código

El código fuente de la aplicación se encuentra estructurado en el paquete raíz `com.example.gestioninventarios`. A continuación se detalla la jerarquía completa de archivos Kotlin:

```
app/src/main/java/com/example/gestioninventarios/
├── AppContainer.kt                         # Contenedor central de inyección de dependencias
├── GestionInventariosApp.kt                # Clase Application y arranque de ciclo de vida
├── MainActivity.kt                         # Actividad única (Single Activity Architecture)
├── data/                                   # Capa de Datos
│   ├── local/                              # Persistencia Local
│   │   ├── AppDatabase.kt                  # Base de datos Room, migraciones y sembrado demo
│   │   ├── PreferenciasManager.kt          # SharedPreferences (tema y estado de respaldo)
│   │   ├── TokenStore.kt                   # DataStore Preferences (sesión de usuario)
│   │   ├── dao/                            # Interfaces Data Access Object
│   │   │   ├── CategoriaDao.kt
│   │   │   ├── ProductoDao.kt
│   │   │   ├── ProveedorDao.kt
│   │   │   ├── UsuarioDao.kt
│   │   │   └── VentaDao.kt
│   │   └── entity/                         # Entidades de Room
│   │       ├── CategoriaEntity.kt
│   │       ├── ProductoEntity.kt
│   │       ├── ProveedorEntity.kt
│   │       ├── UsuarioEntity.kt
│   │       ├── VentaEntity.kt
│   │       └── VentaWithDetalles.kt        # Relación 1 a N (Venta con DetalleVenta)
│   ├── model/                              # Objetos de Transferencia de Datos (DTOs)
│   │   ├── CategoriaDto.kt
│   │   ├── ProductoDto.kt
│   │   ├── ProveedorDto.kt
│   │   ├── ReporteDto.kt
│   │   ├── UsuarioDto.kt
│   │   └── VentaDto.kt
│   └── repository/                         # Repositorios (Lógica de acceso a datos)
│       ├── AuthRepository.kt
│       ├── CategoriaRepository.kt
│       ├── ProductoRepository.kt
│       ├── ProveedorRepository.kt
│       ├── ReporteRepository.kt
│       ├── UsuarioRepository.kt
│       └── VentaRepository.kt
├── ui/                                     # Capa de Presentación (UI)
│   ├── ViewModelFactory.kt                 # Fábrica centralizada de ViewModels
│   ├── ajustes/
│   │   ├── AjustesAvanzadosScreen.kt       # Pantalla de respaldos e importación/exportación
│   │   ├── AjustesAvanzadosViewModel.kt
│   │   ├── AjustesScreen.kt                # Pantalla de tema y preferencias generales
│   │   └── AjustesViewModel.kt
│   ├── catalogos/
│   │   ├── CatalogosScreen.kt              # Pantalla de categorías y proveedores
│   │   └── CatalogosViewModel.kt
│   ├── components/
│   │   └── Componentes.kt                  # Componentes reutilizables (AppScaffold, KpiCard, StatusChip, etc.)
│   ├── home/
│   │   ├── HomeScreen.kt                   # Pantalla principal (Dashboard y menú modular)
│   │   └── HomeViewModel.kt
│   ├── login/
│   │   ├── LoginScreen.kt                  # Pantalla de autenticación
│   │   └── LoginViewModel.kt
│   ├── navigation/
│   │   ├── NavGraph.kt                     # Grafo de navegación de Jetpack Navigation
│   │   └── Screen.kt                       # Definición de rutas y argumentos
│   ├── productos/
│   │   ├── ProductoFormScreen.kt           # Formulario de creación/edición de productos
│   │   ├── ProductoFormViewModel.kt
│   │   ├── ProductosScreen.kt              # Listado, búsqueda y filtros de inventario
│   │   └── ProductosViewModel.kt
│   ├── reportes/
│   │   ├── ReportesScreen.kt               # Pantalla de métricas y gráficos estadísticos
│   │   └── ReportesViewModel.kt
│   ├── scanner/
│   │   └── ScannerScreen.kt                # Escáner de código de barras con CameraX + ML Kit
│   ├── theme/
│   │   ├── Color.kt                        # Definición de colores base
│   │   ├── Theme.kt                        # Configuración de tema claro/oscuro de Material 3
│   │   └── Type.kt                         # Tipografías del sistema
│   ├── usuarios/
│   │   ├── UsuariosScreen.kt               # Pantalla de administración de personal
│   │   └── UsuariosViewModel.kt
│   └── venta/
│       ├── VentaScreen.kt                  # Pantalla de Punto de Venta (POS) e Historial
│       └── VentaViewModel.kt
└── util/                                   # Utilidades y Auxiliares
    ├── AlmacenRespaldos.kt                 # Gestión de carpetas y guardado físico de archivos
    ├── BarcodeGenerator.kt                 # Generador de Bitmap de código de barras con ZXing
    ├── CodigoInterno.kt                    # Algoritmo EAN-13 (prefijo, consecutivo y dígito verificador)
    ├── DateTimeUtils.kt                    # Formateo e interpretación ISO/UTC y rangos de fechas
    ├── Ean13Validator.kt                   # Validador de estructura EAN-13
    ├── ErrorTranslator.kt                  # Traductor de excepciones técnicas a lenguaje humano
    ├── EstadoAccesoArchivos.kt             # Evaluador del estado de permisos de almacenamiento
    ├── OpenDocumentInBackupFolderContract.kt # Contrato personalizado para apertura de archivos JSON
    ├── Resource.kt                         # Wrapper genérico para estados de carga (Success, Error, Loading)
    ├── RespaldoJsonManager.kt              # Exportador/Importador JSON en streaming y validaciones
    ├── SesionConfig.kt                     # Constantes de tiempos de inactividad
    └── SesionManager.kt                    # Control del ciclo de vida y expiración de sesión por inactividad
```

---

<a id="c1f"></a>
### 1.f Guía de compilación y ejecución

#### Prerrequisitos:
1. **Android Studio**: Jellyfish (2023.3.1) o superior (se recomienda Android Studio Ladybug / 2024.2.1+).
2. **JDK**: Java Development Kit 17 configurado en las opciones de Gradle de Android Studio (`Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`).
3. **Dispositivo o Emulador**: Android 7.0 (API 24) o superior. Para probar la función de la cámara, se recomienda un dispositivo físico o un emulador con la cámara trasera configurada en `VirtualScene` o `Webcam`.

#### Pasos para Compilar:
1. Clonar o abrir la carpeta raíz `GestionInventarios` en Android Studio.
2. Ejecutar la sincronización de Gradle mediante `File > Sync Project with Gradle Files` (o llamando al comando Gradle `./gradlew tasks`).
3. Seleccionar la configuración de ejecución `app` y el dispositivo destino.
4. Para compilar el APK de depuración desde la consola de comandos:
   ```bash
   ./gradlew app:assembleDebug
   ```
   El APK generado se encontrará en:
   `app/build/outputs/apk/debug/app-debug.apk`

5. Para ejecutar las pruebas unitarias locales:
   ```bash
   ./gradlew test
   ```

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
