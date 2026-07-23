package com.voidrift.game.presentation.game

import com.voidrift.game.domain.model.GameWorld

sealed class GameUiState {
    data object Idle        : GameUiState()
    data object Loading     : GameUiState()
    data class  Playing(val world: GameWorld) : GameUiState()
    data class  Paused(val world: GameWorld)  : GameUiState()
    data class  GameOver(val world: GameWorld, val causeOfDeath: String) : GameUiState()
    data class  RoomCleared(val world: GameWorld) : GameUiState()
}
