package com.voiddrift.game.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object MainMenu : NavRoutes("main_menu")
    data object GameCanvas : NavRoutes("game_canvas")
    data object Leaderboard : NavRoutes("leaderboard")
    data object Settings : NavRoutes("settings")
    data object HowToPlay : NavRoutes("how_to_play")
}
