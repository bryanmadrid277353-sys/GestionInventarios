[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [16.a Justificación de decisiones de diseño arquitectónico y técnico](#c16a)
- [16.b Limitaciones conocidas del sistema](#c16b)
- [16.c Inconsistencias, código legacy y hallazgos en la base de código](#c16c)
- [16.d Elementos pendientes de verificación y observaciones finales ("⚠ Por verificar")](#c16d)

---

<a id="c16"></a>
## 16. Decisiones de diseño, limitaciones y observaciones

En estricto apego a las reglas generales de documentación técnica, este capítulo registra las decisiones de diseño adoptadas, las limitaciones conocidas del sistema y los hallazgos u observaciones detectados en la base de código existente, **sin haber modificado ningún archivo fuente**.

---

<a id="c16a"></a>
### 16.a Justificación de decisiones de diseño arquitectónico y técnico

1. **Arquitectura 100 % Local (Offline-First / Serverless)**:
   - **Razón**: Las pequeñas empresas (abarrotes, ferreterías, tiendas de barrio) suelen operar en zonas con conectividad inestable o nula. Mantener la base de datos relacional localmente en el dispositivo garantiza una disponibilidad del 100 %, respuesta instantánea en el Punto de Venta sin latencia de red y cero costos de infraestructura o servidores en la nube.
2. **Inyección de Dependencias Manual (`AppContainer`)**:
   - **Razón**: Reducir la complejidad del proyecto y evitar los extensos tiempos de compilación asociados a procesadores de anotaciones pesados como Hilt/Dagger. La instanciación perezosa (`by lazy`) proporciona un control claro sobre la pila de objetos Singleton.
3. **Desnormalización de Nombres en Tablas de SQLite**:
   - **Razón**: `ProductoEntity` almacena `categoriaNombre` y `proveedorNombre`, y `VentaEntity` almacena `usuarioNombre`. Esto permite realizar consultas rápidas en el Punto de Venta e Historial sin necesidad de ejecutar sentencias `JOIN` complejas en cada renderizado de Jetpack Compose, mejorando el rendimiento en dispositivos de gama baja.
4. **Respaldos en JSON Streaming**:
   - **Razón**: El uso de `JsonWriter` y `JsonReader` permite exportar e importar miles de registros leyendo y escribiendo de forma secuencial directamente en los flujos de archivos (`OutputStream` / `InputStream`), evitando cargar archivos JSON masivos en la memoria RAM y previniendo excepciones `OutOfMemoryError`.

---

<a id="c16b"></a>
### 16.b Limitaciones conocidas del sistema

1. **Capacidad de Códigos Internos (Límite de 99)**:
   - La estructura del código interno EAN-13 asigna exactamente 2 dígitos para el código de categoría (`01` a `99`) y 2 dígitos para el código de proveedor (`01` a `99`). Por lo tanto, la aplicación no puede registrar más de 99 categorías ni más de 99 proveedores.
2. **Impresión Física de Tickets**:
   - La aplicación genera un comprobante visual en pantalla y permite compartirlo en texto plano mediante intents del sistema, pero no incluye un driver o SDK nativo para impresión directa vía Bluetooth POS / ESC-POS.
3. **Multiusuario Simultáneo**:
   - Al ser una aplicación 100 % local orientada a un solo dispositivo (teléfono o tablet de caja), no existe sincronización en tiempo real entre múltiples dispositivos físicos.

---

<a id="c16c"></a>
### 16.c Inconsistencias, código legacy y hallazgos en la base de código

Durante la exploración y auditoría estricta del código fuente, se identificaron los siguientes hallazgos técnicos:

1. **Almacenamiento de Contraseñas en Texto Plano en SQLite**:
   - **Ubicación**: [UsuarioEntity.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/local/entity/UsuarioEntity.kt) y [AuthRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/AuthRepository.kt).
   - **Hallazgo**: La entidad `UsuarioEntity` posee la columna `passwordHash: String`. Sin embargo, durante la siembra de datos (`AppDatabase.seedInitialData`) y la creación de usuarios (`UsuarioRepository.crearUsuario`), la contraseña se almacena en texto plano (ej. `"admin123"`) y en `AuthRepository.login` se compara mediante igualdad directa `user.passwordHash == password` en lugar de aplicar un algoritmo de hashing seguro (como BCrypt, Argon2 o PBKDF2).
2. **Residuos de Clave Legacy en TokenStore**:
   - **Ubicación**: [TokenStore.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/local/TokenStore.kt).
   - **Hallazgo**: La clase conserva la constante `LEGACY_LAST_LOGIN_EMAIL_KEY = stringPreferencesKey("last_login_email")` y el método `cleanupLegacyData()` para eliminar dicha clave obsoleta al arrancar la app en `GestionInventariosApp.kt`.
3. **Manejo de Errores Silencioso en `seedInitialData`**:
   - **Ubicación**: [AppDatabase.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/local/AppDatabase.kt).
   - **Hallazgo**: La función de sembrado inicial envuelve todo el proceso en un bloque `try { ... } catch (ignored: Exception) {}` que atrapa cualquier excepción sin registrarla en el logcat, lo que podría ocultar fallas si SQLite encuentra un error durante el primer arranque.
4. **Residuos de Layout XML No Utilizados**:
   - **Ubicación**: [res/layout/activity_main.xml](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/res/layout/activity_main.xml).
   - **Hallazgo**: El proyecto contiene el archivo de layout XML `activity_main.xml`, a pesar de que la interfaz está construida al 100 % con Jetpack Compose y `MainActivity` utiliza `setContent { ... }` sin inflar ningún layout XML.

---

<a id="c16d"></a>
### 16.d Elementos pendientes de verificación y observaciones finales ("⚠ Por verificar")

A continuación se consolidan los puntos que requieren atención o verificación por parte del equipo de desarrollo en futuras versiones:

- ⚠ **Por verificar (Seguridad)**: Se recomienda implementar un algoritmo de hash criptográfico salteado (ej. BCrypt o Argon2) para la columna `passwordHash` en `UsuarioEntity` antes de pasar la aplicación a un entorno de producción real.
- ⚠ **Por verificar (Almacenamiento Android 11+)**: El permiso `MANAGE_EXTERNAL_STORAGE` requiere una justificación especial ante Google Play Store. Si la aplicación se va a publicar en la tienda oficial, debe verificarse si el uso del *Storage Access Framework* (SAF) estándar es suficiente para evitar el rechazo de la política de permisos de almacenamiento de Google Play.
- ⚠ **Por verificar (Limpieza)**: Evaluar la eliminación del archivo redundante `res/layout/activity_main.xml` para mantener limpio el directorio de recursos.

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
