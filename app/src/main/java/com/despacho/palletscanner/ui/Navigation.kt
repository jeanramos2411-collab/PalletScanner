package com.despacho.palletscanner.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.despacho.palletscanner.ui.screens.*
import com.despacho.palletscanner.viewmodels.MainViewModel
import com.despacho.palletscanner.viewmodels.TesteadorViewModel
import com.despacho.palletscanner.data.models.ServerConfiguration

import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun Navigation(
    viewModel: MainViewModel,
    configurationService: com.despacho.palletscanner.data.services.ConfigurationService
) {
    val navController = rememberNavController()
    val serverConfig by configurationService.serverConfiguration.collectAsState(initial = ServerConfiguration())
    val coroutineScope = rememberCoroutineScope()

    // TesteadorViewModel comparte el SignalRService del MainViewModel
    val testeadorViewModel = remember {
        TesteadorViewModel(viewModel.signalRService)
    }

    // Determinar pantalla inicial basada en configuracion
    val startDestination = if (serverConfig.isValid()) "module_selector" else "configuration"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("configuration") {
            ConfigurationScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveConfiguration = { config ->
                    coroutineScope.launch {
                        configurationService.saveServerConfiguration(config)
                        viewModel.connectToServer(config)
                    }
                    navController.navigate("module_selector") {
                        popUpTo("configuration") { inclusive = true }
                    }
                },
                currentConfig = serverConfig
            )
        }

        composable("module_selector") {
            ModuleSelectorScreen(
                onNavigateToDespacho = {
                    navController.navigate("main")
                },
                onNavigateToTesteador = {
                    navController.navigate("testeador")
                },
                onNavigateToConfiguration = {
                    navController.navigate("configuration")
                },
                connectionState = viewModel.connectionState.collectAsState().value
            )
        }

        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToPalletList = {
                    navController.navigate("pallet_list")
                },
                onNavigateToConfiguration = {
                    navController.navigate("configuration")
                }
            )
        }

        composable("pallet_list") {
            PalletListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("testeador") {
            TesteadorScreen(
                viewModel = testeadorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}