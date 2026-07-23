package com.voiddrift.game.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voidrift.game.presentation.game.GameScreen
import com.voidrift.game.presentation.home.HomeScreen
import com.voidrift.game.presentation.runhistory.RunHistoryScreen
import com.voidrift.game.presentation.settings.SettingsScreen
import com.voidrift.game.ui.theme.VoidBackground
import com.voidrift.game.ui.theme.VoidDriftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        setContent {
            VoidDriftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = VoidBackground
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onStartGame  = { navController.navigate("game") },
                                onRunHistory = { navController.navigate("history") },
                                onSettings   = { navController.navigate("settings") }
                            )
                        }

                        composable("game") {
                            GameScreen(
                                onNavigateHome = {
                                    navController.popBackStack("home", inclusive = false)
                                }
                            )
                        }

                        composable("history") {
                            RunHistoryScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
