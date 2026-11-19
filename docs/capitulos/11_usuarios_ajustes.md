[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)

### En este capítulo
- [11.a Gestión de usuarios y asignación de roles (UsuariosScreen / UsuariosViewModel)](#c11a)
- [11.b Control de estado de usuarios (Activo / Inactivo) y reglas de auto-modificación](#c11b)
- [11.c Configuración de apariencia y temas de interfaz (AjustesScreen / AjustesViewModel)](#c11c)
- [11.d Panel de Ajustes Avanzados para administración del sistema](#c11d)

---

<a id="c11"></a>
## 11. Gestión de usuarios y Ajustes

<a id="c11a"></a>
### 11.a Gestión de usuarios y asignación de roles (UsuariosScreen / UsuariosViewModel)

El módulo de administración de usuarios y personal se implementa en [UsuariosScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/usuarios/UsuariosScreen.kt) y [UsuariosViewModel.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/usuarios/UsuariosViewModel.kt). Esta pantalla es accesible exclusivamente para usuarios con rol `ADMIN`.

#### Alta de Cuentas de Usuario (`FormularioUsuarioBottomSheet`):
1. **Campos del Formulario**:
   - `Nombre Completo`: Requerido.
   - `Correo Electrónico`: Requerido. Se valida que cumpla con el formato estándar de correo (`Patterns.EMAIL_ADDRESS`) y se verifica que no esté registrado previamente en SQLite (`usuarioDao.getUsuarioByEmail`).
   - `Contraseña`: Requerida. Se valida que contenga una longitud mínima de 6 caracteres.
   - `Rol en el Sistema`: Selector de dos opciones (`SegmentedButton`): `VENDEDOR` o `ADMIN`.
2. **Avatar de Iniciales**: Cada tarjeta de usuario renderiza automáticamente un avatar circular (`AvatarIniciales`) generado a partir de las iniciales del nombre del usuario.

---

<a id="c11b"></a>
### 11.b Control de estado de usuarios (Activo / Inactivo) y reglas de auto-modificación

El sistema permite desactivar temporalmente el acceso a un usuario sin borrar sus registros históricos de ventas:

#### 1. Inversión de Estado (`desactivarUsuario`):
Al presionar el menú contextual de un usuario, el administrador puede conmutar su estado entre `Activo` e `Inactivo`. Un usuario con estado `Inactivo` no puede iniciar sesión en la pantalla de Login (el sistema muestra error: *"Usuario no encontrado o inactivo"*).

#### 2. Regla de Protección contra Auto-Desactivación:
Para evitar que un administrador bloquee accidentalmente su propia cuenta y quede excluido del sistema, el `UsuariosViewModel` aplica una regla de validación al seleccionar la opción de cambio de estado:

```kotlin
fun promptToggleStatus(usr: UsuarioResponseDto) {
    if (usr.email.equals(_uiState.value.currentEmail, ignoreCase = true)) {
        _uiState.update {
            it.copy(
                showBlockedDialog = true,
                blockedDialogMessage = "No puedes cambiar el estado de tu propia cuenta de usuario."
            )
        }
        return
    }
    // Procede con el diálogo de confirmación...
}
```

---

<a id="c11c"></a>
### 11.c Configuración de apariencia y temas de interfaz (AjustesScreen / AjustesViewModel)

La pantalla de ajustes generales se implementa en [AjustesScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesScreen.kt) y [AjustesViewModel.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesViewModel.kt).

#### Modos de Tema Soportados ([PreferenciasManager.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/data/local/PreferenciasManager.kt)):
1. `AUTOMATICO`: El tema de la aplicación responde al modo claro u oscuro configurado en el sistema operativo Android.
2. `CLARO`: Fuerza el esquema de colores en modo claro.
3. `OSCURO`: Fuerza el esquema de colores en modo oscuro.

#### Aplicación Dinámica del Tema:
El modo seleccionado se almacena en `SharedPreferences` y se expone como `StateFlow<String>`. En [MainActivity.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/MainActivity.kt), la actividad observa este flujo y ajusta dinámicamente el contenedor `InventarioTheme` y el estilo de las barras de estado del sistema (`enableEdgeToEdge`):

```kotlin
val useDarkTheme = when (modoTemaState) {
    PreferenciasManager.TEMA_CLARO -> false
    PreferenciasManager.TEMA_OSCURO -> true
    else -> isSystemInDarkTheme()
}
```

---

<a id="c11d"></a>
### 11.d Panel de Ajustes Avanzados para administración del sistema

Los usuarios con rol `ADMIN` visualizan la tarjeta **"Ajustes Avanzados"** en la pantalla de ajustes, la cual da acceso a [AjustesAvanzadosScreen.kt](file:///C:/Users/Administrador/Desktop/GestionInventarios/app/src/main/java/com/example/gestioninventarios/ui/ajustes/AjustesAvanzadosScreen.kt).

Este panel contiene las herramientas críticas para la gestión del almacenamiento local, exportación e importación de respaldos JSON, y el procedimiento de eliminación total de la base de datos (detallado extensamente en el Capítulo 12).

[← Volver al índice](../DOCUMENTACION_TECNICA.md#indice)
