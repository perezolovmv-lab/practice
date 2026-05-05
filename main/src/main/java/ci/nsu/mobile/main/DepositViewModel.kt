package ci.nsu.mobile.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ci.nsu.mobile.main.ui.DepositViewModel
import ci.nsu.mobile.main.ui.theme.PracticeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticeTheme {
                val navController = rememberNavController()
                val vm: DepositViewModel = viewModel()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(navController) }
                    composable("step1") { Step1Screen(navController, vm) }
                    composable("step2") { Step2Screen(navController, vm) }
                    composable("result") { ResultScreen(navController, vm) }
                    composable("history") { HistoryScreen(navController, vm) }
                }
            }
        }
    }
}