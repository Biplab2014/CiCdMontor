package com.app.cicdmonitor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.cicdmonitor.ui.screens.dashboard.DashboardScreen
import com.app.cicdmonitor.ui.screens.login.LoginScreen
import com.app.cicdmonitor.ui.screens.pipeline.PipelineDetailScreen
import com.app.cicdmonitor.ui.screens.settings.SettingsScreen

@Composable
fun CiCdNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = CiCdDestinations.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(CiCdDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(CiCdDestinations.DASHBOARD) {
                        popUpTo(CiCdDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(CiCdDestinations.DASHBOARD) {
            DashboardScreen(
                onPipelineClick = { pipelineId ->
                    navController.navigate("${CiCdDestinations.PIPELINE_DETAIL}/$pipelineId")
                },
                onSettingsClick = {
                    navController.navigate(CiCdDestinations.SETTINGS)
                }
            )
        }
        
        composable("${CiCdDestinations.PIPELINE_DETAIL}/{pipelineId}") { backStackEntry ->
            val pipelineId = backStackEntry.arguments?.getString("pipelineId") ?: ""
            PipelineDetailScreen(
                pipelineId = pipelineId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(CiCdDestinations.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    navController.navigate(CiCdDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

object CiCdDestinations {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PIPELINE_DETAIL = "pipeline_detail"
    const val SETTINGS = "settings"
}
