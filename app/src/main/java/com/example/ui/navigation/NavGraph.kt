package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.create.CreateScreen
import com.example.ui.customize.CustomizeScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.preview.QrPreviewScreen
import com.example.ui.scan.ScanScreen
import com.example.ui.settings.SettingsScreen
import com.example.viewmodel.MainViewModel

@Composable
fun QrStudioNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen.route) },
                onSelectType = { type -> viewModel.selectType(type) },
                onSelectHistoryItem = { item ->
                    viewModel.loadFromEntity(item)
                    navController.navigate(Screen.Preview.route)
                }
            )
        }

        composable(Screen.Create.route) {
            CreateScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }

        composable(Screen.Customize.route) {
            CustomizeScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }

        composable(Screen.Preview.route) {
            QrPreviewScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen.route) }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(viewModel = viewModel)
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigate = { screen -> navController.navigate(screen.route) },
                onSelectItem = { item ->
                    viewModel.loadFromEntity(item)
                    navController.navigate(Screen.Preview.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
    }
}
