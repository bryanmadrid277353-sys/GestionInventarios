package com.example.gestioninventarios.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Productos : Screen("productos")
    object ProductoForm : Screen("producto_form?id={id}") {
        fun createRoute(id: Int? = null) = if (id != null) "producto_form?id=$id" else "producto_form"
    }
    object Catalogos : Screen("catalogos")
    object Venta : Screen("venta")
    object Scanner : Screen("scanner")
    object Reportes : Screen("reportes")
    object Usuarios : Screen("usuarios")
}
