package com.voiddrift.game.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object MainMenu    : NavRoutes("main_menu")
    data object DungeonGame : NavRoutes("dungeon_game")      // Dungeon crawler game
    data object GameCanvas  : NavRoutes("game_canvas")       // Space shooter canvas
    data object Leaderboard : NavRoutes("leaderboard")
    data object RunHistory  : NavRoutes("run_history")
    data object Settings    : NavRoutes("settings")
    data object HowToPlay   : NavRoutes("how_to_play")
}
