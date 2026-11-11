package com.example.gestioninventarios.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gestioninventarios.AppContainer
import com.example.gestioninventarios.ui.ViewModelFactory
import com.example.gestioninventarios.ui.catalogos.CatalogosScreen
import com.example.gestioninventarios.ui.catalogos.CatalogosViewModel
import com.example.gestioninventarios.ui.home.HomeScreen
import com.example.gestioninventarios.ui.home.HomeViewModel
import com.example.gestioninventarios.ui.login.LoginScreen
import com.example.gestioninventarios.ui.login.LoginViewModel
import com.example.gestioninventarios.ui.productos.ProductoFormScreen
import com.example.gestioninventarios.ui.productos.ProductoFormViewModel
import com.example.gestioninventarios.ui.productos.ProductosScreen
import com.example.gestioninventarios.ui.productos.ProductosViewModel
import com.example.gestioninventarios.ui.reportes.ReportesScreen
import com.example.gestioninventarios.ui.reportes.ReportesViewModel
import com.example.gestioninventarios.ui.scanner.ScannerScreen
import com.example.gestioninventarios.ui.usuarios.UsuariosScreen
import com.example.gestioninventarios.ui.usuarios.UsuariosViewModel
import com.example.gestioninventarios.ui.venta.VentaScreen
import com.example.gestioninventarios.ui.venta.VentaViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val isLoggedIn by appContainer.authRepository.isLoggedIn.collectAsState(initial = false)
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route

    val factory = ViewModelFactory(appContainer)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        appContainer.sesionManager.expirationEvent.collect { motivo ->
            loginViewModel.resetForm()
            loginViewModel.setErrorMessage(motivo)
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToPos = { navController.navigate(Screen.Venta.route) },
                onNavigateToProductos = { navController.navigate(Screen.Productos.route) },
                onNavigateToCatalogos = { navController.navigate(Screen.Catalogos.route) },
                onNavigateToReportes = { navController.navigate(Screen.Reportes.route) },
                onNavigateToUsuarios = { navController.navigate(Screen.Usuarios.route) },
                onLogoutSuccess = {
                    loginViewModel.resetForm()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Productos.route) {
            val productosViewModel: ProductosViewModel = viewModel(factory = factory)
            LaunchedEffect(Unit) {
                productosViewModel.cargarProductos(isRefresh = true)
            }
            ProductosScreen(
                viewModel = productosViewModel,
                onNavigateToCreateProduct = { navController.navigate(Screen.ProductoForm.createRoute()) },
                onNavigateToEditProduct = { id -> navController.navigate(Screen.ProductoForm.createRoute(id)) },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ProductoForm.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val idStr = backStackEntry.arguments?.getString("id")
            val productId = idStr?.toIntOrNull()
            val formViewModel: ProductoFormViewModel = viewModel(factory = factory)

            ProductoFormScreen(
                viewModel = formViewModel,
                navController = navController,
                productoId = productId,
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                onNavigateToEditProduct = { editId ->
                    navController.navigate(Screen.ProductoForm.createRoute(editId)) {
                        popUpTo(Screen.ProductoForm.route) { inclusive = true }
                    }
                },
                onSaveSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Catalogos.route) {
            val catalogosViewModel: CatalogosViewModel = viewModel(factory = factory)
            CatalogosScreen(
                viewModel = catalogosViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Venta.route) { backStackEntry ->
            val ventaViewModel: VentaViewModel = viewModel(factory = factory)

            val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")
            if (!scannedBarcode.isNullOrEmpty()) {
                ventaViewModel.onBarcodeScanned(scannedBarcode)
                backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
            }

            VentaScreen(
                viewModel = ventaViewModel,
                onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                onNavigateToProductos = { navController.navigate(Screen.Productos.route) },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                onBarcodeScanned = { barcode ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                    navController.previousBackStackEntry?.savedStateHandle?.set("codigo", barcode)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reportes.route) {
            val reportesViewModel: ReportesViewModel = viewModel(factory = factory)
            ReportesScreen(
                viewModel = reportesViewModel,
                onNavigateToPos = { navController.navigate(Screen.Venta.route) },
                onNavigateToProductos = { navController.navigate(Screen.Productos.route) },
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Usuarios.route) {
            val usuariosViewModel: UsuariosViewModel = viewModel(factory = factory)
            UsuariosScreen(
                viewModel = usuariosViewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
