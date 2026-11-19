[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [15.a Estrategia de pruebas unitarias y cobertura en capa Util y ViewModel](#c15a)
- [15.b Pruebas de generador de códigos de barras e integridad EAN-13](#c15b)
- [15.c Pruebas de validación e importación/exportación JSON](#c15c)
- [15.d Pruebas de cálculo de rangos de fechas y zonas horarias](#c15d)
- [15.e Pruebas de instrumentalización Android (androidTest) y ejecución](#c15e)

---

<a id="c15"></a>
## 15. Pruebas

<a id="c15a"></a>
### 15.a Estrategia de pruebas unitarias y cobertura en capa Util y ViewModel

El proyecto incluye un conjunto de pruebas unitarias automatizadas orientadas a validar algoritmos críticos de lógica de negocio, generadores de códigos, parseo de fechas con zonas horarias, validadores JSON y lógica de presentación en ViewModels.

 Las pruebas locales de JVM se ubican en el directorio `app/src/test/java/com/example/gestioninventarios/`.

#### Resumen del Suite de Pruebas Unitarias (Local JUnit 4):
1. `LoginViewModelTest.kt`: Validación de formularios de autenticación (campos vacíos y formatos de correo).
2. `CodigoInternoTest.kt`: Validación del algoritmo de generación de códigos EAN-13 y formateo.
3. `Ean13ValidatorTest.kt`: Pruebas de la función de validación del dígito verificador.
4. `ReportesRangoTest.kt`: Validación de cálculo de rangos de fechas y zonas horarias (UTC vs. El Salvador).
5. `RespaldoJsonManagerTest.kt`: Pruebas de resolución de nombres/códigos únicos y validación de esquemas JSON v1 y v2.
6. `ExampleUnitTest.kt`: Prueba de verificación base del entorno de pruebas.

---

<a id="c15b"></a>
### 15.b Pruebas de generador de códigos de barras e integridad EAN-13

Ubicación: [app/src/test/java/com/example/gestioninventarios/util/CodigoInternoTest.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/test/java/com/example/gestioninventarios/util/CodigoInternoTest.kt) y [Ean13ValidatorTest.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/test/java/com/example/gestioninventarios/util/Ean13ValidatorTest.kt).

#### Casos de Prueba Evaluados:
- `testGenerarCodigoInternoValoresEspecificos()`: Verifica que la entrada de categoría, proveedor y consecutivo genere exactamente la cadena EAN-13 esperada:
  - `generar(1, 1, 1)` -> `"2010100000013"` (Dígito verificador 3).
  - `generar(1, 2, 1)` -> `"2010200000012"` (Dígito verificador 2).
  - `generar(3, 1, 12)` -> `"2030100000127"` (Dígito verificador 7).
  - `generar(0, 0, 1)` -> `"2000000000015"` (Dígito verificador 5).
- `testEsEan13Valido()`: Evalúa la detección de códigos válidos (`"4006381333931"`) y la detección de códigos con dígitos verificadores alterados (`"4006381333932"` -> `false`).
- `testFormatear()`: Verifica que la función formateadora inserte los espacios visuales adecuados: `"2010100000013"` -> `"2 01 01 0000001 3"`.

---

<a id="c15c"></a>
### 15.c Pruebas de validación e importación/exportación JSON

Ubicación: [app/src/test/java/com/example/gestioninventarios/util/RespaldoJsonManagerTest.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/test/java/com/example/gestioninventarios/util/RespaldoJsonManagerTest.kt).

#### Casos de Prueba Evaluados:
- `hacerNombreUnico_sinColision_devuelveMismoNombre()`: Verifica que si el nombre no existe, se conserve intacto.
- `hacerNombreUnico_conColisionesConsecutivas_agregaSufijoNumerado()`: Evalúa que ante colisiones múltiples (`"bebidas"`, `"bebidas (importado)"`), la función asigne `"Bebidas (importado) 3"`.
- `hacerCodigoBarrasUnico_conColision_agregaSufijoI()`: Verifica la adición de sufijos `"-I2"` ante códigos de barras existentes.
- `validarJsonImportacion_appIncorrecta_falla()`: Comprueba que archivos JSON con `app != "InventarioPYME"` sean rechazados inmediatamente.
- `validarJsonImportacion_versionMasReciente_falla()`: Comprueba que archivos con `formato > FORMATO_VERSION` sean rechazados.
- `validarJsonImportacion_formato1ConUsuarios_correcto()`: Verifica la retrocompatibilidad con archivos de respaldo versión 1 que incluyen la estructura de usuarios.

---

<a id="c15d"></a>
### 15.d Pruebas de cálculo de rangos de fechas y zonas horarias

Ubicación: [app/src/test/java/com/example/gestioninventarios/util/ReportesRangoTest.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/test/java/com/example/gestioninventarios/util/ReportesRangoTest.kt).

#### Casos de Prueba Evaluados:
- `testReportesRangoZonaHorariaElSalvador()`:
  - Configura la zona horaria a `America/El_Salvador` (UTC-6) con fecha de evaluación el 9 de noviembre de 2026.
  - Evalúa una venta registrada localmente el 9 nov a las 22:18 (almacenada en UTC como `"2026-11-10T04:18:00Z"`). Verifica que `ms2218` caiga dentro del rango de la semana.
  - Evalúa una venta registrada localmente el 9 nov a las 23:59 (en UTC `"2026-11-10T05:59:00Z"`). Verifica que pertenezca al día 9 nov.
  - Evalúa una venta registrada el 10 nov a las 00:01 (en UTC `"2026-11-10T06:01:00Z"`). Verifica que quede excluida del rango cuando la fecha actual de consulta es el 9 nov.

---

<a id="c15e"></a>
### 15.e Pruebas de instrumentalización Android (androidTest) y ejecución

Ubicación: `app/src/androidTest/java/com/example/gestioninventarios/ExampleInstrumentedTest.kt`.

#### Estructura de Pruebas Instrumentadas:
Utilizan el runner `androidx.test.ext.junit.runners.AndroidJUnit4` para ejecutarse en un dispositivo o emulador Android real. Verifican el contexto de la aplicación y la integración con el paquete `com.example.gestioninventarios`.

#### Instrucciones para Ejecutar las Pruebas:
- **Ejecutar Pruebas Unitarias desde Terminal**:
  ```bash
  ./gradlew test
  ```
- **Ejecutar Pruebas Instrumentadas desde Terminal**:
  ```bash
  ./gradlew connectedAndroidTest
  ```

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
