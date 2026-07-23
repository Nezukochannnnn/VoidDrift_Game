package com.voiddrift.game.presentation.model

import com.voiddrift.game.domain.model.GameState
import com.voiddrift.game.domain.model.ScoreRecord

data class GameUiState(
    val gameState: GameState = GameState(),
    val highScore: ScoreRecord? = null,
    val isShaderSupported: Boolean = false,
    val currentFps: Int = 60,
    val isGameOver: Boolean = false
)
