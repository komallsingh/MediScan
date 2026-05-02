package com.komal.mediscan.Navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.komal.mediscan.MVVM.MediScanViewModel
import com.komal.mediscan.Screen.ConfirmScreen
import com.komal.mediscan.Screen.ProcessingScreen
import com.komal.mediscan.Screen.ResultScreen
import com.komal.mediscan.Screen.UploadScreen
import com.komal.mediscan.Screen.WelcomeScreen

// NavGraph.kt
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Upload : Screen("upload")
    object Processing : Screen("processing/{imagePath}") {
        fun createRoute(path: String) = "processing/$path"
    }
    object Confirm : Screen("confirm")
    object Result : Screen("result")
}

@Composable
fun MediScanNavGraph() {
    val navController = rememberNavController()
    val sharedVM: MediScanViewModel = viewModel()

    NavHost(navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Upload.route) { UploadScreen(navController, sharedVM) }
        composable(Screen.Processing.route) { ProcessingScreen(navController, sharedVM) }
        composable(Screen.Confirm.route) { ConfirmScreen(navController, sharedVM) }
        composable(Screen.Result.route) { ResultScreen(navController, sharedVM) }
    }
}