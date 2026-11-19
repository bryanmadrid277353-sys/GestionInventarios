[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [9.a Integración de CameraX y procesamiento de cámara (ScannerScreen)](#c9a)
- [9.b Escaneo e interpretación de códigos de barras con ML Kit](#c9b)
- [9.c Gestión de permisos de cámara y fallback de linterna](#c9c)
- [9.d Interfaz visual de escaneo y retorno de resultados](#c9d)

---

<a id="c9"></a>
## 9. Escáner de códigos de barras

<a id="c9a"></a>
### 9.a Integración de CameraX y procesamiento de cámara (ScannerScreen)

El componente de captura de vídeo y escaneo de códigos de barras se implementa en [ScannerScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/scanner/ScannerScreen.kt) mediante el framework oficial **Jetpack CameraX**.

#### Arquitectura de Captura con CameraX:
- **PreviewView**: Vista nativa embebida en Jetpack Compose mediante `AndroidView` para proyectar el flujo de vídeo de la cámara en pantalla.
- **ProcessCameraProvider**: Administrador del ciclo de vida de la cámara enlazado a la pantalla con `cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)`.
- **ImageAnalysis**: Caso de uso configurado con la estrategia de contrapresión `STRATEGY_KEEP_ONLY_LATEST`, la cual descarta fotogramas intermedios si el procesador está ocupado, garantizando una tasa de refresco fluida sin acumulación de memoria.
- **Executor Secundario**: El procesamiento de imágenes se ejecuta en un hilo secundario mediante `Executors.newSingleThreadExecutor()`.

---

<a id="c9b"></a>
### 9.b Escaneo e interpretación de códigos de barras con ML Kit

El reconocimiento automático de códigos de barras se realiza utilizando el SDK en vivo de **Google ML Kit Barcode Scanning** (`com.google.mlkit:barcode-scanning:17.3.0`).

#### Configuración de Formatos Aceptados:
Para optimizar el rendimiento y prevenir falsos positivos con códigos QR o matrices 2D, el escáner se restringe explícitamente a simbologías comerciales de líneas unidimensionales (1D):

```kotlin
val options = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(
        Barcode.FORMAT_EAN_13,
        Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A,
        Barcode.FORMAT_UPC_E,
        Barcode.FORMAT_CODE_128
    )
    .build()
val barcodeScanner = BarcodeScanning.getClient(options)
```

#### Flujo de Análisis de Fotogramas:
1. `ImageAnalysis` extrae el objeto `ImageProxy` de la cámara.
2. Convierte la imagen al objeto `InputImage.fromMediaImage(mediaImage, rotationDegrees)`.
3. `barcodeScanner.process(image)` analiza la imagen de forma asíncrona.
4. Al detectar un código válido, extrae la cadena alfanumérica `barcode.rawValue`.

---

<a id="c9c"></a>
### 9.c Gestión de permisos de cámara y fallback de linterna

#### 1. Gestión de Permisos Runtime:
- Al ingresar a `ScannerScreen`, la aplicación verifica si el permiso `android.permission.CAMERA` se encuentra concedido utilizando `ContextCompat.checkSelfPermission`.
- Si el permiso no está concedido, activa automáticamente el diálogo de solicitud nativo mediante `rememberLauncherForActivityResult(RequestPermission())`.
- Si el usuario deniega el permiso, se muestra una interfaz alternativa indicando que el permiso es requerido con el botón *"Otorgar Permiso de Cámara"*.

#### 2. Control de Linterna (Flash):
En la barra superior de la pantalla (`AppTopBar`), se incluye un botón de acción con ícono conmutable (`Icons.Default.FlashOn` / `FlashOff`). Al presionar el botón, se obtiene la referencia al control de la cámara (`cameraRef.cameraControl`) e invoca `enableTorch(isTorchOn)`, permitiendo iluminar códigos de barras en entornos con escasa luz.

---

<a id="c9d"></a>
### 9.d Interfaz visual de escaneo y retorno de resultados

#### 1. Marco Visor Personalizado (`MarcoVisorEscaneo`):
La pantalla dibuja una superposición de enfoque utilizando el componente `Canvas` de Compose:
- **Fondo Oscurecido**: Renderiza un rectángulo semitransparente negro (`Color.Black.copy(alpha = 0.5f)`) sobre toda la superficie.
- **Recorte Transparente**: Recorta un visor central con esquinas redondeadas (`CornerRadius(16.dp)`) utilizando el modo de mezcla `BlendMode.Clear`.
- **Marco de Enfoque**: Dibuja un borde de color cian (`Color(0xFF22D3EE)`) alrededor del visor.
- **Línea Láser Central**: Dibuja una línea horizontal roja semitransparente en el centro del marco simulando un escáner láser físico.

#### 2. Retroalimentación Háptica y Sonora:
Al detectar un código exitoso, el método privado `vibrarAlEscanear` dispara una pulsación de vibración breve (120 ms) utilizando el servicio `Vibrator` o `VibratorManager` del sistema Android (con fallback a `LocalHapticFeedback`).

#### 3. Retorno del Resultado a la Pantalla Anterior:
Una vez detectado y procesado el código, la pantalla establece el valor en el `savedStateHandle` de la pila de navegación y regresa inmediatamente:

```kotlin
onBarcodeScanned = { barcode ->
    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
    navController.previousBackStackEntry?.savedStateHandle?.set("codigo", barcode)
    navController.popBackStack()
}
```

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
