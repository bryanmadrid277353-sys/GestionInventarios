[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [13.a Sistema de diseño Material 3 y esquema de colores semánticos (Theme / Color / Type)](#c13a)
- [13.b Soporte completo para tema claro y oscuro](#c13b)
- [13.c Componentes UI reutilizables y contenedores principales (Componentes.kt)](#c13c)
- [13.d Estructura base de pantallas (AppScaffold) y manejo de insets de sistema](#c13d)

---

<a id="c13"></a>
## 13. Interfaz de usuario

<a id="c13a"></a>
### 13.a Sistema de diseño Material 3 y esquema de colores semánticos (Theme / Color / Type)

La interfaz de usuario de **Inventario PyME** está desarrollada de manera integral con **Jetpack Compose** aplicando la especificación **Material Design 3 (M3)**.

#### Configuración del Tema ([Theme.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/theme/Theme.kt)):
- **Esquema de Colores Primarios**: Basado en una paleta azul petróleo profesional con contrastes elevados para entornos comerciales.
- **Tipografía ([Type.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/theme/Type.kt))**: Escala tipográfica estandarizada utilizando `FontFamily.Default` y `FontFamily.Monospace` para códigos numéricos y precios.
- **Formas y Redondeos (`Formas`)**:
  - `small`: 8.dp (Chips, campos de entrada).
  - `medium`: 12.dp (Tarjetas de productos, alertas).
  - `large`: 16.dp (Tarjetas principales, modales).
  - `extraLarge`: 28.dp (Barra de búsqueda).
- **Constantes de Espaciado (`Espacio`)**: `xs = 4.dp`, `s = 8.dp`, `m = 16.dp`, `l = 24.dp`, `xl = 32.dp`.

#### Sistema de Colores Semánticos Personalizados (`ColoresSemanticos`):
Material 3 no incluye por defecto colores de éxito o advertencia. La aplicación extiende la jerarquía de Compose agregando una estructura inmutable inyectada a través de `CompositionLocalProvider`:

```kotlin
@Immutable
data class ColoresSemanticos(
    val exito: Color, val exitoContenedor: Color, val onExitoContenedor: Color,
    val advertencia: Color, val advertenciaContenedor: Color, val onAdvertenciaContenedor: Color
)
```

Acceso global mediante `Semantico.colores.exito` o `Semantico.colores.advertenciaContenedor`.

---

<a id="c13b"></a>
### 13.b Soporte completo para tema claro y oscuro

La aplicación proporciona soporte nativo completo para los modos **Claro** y **Oscuro**, garantizando legibilidad y confort visual en cualquier entorno de iluminación.

| Token de Color | Tema Claro | Tema Oscuro | Aplicación en la Interfaz |
| :--- | :--- | :--- | :--- |
| `primary` | `#00658B` | `#7DD3FC` | Botones primarios, enlaces y barras de gráficos. |
| `primaryContainer` | `#C7EBFF` | `#004D6D` | Fondo de barra superior (`AppTopBar`), tarjetas destacadas y contenedor de total. |
| `background` / `surface` | `#F5FAFC` | `#0D1519` | Fondo general de todas las pantallas de la app. |
| `surfaceContainerHigh` | `#E3E9EC` | `#222C31` | Tarjetas secundarias y contenedores de formularios. |
| `exitoContenedor` | `#CDF5E3` | `#0F3D2E` | Insignias de stock normal y resúmenes de ganancias. |
| `advertenciaContenedor` | `#FFE9B8` | `#4A3600` | Banners de advertencia de stock bajo y chips de estado. |
| `errorContainer` | `#FFFFDAD6` | `#93000A` | Errores de validación, diálogos de eliminación y stock agotado. |

---

<a id="c13c"></a>
### 13.c Componentes UI reutilizables y contenedores principales (Componentes.kt)

Todos los componentes reutilizables de la interfaz residen en [Componentes.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/components/Componentes.kt).

#### Resumen de Componentes Reutilizables:
1. `AppTopBar`: Barra de navegación superior estandarizada con título, botón de retorno (`ArrowBack`) e íconos de acción.
2. `StatusChip`: Insignia redondeada para mostrar estados (`Exito`, `Advertencia`, `Error`, `Info`, `Neutro`).
3. `StockBadge`: Insignia inteligente que evalúa las existencias e imprime *"Agotado"*, *"Stock bajo: N"* o *"Stock: N"*.
4. `KpiCard`: Tarjeta de indicador clave con ícono circular, valor principal destacado y etiqueta descriptiva.
5. `ConfirmDialog`: Diálogo de confirmación con opciones personalizables y soporte para estilos destructivos (rojo).
6. `BuscadorCampo`: Campo de búsqueda de forma ovalada (`RoundedCornerShape(28.dp)`) con ícono de lupa y botón de limpieza rápida (`Clear`).
7. `EsqueletoCarga` (*Shimmer Skeleton*): Animación de carga parpadeante que simula la estructura de las listas mientras se leen los datos desde SQLite.
8. `AvatarIniciales`: Círculo con las dos iniciales del nombre de una persona o usuario.
9. `EstadoVacio`: Contenedor centrado con ícono grande, título y mensaje explicativo cuando una lista o consulta no arroja resultados.
10. `formatoMoneda` / `formatoMonedaCompacta`: Funciones auxiliares para formatear valores numéricos a moneda local (ej. `$1,250.00` o `$1.3k`).

---

<a id="c13d"></a>
### 13.d Estructura base de pantallas (AppScaffold) y manejo de insets de sistema

Todas las pantallas de la aplicación utilizan el contenedor [AppScaffold](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/components/Componentes.kt).

#### Gestión de Insets del Sistema y Teclado:
- **Edge-to-Edge**: `MainActivity` activa `enableEdgeToEdge()`, dibujando el contenido detrás de las barras del sistema (barra de estado y barra de navegación).
- **Manejo de Insets**: El `AppScaffold` provee los valores de `PaddingValues` en su lambda de contenido (`calculateTopPadding()` y `calculateBottomPadding()`). Las pantallas aplican estos valores para garantizar que el contenido no quede oculto bajo los botones del sistema.
- **Teclado Virtual y Teclas del Sistema**: Los formularios y pantallas con campos de texto incorporan los modificadores `.navigationBarsPadding()` e `.imePadding()`. Esto eleva automáticamente los botones y campos de entrada cuando el teclado virtual aparece en pantalla, evitando que la interfaz se solape o se vuelva inaccesible.

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
