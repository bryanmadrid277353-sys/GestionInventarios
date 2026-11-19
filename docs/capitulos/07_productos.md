[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [7.a Listado de productos, búsqueda reactiva, ordenamiento y filtros](#c7a)
- [7.b Formulario de creación y edición de productos (ProductoFormScreen)](#c7b)
- [7.c Algoritmo de generación de código interno EAN-13](#c7c)
- [7.d Generación de imagen Bitmap y vista previa de código de barras](#c7d)
- [7.e Operación de adición rápida de stock y eliminación suave](#c7e)

---

<a id="c7"></a>
## 7. Productos

<a id="c7a"></a>
### 7.a Listado de productos, búsqueda reactiva, ordenamiento y filtros

El módulo de administración de inventario se implementa en [ProductosScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/productos/ProductosScreen.kt) y [ProductosViewModel.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/productos/ProductosViewModel.kt).

#### Mecanismos de Filtrado y Ordenamiento:
1. **Búsqueda por Texto**: Campo de búsqueda reactivo que filtra en tiempo real por el nombre del producto o código de barras (case-insensitive).
2. **Filtro por Categorías**: Barra de desplazamiento horizontal (`LazyRow`) con chips interactivos (`FilterChip`) para filtrar productos pertenecientes a una categoría específica o la opción "Todas".
3. **Filtro de Stock Bajo**: Chip conmutable `"Solo stock bajo"` que filtra los productos cuyo inventario actual sea menor o igual al umbral mínimo (`stock <= stockMinimo`).
4. **Criterios de Ordenación (`OrdenProducto`)**:
   - `NOMBRE`: Orden alfabético ascendente por nombre (`A-Z`).
   - `STOCK_ASC`: Orden por existencias de menor a mayor.
   - `PRECIO_ASC`: Orden por precio de venta de menor a mayor.
   - `PRECIO_DESC`: Orden por precio de venta de mayor a menor.

---

<a id="c7b"></a>
### 7.b Formulario de creación y edición de productos (ProductoFormScreen)

El formulario de alta y modificación de productos reside en [ProductoFormScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/productos/ProductoFormScreen.kt) y [ProductoFormViewModel.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/productos/ProductoFormViewModel.kt).

#### Validaciones del Formulario:
- **Categoría y Proveedor**: Campos de selección obligatorios (`ExposedDropdownMenuBox`).
- **Código de Barras**: Campo requerido. Si el código ingresado tiene 13 dígitos numéricos, debe superar la validación del algoritmo del dígito verificador EAN-13 ([Ean13Validator.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/Ean13Validator.kt)).
- **Detección de Duplicados en Tiempo Real**: Al ingresar o escanear un código de barras, `verificarCodigoDuplicado` consulta la base de datos. Si el código ya pertenece a otro producto, muestra un aviso de advertencia con la opción de navegar a editar el producto existente.
- **Nombre**: Requerido. No puede ser una cadena vacía.
- **Precio y Costo**: `precio` debe ser mayor a 0. `costo` debe ser mayor o igual a 0. Se valida estrictamente que `precio >= costo`. Si el costo es válido, la UI calcula y muestra dinámicamente el chip con el **Margen de Ganancia**:
  $$\text{Margen (\%)} = \frac{\text{Precio} - \text{Costo}}{\text{Costo}} \times 100$$
- **Stock y Stock Mínimo**: El stock actual y el stock mínimo deben ser números enteros mayores o iguales a 0.

---

<a id="c7c"></a>
### 7.c Algoritmo de generación de código interno EAN-13

Cuando un producto carece de un código de barras comercial impreso en su empaque, la aplicación permite generar un código de barras interno estandarizado EAN-13 utilizando el módulo [CodigoInterno.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/CodigoInterno.kt).

#### Estructura del Código Interno de 13 Dígitos:
```
 [ 2 ] [ Cat (2d) ] [ Prov (2d) ] [ Consecutivo (7d) ] [ DV (1d) ]
  |          |            |                |                |
Dígito 1  Dígitos 2-3  Dígitos 4-5     Dígitos 6-12        Dígito 13
```

1. **Dígito 1 (Bandera de Código Interno)**: Inicia fixed con el dígito `'2'` (prefijo asignado por la norma EAN/GS1 para circulación interna en establecimientos).
2. **Dígitos 2 y 3 (Categoría)**: Código numérico de 2 dígitos de la categoría asignada (del `00` al `99`).
3. **Dígitos 4 y 5 (Proveedor)**: Código numérico de 2 dígitos del proveedor asignado (del `00` al `99`).
4. **Dígitos 6 a 12 (Consecutivo)**: Secuencia numérica de 7 dígitos (del `0000001` al `9999999`) que se autoincrementa consultando el valor máximo existente en la base de datos para ese prefijo de 5 dígitos (`productoDao.getMaxConsecutivoByPrefijo`).
5. **Dígito 13 (Dígito Verificador - Check Digit)**: Calculado mediante el algoritmo estándar de suma ponderada EAN-13.

#### Algoritmo de Cálculo del Dígito Verificador:
Para los primeros 12 dígitos, se alternan pesos de 1 y 3 (posiciones impares peso 1, posiciones pares peso 3, índice base 0):
$$\text{Suma} = \sum_{i=0}^{11} \text{Dígito}[i] \times (1 \text{ si } i \text{ es par else } 3)$$
$$\text{Dígito Verificador} = (10 - (\text{Suma} \pmod{10})) \pmod{10}$$

#### Ejemplos de Códigos Internos Generados:
- **Categoría 1 (Bebidas), Proveedor 1 (Distribuidora del Norte), Consecutivo 1**:
  - Primeros 12 dígitos: `201010000001`
  - Suma ponderada: $(2\times 1) + (0\times 3) + (1\times 1) + (0\times 3) + (1\times 1) + (0\times 3) + (0\times 1) + (0\times 3) + (0\times 1) + (0\times 3) + (0\times 1) + (1\times 3) = 2 + 0 + 1 + 0 + 1 + 0 + 0 + 0 + 0 + 0 + 0 + 3 = 7$
  - Dígito Verificador: $(10 - (7 \pmod{10})) \pmod{10} = 3$
  - Código EAN-13 Resultante: `2010100000013`
  - Formato de Visualización Bonita (`CodigoInterno.formatear`): `"2 01 01 0000001 3"`

- **Categoría 2 (Abarrotes), Proveedor 2 (Comercializadora Central), Consecutivo 1**:
  - Primeros 12 dígitos: `202020000001` -> Dígito Verificador: `1` -> Código Resultante: `2020200000011`

---

<a id="c7d"></a>
### 7.d Generación de imagen Bitmap y vista previa de código de barras

Para permitir la lectura física o la impresión de etiquetas, la aplicación incluye el generador gráfico [BarcodeGenerator.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/util/BarcodeGenerator.kt).

#### Proceso de Generación Gráfica:
1. Recibe la cadena de texto del código de barras.
2. Determina la simbología a utilizar: Si el código es un EAN-13 válido (`esEan13Valido == true`), utiliza `BarcodeFormat.EAN_13`. En caso contrario (códigos alfanuméricos o cortos), utiliza `BarcodeFormat.CODE_128`.
3. Invoca la librería **ZXing** (`MultiFormatWriter().encode(...)`) para construir la matriz de bits de código de barras (`BitMatrix`) con dimensiones de 600x200 píxeles.
4. Recorre la matriz de bits y crea un objeto `android.graphics.Bitmap` de formato `ARGB_8888`, asignando píxeles negros (`Color.BLACK`) en las posiciones activas de la barra y píxeles blancos (`Color.WHITE`) en los espacios de fondo.
5. El Bitmap resultante se renderiza en la UI mediante `Image(bitmap.asImageBitmap(), ...)` dentro del modal `VistaPreviaCodigoBarrasDialog`.

---

<a id="c7e"></a>
### 7.e Operación de adición rápida de stock y eliminación suave

#### 1. Adición Rápida de Stock (`AnadirStockDialog`):
Permite incrementar las existencias de un producto sin ingresar al formulario de edición completa. El administrador presiona la opción *"Añadir Stock"* en el menú del producto, ingresa la cantidad a sumar en un campo numérico y confirma. El ViewModel invoca `productoRepository.addStock(id, cantidad)`, el cual actualiza el stock directamente en SQLite ejecutando `UPDATE productos SET stock = stock + :cantidad WHERE id = :id`.

#### 2. Eliminación Suave (Soft Delete):
Para preservar la integridad referencial del historial de ventas pasadas, la aplicación no borra físicamente los registros de la tabla `productos`. Al eliminar un producto, el repositorio ejecuta `productoDao.softDeleteProducto(id)`, el cual actualiza la columna `activo = 0`. Los productos con `activo = 0` quedan excluidos de las listas de inventario, búsquedas y sugerencias del POS, pero sus nombres y datos históricos permanecen intactos en las ventas registradas con anterioridad.

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
