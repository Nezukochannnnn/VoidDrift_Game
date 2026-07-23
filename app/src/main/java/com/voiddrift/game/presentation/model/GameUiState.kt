package com.voiddrift.game.presentation.model

/**
 * Sealed class representing the overall UI state of the dungeon game screen.
 * The game loop runs while [GameUiState] == [Playing].
 */
sealed class GameUiState {
    data object Idle     : GameUiState()
    data object Playing  : GameUiState()
    data object Paused   : GameUiState()
    data object GameOver : GameUiState()
    data object RoomCleared : GameUiState()
}
