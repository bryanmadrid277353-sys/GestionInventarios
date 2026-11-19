[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [14.a Trazabilidad y flujo de vida de los datos de productos](#c14a)
- [14.b Trazabilidad y flujo de vida de los datos de ventas y transacciones](#c14b)
- [14.c Trazabilidad y flujo de vida de los datos de categorías y proveedores](#c14c)
- [14.d Trazabilidad y flujo de vida de los datos de usuarios y sesión](#c14d)
- [14.e Trazabilidad y flujo de vida de las preferencias y archivos de respaldo](#c14e)

---

<a id="c14"></a>
## 14. Ciclo de vida de los datos

Este capítulo describe la trazabilidad completa de cada entidad de datos en el sistema: dónde nace, dónde se almacena, qué componentes la leen o modifican, su formato de representación y cómo se respalda o elimina.

---

<a id="c14a"></a>
### 14.a Trazabilidad y flujo de vida de los datos de productos

#### Ciclo de Vida:
1. **Nacimiento (Origen)**:
   - Ingresado manualmente por el usuario `ADMIN` en `ProductoFormScreen`.
   - Escaneado mediante `ScannerScreen` (asigna el código de barras obtenido).
   - Generado automáticamente mediante `CodigoInterno.generar(...)` (EAN-13 interno).
   - Sembrado inicial en `AppDatabase.seedInitialData`.
   - Importación masiva desde archivo JSON en `AjustesAvanzadosViewModel`.
2. **Almacenamiento**:
   - Tabla SQLite `productos` a través de `ProductoDao.insertProducto`.
   - Representación en memoria mediante `ProductoEntity` y `ProductoDto`.
3. **Lectura y Modificación**:
   - Lectura: `ProductosScreen` (inventario), `VentaScreen` (búsqueda y carrito), `ReportesScreen` (alertas de stock bajo).
   - Modificación: `ProductoFormScreen` (edición de precios/nombres) y `ProductosScreen` (adición rápida de stock).
   - Descuento de Existencias: Al confirmar una venta en `VentaRepository.crearVenta`, el stock decrementa automáticamente (`updateStock`).
4. **Respaldo y Eliminación**:
   - Respaldo: Exportado en el nodo `"productos"` del archivo JSON por `RespaldoJsonManager`.
   - Eliminación: Borrado lógico (`activo = 0`). El registro físico se conserva en SQLite para mantener la integridad de reportes de ventas históricas.

---

<a id="c14b"></a>
### 14.b Trazabilidad y flujo de vida de los datos de ventas y transacciones

#### Ciclo de Vida:
1. **Nacimiento (Origen)**:
   - Creado en el Punto de Venta (`VentaScreen`) al pulsar *"COBRAR"* y confirmar el pago.
2. **Almacenamiento**:
   - Tablas SQLite `ventas` (encabezado) y `detalle_ventas` (partidas) mediante `VentaDao.insertVenta` e `insertDetalles` dentro de una transacción atómica.
3. **Lectura y Consulta**:
   - `VentaScreen` (pestaña Historial): Muestra las ventas agrupadas por fecha.
   - `ReportesScreen`: Agrupa las ventas por día/semana/mes para calcular KPIs y dibujar gráficos en Canvas.
   - `HomeScreen`: Calcula la suma de ventas del día en curso.
4. **Respaldo y Eliminación**:
   - Respaldo: Exportado en los nodos `"ventas"` y `"detalle_venta"` del archivo JSON. Incluye los campos desnormalizados `vendedorCorreo` y `vendedorNombre`.
   - Eliminación: Se eliminan únicamente mediante el procedimiento de *"Eliminar todos los datos"* en Ajustes Avanzados. La eliminación de un encabezado de venta borra automáticamente sus renglones por la clave foránea `ON DELETE CASCADE`.

---

<a id="c14c"></a>
### 14.c Trazabilidad y flujo de vida de los datos de categorías y proveedores

#### Ciclo de Vida:
1. **Nacimiento (Origen)**:
   - Formulario emergente `FormularioCategoriaBottomSheet` / `FormularioProveedorBottomSheet` en `CatalogosScreen`.
   - Sembrado inicial en `AppDatabase`.
   - Importación desde archivo JSON.
2. **Almacenamiento**:
   - Tablas SQLite `categorias` y `proveedores` con código único (1 a 99).
3. **Lectura y Referencia**:
   - `CatalogosScreen`: Listado con conteo de productos asociados.
   - `ProductoFormScreen`: Desplegables de selección para vincular el producto.
   - `CodigoInterno`: Lectura de los códigos de 2 dígitos para construir el prefijo EAN-13.
4. **Respaldo y Eliminación**:
   - Respaldo: Exportado en los nodos `"categorias"` y `"proveedores"`.
   - Eliminación: Borrado lógico (`activo = 0`). Bloqueado si la categoría o proveedor posee productos activos vinculados.

---

<a id="c14d"></a>
### 14.d Trazabilidad y flujo de vida de los datos de usuarios y sesión

#### Ciclo de Vida:
1. **Nacimiento (Origen)**:
   - `UsuariosScreen` (alta por usuario `ADMIN`).
   - Cuentas demo sembradas en `AppDatabase.seedInitialData`.
2. **Almacenamiento**:
   - Cuentas: Tabla SQLite `usuarios`.
   - Sesión Activa: Archivo `user_session.preferences` mediante `TokenStore` (DataStore Preferences).
3. **Lectura y Autenticación**:
   - `AuthRepository.login`: Valida credenciales contra `usuarios` y escribe en `TokenStore`.
   - `NavGraph` y `HomeScreen`: Leen el estado de sesión (`isLoggedIn`, `userRole`, `userName`) para personalizar la interfaz.
   - `SesionManager`: Monitorea la marca de tiempo de inactividad (`KEY_ULTIMA_ACTIVIDAD`).
4. **Respaldo y Eliminación**:
   - **Exclusión de Respaldos**: Las cuentas de usuario **NO SE EXPORTAN NI SE IMPORTAN** en respaldos JSON versión 2 para evitar riesgos de seguridad.
   - Preservación en Borrado Total: El procedimiento de *"Eliminar todos los datos"* conserva intacta la tabla `usuarios`.

---

<a id="c14e"></a>
### 14.e Trazabilidad y flujo de vida de las preferencias y archivos de respaldo

#### Ciclo de Vida:
1. **Preferencias Generales ([PreferenciasManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/local/PreferenciasManager.kt))**:
   - Almacenado en `preferencias_inventario_pyme.xml` (`SharedPreferences`).
   - Guarda el modo de tema (`modo_tema`), bandera de datos sembrados (`datos_sembrados`), fecha (`ultimo_respaldo_fecha`) y ruta legible (`ultimo_respaldo_ubicacion`) de la última exportación.
2. **Archivos de Respaldo JSON**:
   - Nacimiento: Creados por `AlmacenRespaldos.guardarRespaldoJsonVerificado` como archivos `.tmp` y renombrados a `.json` tras superar la verificación estricta de checksum/conteos.
   - Ubicación: En la raíz del almacenamiento externo (`/storage/emulated/0/Inventario PYME/Respaldos de Datos/`).
   - Lectura: Seleccionados por el usuario mediante `OpenDocumentInBackupFolderContract` para restauración.

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
