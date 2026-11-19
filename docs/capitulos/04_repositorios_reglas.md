[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [4.a Repositorio de Autenticación (AuthRepository)](#c4a)
- [4.b Repositorio de Productos (ProductoRepository)](#c4b)
- [4.c Repositorio de Categorías (CategoriaRepository)](#c4c)
- [4.d Repositorio de Proveedores (ProveedorRepository)](#c4d)
- [4.e Repositorio de Ventas (VentaRepository)](#c4e)
- [4.f Repositorio de Reportes (ReporteRepository)](#c4f)
- [4.g Repositorio de Usuarios (UsuarioRepository)](#c4g)

---

<a id="c4"></a>
## 4. Repositorio y reglas de negocio

En este capítulo se detallan las firmas, propósitos, parámetros, retornos, validaciones, reglas de negocio y efectos secundarios de cada uno de los 7 repositorios de la aplicación.

---

<a id="c4a"></a>
### 4.a Repositorio de Autenticación (AuthRepository)

**Archivo**: [data/repository/AuthRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/AuthRepository.kt)  
**Dependencias**: `UsuarioDao`, `TokenStore`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `login(email, password)` | Autentica a un usuario y guarda la sesión activa en DataStore. | `email: String`<br>`password: String` | `Resource<UsuarioResponseDto>` | **Reglas**: Busca el usuario por correo sin espacios iniciales/finales. Verifica que exista y que `activo == true`. Valida `passwordHash == password`.<br>**Efectos**: Guarda `userId`, `nombre`, `email` y `rol` en `TokenStore`. |
| `logout()` | Cierra la sesión del usuario activo. | Ninguno | `Resource<Unit>` | **Efectos**: Invoca `tokenStore.clearSession()`, eliminando las claves almacenadas en DataStore. |
| `cleanupLegacyData()` | Elimina claves obsoletas de versiones anteriores. | Ninguno | `Unit` | **Efectos**: Invoca `tokenStore.cleanupLegacyData()`. |

---

<a id="c4b"></a>
### 4.b Repositorio de Productos (ProductoRepository)

**Archivo**: [data/repository/ProductoRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/ProductoRepository.kt)  
**Dependencias**: `ProductoDao`, `CategoriaDao`, `ProveedorDao`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `getProductosFromCache(search, categoriaId)` | Filtra y retorna los productos activos desde la base de datos. | `search: String?`<br>`categoriaId: Int?` | `List<ProductoDto>` | **Reglas**: Filtra por `categoriaId` si no es nulo. Filtra por nombre o código de barras (case-insensitive) si `search` no es nulo ni vacío. |
| `generarCodigoInterno(categoriaId, proveedorId)` | Genera un código de barras EAN-13 automático estructurado. | `categoriaId: Int?`<br>`proveedorId: Int?` | `String` (13 dígitos) | **Reglas**: Obtiene los códigos numéricos de categoría y proveedor. Construye el prefijo `"2" + catCodigo(2d) + provCodigo(2d)`. Obtiene el consecutivo más alto para dicho prefijo y calcula el dígito verificador. |
| `listarProductos(search, categoriaId)` | Envuelve el listado filtrado de productos en un recurso de respuesta. | `search: String?`<br>`categoriaId: Int?` | `Resource<List<ProductoDto>>` | Retorna `Resource.Success` con los productos o `Resource.Error` ante excepciones. |
| `obtenerProducto(id)` | Obtiene el detalle de un producto específico. | `id: Int` | `Resource<ProductoDto>` | **Reglas**: Verifica que el producto exista en la base de datos y que su estado sea `activo == true`. |
| `obtenerPorCodigoBarras(codigo)` | Busca un producto por su código de barras. | `codigo: String` | `Resource<ProductoDto>` | **Reglas**: Aplica `trim()` al código ingresado y busca coincidencia exacta con productos activos. |
| `crearProducto(createDto)` | Registra un nuevo producto en la base de datos. | `createDto: ProductoCreateDto` | `Resource<ProductoDto>` | **Efectos**: Calcula el nuevo `id = max(id) + 1`. Desnormaliza los nombres de categoría y proveedor en la entidad `ProductoEntity`. |
| `actualizarProducto(id, updateDto)` | Actualiza los datos de un producto existente. | `id: Int`<br>`updateDto: ProductoUpdateDto` | `Resource<ProductoDto>` | **Reglas**: Verifica la existencia del producto. Desnormaliza los nuevos nombres de categoría y proveedor. |
| `addStock(id, cantidadAnadir)` | Incrementa las existencias de un producto. | `id: Int`<br>`cantidadAnadir: Int` | `Resource<ProductoDto>` | **Efectos**: Calcula `nuevoStock = stock + cantidadAnadir` y ejecuta `updateStock(id, nuevoStock)` en la base de datos. |
| `eliminarProducto(id)` | Realiza un borrado lógico del producto. | `id: Int` | `Resource<Unit>` | **Efectos**: Ejecuta `UPDATE productos SET activo = 0 WHERE id = :id`. No elimina el registro físicamente para preservar el historial de ventas. |

---

<a id="c4c"></a>
### 4.c Repositorio de Categorías (CategoriaRepository)

**Archivo**: [data/repository/CategoriaRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/CategoriaRepository.kt)  
**Dependencias**: `CategoriaDao`, `ProductoDao`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `obtenerSiguienteCodigo()` | Calcula el código numérico consecutivo para una nueva categoría. | Ninguno | `Int` | **Reglas**: Busca `max(codigo)` en la tabla `categorias`. Si es nulo retorna 1, de lo contrario `max + 1`. |
| `listarCategorias()` | Obtiene la lista de categorías activas ordenadas alfabéticamente. | Ninguno | `Resource<List<CategoriaDto>>` | Retorna `Resource.Success` con la lista de DTOs. |
| `crearCategoria(nombre, descripcion)` | Registra una nueva categoría. | `nombre: String`<br>`descripcion: String?` | `Resource<CategoriaDto>` | **Reglas**: Si el siguiente código excede de 99, retorna error: *"Se alcanzó el límite de 99 categorías"*. Asigna el id incremental. |
| `actualizarCategoria(id, nombre, descripcion)` | Actualiza el nombre y descripción de una categoría. | `id: Int`<br>`nombre: String`<br>`descripcion: String?` | `Resource<CategoriaDto>` | **Efectos**: Ejecuta `categoriaDao.updateCategoria`. |
| `eliminarCategoria(id)` | Desactiva una categoría mediante borrado lógico. | `id: Int` | `Resource<Unit>` | **Reglas (Integridad Referencial)**: Consulta a `productoDao.getCountByCategoria(id)`. Si es mayor a 0, bloquea la eliminación retornando error: *"No se puede eliminar la categoría porque tiene productos asociados"*. |

---

<a id="c4d"></a>
### 4.d Repositorio de Proveedores (ProveedorRepository)

**Archivo**: [data/repository/ProveedorRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/ProveedorRepository.kt)  
**Dependencias**: `ProveedorDao`, `ProductoDao`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `obtenerSiguienteCodigo()` | Calcula el código consecutivo para un nuevo proveedor. | Ninguno | `Int` | Retorna `max(codigo) + 1` (rango 1 a 99). |
| `listarProveedores()` | Obtiene los proveedores activos ordenados alfabéticamente. | Ninguno | `Resource<List<ProveedorDto>>` | Retorna `Resource.Success` con la lista de DTOs. |
| `crearProveedor(nombre, contacto, telefono, email)` | Registra un nuevo proveedor. | `nombre: String`<br>`contacto: String?`<br>`telefono: String?`<br>`email: String?` | `Resource<ProveedorDto>` | **Reglas**: Limite máximo de 99 proveedores. Aplica `trim()` a todas las cadenas. |
| `actualizarProveedor(...)` | Actualiza la información de un proveedor. | `id: Int`, campos de texto | `Resource<ProveedorDto>` | Actualiza la entidad en la base de datos local. |
| `eliminarProveedor(id)` | Desactiva un proveedor mediante borrado lógico. | `id: Int` | `Resource<Unit>` | **Reglas (Integridad Referencial)**: Valida `productoDao.getCountByProveedor(id)`. Si hay productos asociados, bloquea la operación retornando error: *"No se puede eliminar el proveedor porque tiene productos asociados"*. |

---

<a id="c4e"></a>
### 4.e Repositorio de Ventas (VentaRepository)

**Archivo**: [data/repository/VentaRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/VentaRepository.kt)  
**Dependencias**: `VentaDao`, `ProductoDao`, `TokenStore`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `crearVenta(detalles)` | Procesa y registra una venta transaccional completa. | `detalles: List<DetalleVentaCreateDto>` | `Resource<VentaResponseDto>` | **Paso 1**: Valida que `detalles` no esté vacío.<br>**Paso 2**: Lee el ID y nombre del vendedor activo desde `tokenStore`.<br>**Paso 3**: Por cada producto, valida existencia y que `prod.stock >= cantidad`. Si no hay stock suficiente, aborta la venta.<br>**Paso 4**: Calcula subtotales y total general.<br>**Paso 5**: Inserta `VentaEntity` y obtiene `ventaId`.<br>**Paso 6**: Inserta los renglones `DetalleVentaEntity` asociados a `ventaId`.<br>**Paso 7**: Descuenta el stock de cada producto en la base de datos (`updateStock`). |
| `listarVentas()` | Obtiene el historial completo de ventas registradas. | Ninguno | `Resource<List<VentaResponseDto>>` | Invoca `ventaDao.getVentasWithDetalles()` ordenadas de forma descendente por fecha. |
| `obtenerVenta(id)` | Obtiene una venta específica con su desglose de partidas. | `id: Int` | `Resource<VentaResponseDto>` | Retorna la venta correspondiente al ID proporcionado. |

---

<a id="c4f"></a>
### 4.f Repositorio de Reportes (ReporteRepository)

**Archivo**: [data/repository/ReporteRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/ReporteRepository.kt)  
**Dependencias**: `VentaDao`, `ProductoDao`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `obtenerVentasPorDia(desde)` | Agrupa y suma las ventas registradas por día calendario. | `desde: String?` | `Resource<List<ReporteVentasDiaDto>>` | **Reglas**: Filtra las ventas cuya fecha en milisegundos sea `>= desde`. Agrupa los registros por la fecha local resultante (`YYYY-MM-DD`) y calcula `sumOf { total }` y `count()`. |
| `obtenerConteoStockBajo()` | Cuenta los productos que requieren reabastecimiento. | Ninguno | `Int` | Ejecuta `productoDao.getCountStockBajo()`. |
| `obtenerStockBajo()` | Lista los productos con stock menor o igual a su stock mínimo. | Ninguno | `Resource<List<ReporteStockBajoDto>>` | Retorna los productos bajo el umbral de reabastecimiento. |

---

<a id="c4g"></a>
### 4.g Repositorio de Usuarios (UsuarioRepository)

**Archivo**: [data/repository/UsuarioRepository.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/repository/UsuarioRepository.kt)  
**Dependencias**: `UsuarioDao`

| Nombre y Firma | Qué hace | Parámetros | Retorna | Efectos y Reglas de Negocio |
| :--- | :--- | :--- | :--- | :--- |
| `listarUsuarios()` | Obtiene todas las cuentas de usuario registradas en el sistema. | Ninguno | `Resource<List<UsuarioResponseDto>>` | Invoca `usuarioDao.getUsuarios()` y los mapea a DTO. |
| `crearUsuario(createDto)` | Registra una nueva cuenta de usuario. | `createDto: UsuarioCreateDto` | `Resource<UsuarioResponseDto>` | **Reglas**: Verifica que el correo electrónico no esté registrado previamente (`getUsuarioByEmail`). Si existe, retorna error: *"El correo electrónico ya está registrado"*. |
| `desactivarUsuario(id)` | Alterna el estado de activación (`activo = !activo`) de un usuario. | `id: Int` | `Resource<UsuarioResponseDto>` | **Reglas**: Invoca `usuarioDao.getUsuarioById(id)`, invierte el valor del booleano `activo` y guarda los cambios. |

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
