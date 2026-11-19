# DOCUMENTACIÓN TÉCNICA OFICIAL
## Aplicación Móvil: Inventario PyME

---

### Datos del Documento

- **Nombre de la Aplicación**: Inventario PyME
- **Nombre Técnico de Proyecto**: `GestionInventarios`
- **Paquete Base (applicationId)**: `com.example.gestioninventarios`
- **Versión de la Aplicación**: 1.0 (versionCode: 1)
- **Fecha de Documentación**: Marzo 2026
- **Stack Tecnológico**: Android, Kotlin 2.0.21, Jetpack Compose, Material 3, ViewModel & Coroutines, Room (SQLite), DataStore Preferences, CameraX & ML Kit.
- **Arquitectura**: MVVM (Model-View-ViewModel) con Inyección Manual de Dependencias (`AppContainer`) y almacenamiento 100% local en dispositivo (sin servidor remoto).

---

### Cómo leer este documento

La presente **Documentación Técnica** ha sido estructurada como un compendio exhaustivo y de nivel profesional para desarrolladores, arquitectos de software, auditores y mantenedores del sistema **Inventario PyME**.

El contenido está distribuido en 17 capítulos independientes organizados en archivos Markdown individuales dentro de la carpeta `docs/capitulos/`. Cada capítulo contiene un encabezado navegable con hipervínculos relativos que garantizan su correcta visualización y navegación cruzada tanto en **Android Studio**, **GitHub**, **GitLab** y **VS Code**.

Para explorar un módulo o aspecto técnico específico:
1. Navegue al **Índice General** que se presenta a continuación.
2. Haga clic sobre el capítulo o sección de su interés. Cada enlace apunta a un ancla explícita (HTML anchor tag) dentro del archivo correspondiente.
3. Al finalizar la lectura de cualquier sección o capítulo, utilice el enlace **"← Volver al índice"** ubicado al inicio y al final del archivo para regresar a este documento maestro.

---

<a id="indice"></a>
### ÍNDICE GENERAL

- [1. Visión general y entorno](capitulos/01_vision_general.md#c1)
  - [1.a Propósito y alcance](capitulos/01_vision_general.md#c1a)
  - [1.b Requisitos del sistema y entorno de desarrollo](capitulos/01_vision_general.md#c1b)
  - [1.c Permisos y características del sistema (AndroidManifest)](capitulos/01_vision_general.md#c1c)
  - [1.d Dependencias del proyecto y sus funciones](capitulos/01_vision_general.md#c1d)
  - [1.e Estructura de carpetas y organización del código](capitulos/01_vision_general.md#c1e)
  - [1.f Guía de compilación y ejecución](capitulos/01_vision_general.md#c1f)
- [2. Arquitectura](capitulos/02_arquitectura.md#c2)
  - [2.a Patrón arquitectónico MVVM y estratificación por capas](capitulos/02_arquitectura.md#c2a)
  - [2.b Contenedor de dependencias e inyección manual (AppContainer)](capitulos/02_arquitectura.md#c2b)
  - [2.c Flujo de datos, estado inmutable y reactividad (StateFlow y SharedFlow)](capitulos/02_arquitectura.md#c2c)
  - [2.d Sistema de navegación centralizado y rutas (NavGraph y Screen)](capitulos/02_arquitectura.md#c2d)
  - [2.e Interoperabilidad y comunicación entre pantallas](capitulos/02_arquitectura.md#c2e)
- [3. Base de datos (Room/SQLite)](capitulos/03_base_de_datos.md#c3)
  - [3.a Configuración de RoomDatabase y versionamiento (AppDatabase)](capitulos/03_base_de_datos.md#c3a)
  - [3.b Entidades de datos y diseño relacional (Entities)](capitulos/03_base_de_datos.md#c3b)
  - [3.c Objetos de Acceso a Datos (DAOs) y consultas SQL](capitulos/03_base_de_datos.md#c3c)
  - [3.d Estrategia de migraciones de esquema (MIGRATION_1_2, 2_3, 3_4)](capitulos/03_base_de_datos.md#c3d)
  - [3.e Sembrado inicial de datos demo (seedInitialData)](capitulos/03_base_de_datos.md#c3e)
- [4. Repositorio y reglas de negocio](capitulos/04_repositorios_reglas.md#c4)
  - [4.a Repositorio de Autenticación (AuthRepository)](capitulos/04_repositorios_reglas.md#c4a)
  - [4.b Repositorio de Productos (ProductoRepository)](capitulos/04_repositorios_reglas.md#c4b)
  - [4.c Repositorio de Categorías (CategoriaRepository)](capitulos/04_repositorios_reglas.md#c4c)
  - [4.d Repositorio de Proveedores (ProveedorRepository)](capitulos/04_repositorios_reglas.md#c4d)
  - [4.e Repositorio de Ventas (VentaRepository)](capitulos/04_repositorios_reglas.md#c4e)
  - [4.f Repositorio de Reportes (ReporteRepository)](capitulos/04_repositorios_reglas.md#c4f)
  - [4.g Repositorio de Usuarios (UsuarioRepository)](capitulos/04_repositorios_reglas.md#c4g)
- [5. Seguridad, sesión y roles](capitulos/05_seguridad_sesion_roles.md#c5)
  - [5.a Modelo de seguridad, almacenamiento de credenciales y roles](capitulos/05_seguridad_sesion_roles.md#c5a)
  - [5.b Mecanismo de autenticación y proceso de Login](capitulos/05_seguridad_sesion_roles.md#c5b)
  - [5.c Gestión de sesión persistente con DataStore (TokenStore)](capitulos/05_seguridad_sesion_roles.md#c5c)
  - [5.d Control de inactividad, expiración y cierre de sesión (SesionManager)](capitulos/05_seguridad_sesion_roles.md#c5d)
  - [5.e Validación de permisos por rol en la interfaz de usuario](capitulos/05_seguridad_sesion_roles.md#c5e)
- [6. Punto de Venta](capitulos/06_punto_de_venta.md#c6)
  - [6.a Estructura del Punto de Venta y gestión del carrito (VentaScreen / VentaViewModel)](capitulos/06_punto_de_venta.md#c6a)
  - [6.b Búsqueda reactiva, sugerencias y escaneo de productos](capitulos/06_punto_de_venta.md#c6b)
  - [6.c Validación de stock y reglas de existencias en el carrito](capitulos/06_punto_de_venta.md#c6c)
  - [6.d Proceso transaccional de cobro y registro de venta paso a paso](capitulos/06_punto_de_venta.md#c6d)
  - [6.e Deshacer eliminación de ítems con Snackbar y eventos reactivos](capitulos/06_punto_de_venta.md#c6e)
  - [6.f Historial de ventas, filtrado por vendedor y agrupación por fecha](capitulos/06_punto_de_venta.md#c6f)
- [7. Productos](capitulos/07_productos.md#c7)
  - [7.a Listado de productos, búsqueda reactiva, ordenamiento y filtros](capitulos/07_productos.md#c7a)
  - [7.b Formulario de creación y edición de productos (ProductoFormScreen)](capitulos/07_productos.md#c7b)
  - [7.c Algoritmo de generación de código interno EAN-13](capitulos/07_productos.md#c7c)
  - [7.d Generación de imagen Bitmap y vista previa de código de barras](capitulos/07_productos.md#c7d)
  - [7.e Operación de adición rápida de stock y eliminación suave](capitulos/07_productos.md#c7e)
- [8. Catálogos](capitulos/08_catalogos.md#c8)
  - [8.a Gestión de categorías de productos (CatalogosScreen / CatalogosViewModel)](capitulos/08_catalogos.md#c8a)
  - [8.b Gestión de proveedores y datos de contacto](capitulos/08_catalogos.md#c8b)
  - [8.c Sistema de códigos internos consecutivos de 2 dígitos](capitulos/08_catalogos.md#c8c)
  - [8.d Reglas de integridad referencial y bloqueo de eliminación](capitulos/08_catalogos.md#c8d)
- [9. Escáner de códigos de barras](capitulos/09_escaner_barras.md#c9)
  - [9.a Integración de CameraX y procesamiento de cámara (ScannerScreen)](capitulos/09_escaner_barras.md#c9a)
  - [9.b Escaneo e interpretación de códigos de barras con ML Kit](capitulos/09_escaner_barras.md#c9b)
  - [9.c Gestión de permisos de cámara y fallback de linterna](capitulos/09_escaner_barras.md#c9c)
  - [9.d Interfaz visual de escaneo y retorno de resultados](capitulos/09_escaner_barras.md#c9d)
- [10. Reportes](capitulos/10_reportes.md#c10)
  - [10.a Métricas clave de rendimiento (KPIs) e indicadores de negocio](capitulos/10_reportes.md#c10a)
  - [10.b Selección de períodos y cálculo de rangos de fechas (DateTimeUtils)](capitulos/10_reportes.md#c10b)
  - [10.c Gráficos estadísticos personalizados en Canvas (CustomBarChart)](capitulos/10_reportes.md#c10c)
  - [10.d Análisis de productos más vendidos (Top 5) y alertas de stock bajo](capitulos/10_reportes.md#c10d)
  - [10.e Exportación de reportes estadísticos en formato CSV](capitulos/10_reportes.md#c10e)
- [11. Gestión de usuarios y Ajustes](capitulos/11_usuarios_ajustes.md#c11)
  - [11.a Gestión de usuarios y asignación de roles (UsuariosScreen / UsuariosViewModel)](capitulos/11_usuarios_ajustes.md#c11a)
  - [11.b Control de estado de usuarios (Activo / Inactivo) y reglas de auto-modificación](capitulos/11_usuarios_ajustes.md#c11b)
  - [11.c Configuración de apariencia y temas de interfaz (AjustesScreen / AjustesViewModel)](capitulos/11_usuarios_ajustes.md#c11c)
  - [11.d Panel de Ajustes Avanzados para administración del sistema](capitulos/11_usuarios_ajustes.md#c11d)
- [12. Respaldos y gestión de datos](capitulos/12_respaldos_gestion_datos.md#c12)
  - [12.a Arquitectura de respaldos JSON e integridad de datos (RespaldoJsonManager)](capitulos/12_respaldos_gestion_datos.md#c12a)
  - [12.b Proceso de exportación de respaldos paso a paso](capitulos/12_respaldos_gestion_datos.md#c12b)
  - [12.c Proceso de importación de respaldos, resolución de duplicados y mapeo de IDs](capitulos/12_respaldos_gestion_datos.md#c12c)
  - [12.d Procedimiento de eliminación total de datos](capitulos/12_respaldos_gestion_datos.md#c12d)
  - [12.e Gestión de almacenamiento externo, rutas y permisos por versión de Android](capitulos/12_respaldos_gestion_datos.md#c12e)
- [13. Interfaz de usuario](capitulos/13_interfaz_usuario.md#c13)
  - [13.a Sistema de diseño Material 3 y esquema de colores semánticos (Theme / Color / Type)](capitulos/13_interfaz_usuario.md#c13a)
  - [13.b Soporte completo para tema claro y oscuro](capitulos/13_interfaz_usuario.md#c13b)
  - [13.c Componentes UI reutilizables y contenedores principales (Componentes.kt)](capitulos/13_interfaz_usuario.md#c13c)
  - [13.d Estructura base de pantallas (AppScaffold) y manejo de insets de sistema](capitulos/13_interfaz_usuario.md#c13d)
- [14. Ciclo de vida de los datos](capitulos/14_ciclo_vida_datos.md#c14)
  - [14.a Trazabilidad y flujo de vida de los datos de productos](capitulos/14_ciclo_vida_datos.md#c14a)
  - [14.b Trazabilidad y flujo de vida de los datos de ventas y transacciones](capitulos/14_ciclo_vida_datos.md#c14b)
  - [14.c Trazabilidad y flujo de vida de los datos de categorías y proveedores](capitulos/14_ciclo_vida_datos.md#c14c)
  - [14.d Trazabilidad y flujo de vida de los datos de usuarios y sesión](capitulos/14_ciclo_vida_datos.md#c14d)
  - [14.e Trazabilidad y flujo de vida de las preferencias y archivos de respaldo](capitulos/14_ciclo_vida_datos.md#c14e)
- [15. Pruebas](capitulos/15_pruebas.md#c15)
  - [15.a Estrategia de pruebas unitarias y cobertura en capa Util y ViewModel](capitulos/15_pruebas.md#c15a)
  - [15.b Pruebas de generador de códigos de barras e integridad EAN-13](capitulos/15_pruebas.md#c15b)
  - [15.c Pruebas de validación e importación/exportación JSON](capitulos/15_pruebas.md#c15c)
  - [15.d Pruebas de cálculo de rangos de fechas y zonas horarias](capitulos/15_pruebas.md#c15d)
  - [15.e Pruebas de instrumentalización Android (androidTest) y ejecución](capitulos/15_pruebas.md#c15e)
- [16. Decisiones de diseño, limitaciones y observaciones](capitulos/16_decisiones_diseno_observaciones.md#c16)
  - [16.a Justificación de decisiones de diseño arquitectónico y técnico](capitulos/16_decisiones_diseno_observaciones.md#c16a)
  - [16.b Limitaciones conocidas del sistema](capitulos/16_decisiones_diseno_observaciones.md#c16b)
  - [16.c Inconsistencias, código legacy y hallazgos en la base de código](capitulos/16_decisiones_diseno_observaciones.md#c16c)
  - [16.d Elementos pendientes de verificación y observaciones finales ("⚠ Por verificar")](capitulos/16_decisiones_diseno_observaciones.md#c16d)
- [17. Apéndices](capitulos/17_apendices.md#c17)
  - [17.a Mapeo completo de archivos, clases y capítulos](capitulos/17_apendices.md#c17a)
  - [17.b Tabla detallada de permisos Android requeridos](capitulos/17_apendices.md#c17b)
  - [17.c Tabla de constantes de configuración del sistema](capitulos/17_apendices.md#c17c)
  - [17.d Glosario de términos técnicos y conceptos del dominio](capitulos/17_apendices.md#c17d)

---

### Resumen de Archivos de la Documentación

Toda la documentación está contenida en los siguientes archivos físicos dentro del repositorio:
1. `docs/DOCUMENTACION_TECNICA.md` (Archivo Maestro e Índice)
2. `docs/capitulos/01_vision_general.md`
3. `docs/capitulos/02_arquitectura.md`
4. `docs/capitulos/03_base_de_datos.md`
5. `docs/capitulos/04_repositorios_reglas.md`
6. `docs/capitulos/05_seguridad_sesion_roles.md`
7. `docs/capitulos/06_punto_de_venta.md`
8. `docs/capitulos/07_productos.md`
9. `docs/capitulos/08_catalogos.md`
10. `docs/capitulos/09_escaner_barras.md`
11. `docs/capitulos/10_reportes.md`
12. `docs/capitulos/11_usuarios_ajustes.md`
13. `docs/capitulos/12_respaldos_gestion_datos.md`
14. `docs/capitulos/13_interfaz_usuario.md`
15. `docs/capitulos/14_ciclo_vida_datos.md`
16. `docs/capitulos/15_pruebas.md`
17. `docs/capitulos/16_decisiones_diseno_observaciones.md`
18. `docs/capitulos/17_apendices.md`
